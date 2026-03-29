package com.example.clouduser.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.clouduser.mapper.UserMapper;
import org.example.common.Result;
import org.example.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/v1/admin/user")
public class AdminUserController {

    private static final String DEFAULT_PASSWORD = "030219";

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/list")
    public Result<List<User>> list(
            @RequestParam(required = false) Integer roleType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (roleType != null) {
            queryWrapper.eq(User::getRoleType, roleType);
        }
        if (status != null) {
            queryWrapper.eq(User::getStatus, status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim();
            queryWrapper.and(w -> w.like(User::getUsername, k).or().like(User::getRealName, k).or().like(User::getStudentNo, k));
        }
        queryWrapper.orderByDesc(User::getCreateTime);
        List<User> users = userMapper.selectList(queryWrapper);
        if (users != null) {
            for (User u : users) {
                if (u != null) u.setPassword(null);
            }
        }
        return Result.success(users);
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody User user) {
        if (user == null) return Result.fail("参数不能为空");
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) return Result.fail("用户名不能为空");
        if (user.getRealName() == null || user.getRealName().trim().isEmpty()) return Result.fail("姓名不能为空");
        if (user.getRoleType() == null || (user.getRoleType() != 2 && user.getRoleType() != 3)) return Result.fail("角色类型必须是教师(2)或学生(3)");

        String username = user.getUsername().trim();
        LambdaQueryWrapper<User> exists = new LambdaQueryWrapper<>();
        exists.eq(User::getUsername, username);
        if (userMapper.selectCount(exists) > 0) {
            return Result.fail("用户名已存在");
        }

        user.setUsername(username);
        user.setId(null);
        user.setPassword(DEFAULT_PASSWORD);
        if (user.getStatus() == null) user.setStatus(1);
        Date now = new Date();
        user.setCreateTime(now);
        user.setUpdateTime(now);

        userMapper.insert(user);
        return Result.success("新增成功，默认密码为 " + DEFAULT_PASSWORD);
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody User user) {
        if (user == null || user.getId() == null) return Result.fail("用户ID不能为空");
        User old = userMapper.selectById(user.getId());
        if (old == null) return Result.fail("用户不存在");
        if (old.getRoleType() != null && old.getRoleType() == 1) return Result.fail("不允许修改管理员账号");

        if (user.getUsername() != null && !user.getUsername().trim().isEmpty() && !user.getUsername().trim().equals(old.getUsername())) {
            String username = user.getUsername().trim();
            LambdaQueryWrapper<User> exists = new LambdaQueryWrapper<>();
            exists.eq(User::getUsername, username);
            if (userMapper.selectCount(exists) > 0) return Result.fail("用户名已存在");
            old.setUsername(username);
        }

        if (user.getRealName() != null) old.setRealName(user.getRealName());
        if (user.getStudentNo() != null) old.setStudentNo(user.getStudentNo());
        if (user.getPhone() != null) old.setPhone(user.getPhone());
        if (user.getEmail() != null) old.setEmail(user.getEmail());
        if (user.getStatus() != null) old.setStatus(user.getStatus());
        if (user.getRoleType() != null && (user.getRoleType() == 2 || user.getRoleType() == 3)) old.setRoleType(user.getRoleType());

        old.setUpdateTime(new Date());
        userMapper.updateById(old);
        return Result.success("修改成功");
    }

    @PostMapping("/status")
    public Result<String> status(@RequestParam Long id, @RequestParam Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) return Result.fail("用户不存在");
        if (user.getRoleType() != null && user.getRoleType() == 1) return Result.fail("不允许禁用管理员账号");
        if (status == null || (status != 0 && status != 1)) return Result.fail("状态必须是0或1");
        user.setStatus(status);
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
        return Result.success(status == 1 ? "已启用" : "已禁用");
    }

    @PostMapping("/resetPassword")
    public Result<String> resetPassword(@RequestParam Long id) {
        User user = userMapper.selectById(id);
        if (user == null) return Result.fail("用户不存在");
        if (user.getRoleType() != null && user.getRoleType() == 1) return Result.fail("不允许重置管理员账号密码");
        user.setPassword(DEFAULT_PASSWORD);
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
        return Result.success("密码已重置为 " + DEFAULT_PASSWORD);
    }

    @PostMapping("/delete")
    public Result<String> delete(@RequestParam Long id) {
        User user = userMapper.selectById(id);
        if (user == null) return Result.fail("用户不存在");
        if (user.getRoleType() != null && user.getRoleType() == 1) return Result.fail("不允许删除管理员账号");
        userMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
