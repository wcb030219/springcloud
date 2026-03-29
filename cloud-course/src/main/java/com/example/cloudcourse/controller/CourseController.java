package com.example.cloudcourse.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.cloudcourse.mapper.CourseCategoryMapper;
import com.example.cloudcourse.mapper.CourseMapper;
import com.example.cloudcourse.mapper.EvaluationStatsMapper;
import com.example.cloudcourse.mapper.SelectionMapper;
import com.example.cloudcourse.mapper.UserMapper;
import org.example.common.Result;
import org.example.pojo.Course;
import org.example.pojo.CourseCategory;
import org.example.pojo.CourseSelection;
import org.example.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class CourseController {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private SelectionMapper selectionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Autowired
    private EvaluationStatsMapper evaluationStatsMapper;

    /**
     * 学生在线选课
     */
    @PostMapping("/select")
    @Transactional // 开启事务
    public Result<String> selectCourse(
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam Long courseId) {
        userId = resolveUserId(userId, headerUserId);
        if (userId == null) return Result.fail("未登录");
        // 1. 查询并校验课程是否存在
        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getCourseStatus() != 1) {
            return Result.fail("课程不存在或已下架");
        }
        if (course.getCourseType() != null && course.getCourseType() == 2) {
            String ct = course.getClassTime() == null ? "" : course.getClassTime().trim();
            if ((!ct.contains("周三") && !ct.contains("周五")) || !ct.contains("下午")) {
                return Result.fail("选修课仅允许安排在周三/周五下午时段");
            }
        }

        String classTime = course.getClassTime() == null ? null : course.getClassTime().trim();
        if (classTime != null && !classTime.isEmpty()) {
            LambdaQueryWrapper<CourseSelection> selectedQuery = new LambdaQueryWrapper<>();
            selectedQuery.eq(CourseSelection::getStudentId, userId)
                    .eq(CourseSelection::getSelectionStatus, 1);
            List<CourseSelection> selected = selectionMapper.selectList(selectedQuery);
            if (selected != null && !selected.isEmpty()) {
                List<Long> courseIds = new ArrayList<>();
                for (CourseSelection s : selected) {
                    if (s.getCourseId() != null && !s.getCourseId().equals(courseId)) {
                        courseIds.add(s.getCourseId());
                    }
                }
                if (!courseIds.isEmpty()) {
                    List<Course> selectedCourses = courseMapper.selectBatchIds(courseIds);
                    if (selectedCourses != null) {
                        for (Course c : selectedCourses) {
                            String ct = c.getClassTime() == null ? null : c.getClassTime().trim();
                            if (ct != null && !ct.isEmpty() && ct.equals(classTime)) {
                                return Result.fail("选课冲突：与课程[" + c.getCourseName() + "]上课时间冲突（" + classTime + "）");
                            }
                        }
                    }
                }
            }
            List<Course> requiredCourses = listRequiredCourses(userId);
            for (Course c : requiredCourses) {
                String ct = c.getClassTime() == null ? null : c.getClassTime().trim();
                if (ct != null && !ct.isEmpty() && ct.equals(classTime)) {
                    return Result.fail("选课冲突：与必修课[" + c.getCourseName() + "]上课时间冲突（" + classTime + "）");
                }
            }
        }

        // 2. 校验重复选课（支持退课后重新选课）
        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSelection::getStudentId, userId)
                    .eq(CourseSelection::getCourseId, courseId);
        CourseSelection existedSelection = selectionMapper.selectOne(queryWrapper);
        if (existedSelection != null && existedSelection.getSelectionStatus() != null && existedSelection.getSelectionStatus() == 1) {
            return Result.fail("您已经选过这门课程，请勿重复选课");
        }

        // 3. 校验人数并更新已选人数 (乐观锁思路：SQL 层面判断容量)
        // 只有当 selected_count < capacity 时才更新，防止超卖
        LambdaUpdateWrapper<Course> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.setSql("selected_count = selected_count + 1")
                     .eq(Course::getId, courseId)
                     .lt(Course::getSelectedCount, course.getCapacity()); // 核心判断
        
        boolean updateResult = courseMapper.update(null, updateWrapper) > 0;
        if (!updateResult) {
            return Result.fail("选课失败，该课程选课人数已满");
        }

        Date now = new Date();
        // 4. 写入/更新选课记录
        if (existedSelection != null) {
            LambdaUpdateWrapper<CourseSelection> selectionUpdateWrapper = new LambdaUpdateWrapper<>();
            selectionUpdateWrapper
                    .set(CourseSelection::getSelectionStatus, 1)
                    .set(CourseSelection::getSelectionTime, now)
                    .set(CourseSelection::getDropTime, null)
                    .set(CourseSelection::getUpdateTime, now)
                    .eq(CourseSelection::getId, existedSelection.getId())
                    .eq(CourseSelection::getSelectionStatus, 0);
            boolean selectionUpdated = selectionMapper.update(null, selectionUpdateWrapper) > 0;
            if (!selectionUpdated) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return Result.fail("选课失败，请稍后重试");
            }
        } else {
            CourseSelection selection = new CourseSelection();
            selection.setStudentId(userId);
            selection.setCourseId(courseId);
            selection.setSelectionTime(now);
            selection.setSelectionStatus(1);
            selection.setCreateTime(now);
            selection.setUpdateTime(now);
            selectionMapper.insert(selection);
        }

        return Result.success("选课成功！");
    }

    /**
     * 学生在线退课
     */
    @PostMapping("/drop")
    @Transactional
    public Result<String> dropCourse(
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam Long courseId) {
        userId = resolveUserId(userId, headerUserId);
        if (userId == null) return Result.fail("未登录");
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            return Result.fail("课程不存在");
        }

        LambdaQueryWrapper<CourseSelection> selectionQueryWrapper = new LambdaQueryWrapper<>();
        selectionQueryWrapper.eq(CourseSelection::getStudentId, userId)
                .eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getSelectionStatus, 1);
        CourseSelection selection = selectionMapper.selectOne(selectionQueryWrapper);
        if (selection == null) {
            return Result.fail("您未选该课程，无法退课");
        }

        Date now = new Date();

        LambdaUpdateWrapper<CourseSelection> selectionUpdateWrapper = new LambdaUpdateWrapper<>();
        selectionUpdateWrapper
                .set(CourseSelection::getSelectionStatus, 0)
                .set(CourseSelection::getDropTime, now)
                .set(CourseSelection::getUpdateTime, now)
                .eq(CourseSelection::getId, selection.getId())
                .eq(CourseSelection::getSelectionStatus, 1);
        boolean selectionUpdated = selectionMapper.update(null, selectionUpdateWrapper) > 0;
        if (!selectionUpdated) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.fail("退课失败，请稍后重试");
        }

        LambdaUpdateWrapper<Course> courseUpdateWrapper = new LambdaUpdateWrapper<>();
        courseUpdateWrapper
                .setSql("selected_count = selected_count - 1")
                .eq(Course::getId, courseId)
                .gt(Course::getSelectedCount, 0);
        boolean courseUpdated = courseMapper.update(null, courseUpdateWrapper) > 0;
        if (!courseUpdated) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.fail("退课失败，请稍后重试");
        }

        return Result.success("退课成功！");
    }

    @GetMapping("/my/selected")
    public Result<List<CourseSelection>> mySelected(
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        userId = resolveUserId(userId, headerUserId);
        if (userId == null) return Result.fail("未登录");

        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSelection::getStudentId, userId)
                .eq(CourseSelection::getSelectionStatus, 1)
                .orderByDesc(CourseSelection::getSelectionTime);
        List<CourseSelection> selections = selectionMapper.selectList(queryWrapper);
        fillCourses(selections);
        return Result.success(selections);
    }

    @GetMapping("/my/required")
    public Result<List<Course>> myRequired(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return Result.fail("未登录");
        return Result.success(listRequiredCourses(userId));
    }

    @GetMapping("/my/drops")
    public Result<List<CourseSelection>> myDrops(
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        userId = resolveUserId(userId, headerUserId);
        if (userId == null) return Result.fail("未登录");

        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSelection::getStudentId, userId)
                .eq(CourseSelection::getSelectionStatus, 0)
                .orderByDesc(CourseSelection::getDropTime);
        List<CourseSelection> selections = selectionMapper.selectList(queryWrapper);
        fillCourses(selections);
        return Result.success(selections);
    }

    @GetMapping("/my/timetable")
    public Result<List<Map<String, Object>>> myTimetable(
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        userId = resolveUserId(userId, headerUserId);
        if (userId == null) return Result.fail("未登录");

        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSelection::getStudentId, userId)
                .eq(CourseSelection::getSelectionStatus, 1)
                .orderByAsc(CourseSelection::getSelectionTime);
        List<CourseSelection> selections = selectionMapper.selectList(queryWrapper);
        fillCourses(selections);

        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        List<Course> requiredCourses = listRequiredCourses(userId);
        for (Course c : requiredCourses) {
            if (c == null) continue;
            String time = c.getClassTime() == null ? "" : c.getClassTime().trim();
            String location = c.getClassLocation() == null ? "" : c.getClassLocation().trim();
            String key = time + "@@" + location;

            Map<String, Object> item = grouped.get(key);
            if (item == null) {
                item = new LinkedHashMap<>();
                item.put("classTime", time);
                item.put("classLocation", location);
                item.put("courses", new ArrayList<Course>());
                grouped.put(key, item);
            }
            @SuppressWarnings("unchecked")
            List<Course> list = (List<Course>) item.get("courses");
            list.add(c);
        }
        if (selections != null) {
            for (CourseSelection s : selections) {
                Course c = s.getCourse();
                if (c == null) continue;
                String time = c.getClassTime() == null ? "" : c.getClassTime().trim();
                String location = c.getClassLocation() == null ? "" : c.getClassLocation().trim();
                String key = time + "@@" + location;

                Map<String, Object> item = grouped.get(key);
                if (item == null) {
                    item = new LinkedHashMap<>();
                    item.put("classTime", time);
                    item.put("classLocation", location);
                    item.put("courses", new ArrayList<Course>());
                    grouped.put(key, item);
                }
                @SuppressWarnings("unchecked")
                List<Course> list = (List<Course>) item.get("courses");
                list.add(c);
            }
        }

        return Result.success(new ArrayList<>(grouped.values()));
    }

    @GetMapping("/teacher/courses")
    public Result<List<Course>> teacherCourses(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return Result.fail("未登录");
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getTeacherId, userId).orderByDesc(Course::getCreateTime);
        return Result.success(courseMapper.selectList(queryWrapper));
    }

    @GetMapping("/teacher/course/students")
    public Result<List<User>> teacherCourseStudents(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam Long courseId) {
        if (userId == null) return Result.fail("未登录");

        LambdaQueryWrapper<Course> courseQuery = new LambdaQueryWrapper<>();
        courseQuery.eq(Course::getId, courseId).eq(Course::getTeacherId, userId);
        if (courseMapper.selectCount(courseQuery) == 0) {
            return Result.fail("无权限查看该课程学生名单");
        }

        LambdaQueryWrapper<CourseSelection> selectionQuery = new LambdaQueryWrapper<>();
        selectionQuery.eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getSelectionStatus, 1)
                .orderByDesc(CourseSelection::getSelectionTime);
        List<CourseSelection> selections = selectionMapper.selectList(selectionQuery);
        List<Long> studentIds = new ArrayList<>();
        if (selections != null) {
            for (CourseSelection s : selections) {
                if (s.getStudentId() != null) studentIds.add(s.getStudentId());
            }
        }
        if (studentIds.isEmpty()) return Result.success(new ArrayList<>());

        List<User> users = userMapper.selectBatchIds(studentIds);
        if (users == null) users = new ArrayList<>();
        for (User u : users) {
            u.setPassword(null);
        }
        return Result.success(users);
    }

    /**
     * 查询所有已上架的课程
     */
    @GetMapping("/list")
    public Result<List<Course>> list() {
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getCourseStatus, 1)
                .and(w -> w.eq(Course::getCourseType, 2).or().isNull(Course::getCourseType));
        List<Course> courses = courseMapper.selectList(queryWrapper);
        fillTeacherNames(courses);
        return Result.success(courses);
    }

    // --- 管理员功能 ---

    /**
     * 新增课程
     */
    @PostMapping("/admin/save")
    public Result<String> saveCourse(@RequestBody Course course) {
        // 1. 校验必填项
        if (course.getCourseNo() == null || course.getCourseNo().trim().isEmpty()) {
            return Result.fail("课程编号不能为空");
        }
        if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
            return Result.fail("课程名称不能为空");
        }
        if (course.getTeacherId() == null) {
            return Result.fail("授课教师不能为空");
        }
        User teacher = userMapper.selectById(course.getTeacherId());
        if (teacher == null || teacher.getRoleType() == null || teacher.getRoleType() != 2) {
            return Result.fail("授课教师不存在或不是教师角色");
        }
        if (teacher.getStatus() != null && teacher.getStatus() == 0) {
            return Result.fail("授课教师账号已禁用");
        }
        course.setCourseNo(course.getCourseNo().trim());
        course.setCourseName(course.getCourseName().trim());

        // 2. 校验课程编号是否重复
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getCourseNo, course.getCourseNo());
        if (courseMapper.selectCount(queryWrapper) > 0) {
            return Result.fail("课程编号 [" + course.getCourseNo() + "] 已存在，请勿重复添加");
        }

        course.setId(null);
        course.setCreateTime(new Date());
        course.setSelectedCount(0); // 初始已选人数为 0
        if (course.getCourseStatus() == null) {
            course.setCourseStatus(1); // 默认上架
        }
        if (course.getCourseType() == null) {
            course.setCourseType(2);
        }

        try {
            courseMapper.insert(course);
            return Result.success("课程新增成功");
        } catch (Exception e) {
            if (isDuplicateKey(e)) {
                return Result.fail("课程编号 [" + course.getCourseNo() + "] 已存在，请勿重复添加");
            }
            return Result.fail("课程新增失败");
        }
    }

    /**
     * 修改课程
     */
    @PostMapping("/admin/update")
    public Result<String> updateCourse(@RequestBody Course course) {
        if (course.getId() == null && course.getCourseNo() != null && !course.getCourseNo().trim().isEmpty()) {
            LambdaQueryWrapper<Course> q = new LambdaQueryWrapper<>();
            q.eq(Course::getCourseNo, course.getCourseNo().trim());
            Course found = courseMapper.selectOne(q);
            if (found != null) course.setId(found.getId());
        }
        if (course.getId() == null) return Result.fail("课程ID不能为空");
        
        // 1. 校验课程是否存在
        Course oldCourse = courseMapper.selectById(course.getId());
        if (oldCourse == null) {
            return Result.fail("课程不存在");
        }

        if (course.getTeacherId() != null && (oldCourse.getTeacherId() == null || !course.getTeacherId().equals(oldCourse.getTeacherId()))) {
            User teacher = userMapper.selectById(course.getTeacherId());
            if (teacher == null || teacher.getRoleType() == null || teacher.getRoleType() != 2) {
                return Result.fail("授课教师不存在或不是教师角色");
            }
            if (teacher.getStatus() != null && teacher.getStatus() == 0) {
                return Result.fail("授课教师账号已禁用");
            }
        }
        
        // 2. 如果修改了课程编号，校验新编号是否已存在
        if (course.getCourseNo() != null && !course.getCourseNo().equals(oldCourse.getCourseNo())) {
            LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Course::getCourseNo, course.getCourseNo());
            if (courseMapper.selectCount(queryWrapper) > 0) {
                return Result.fail("课程编号 [" + course.getCourseNo() + "] 已被其他课程占用");
            }
        }
        
        course.setUpdateTime(new Date());
        try {
            courseMapper.updateById(course);
            return Result.success("课程修改成功");
        } catch (Exception e) {
            if (isDuplicateKey(e)) {
                return Result.fail("课程编号 [" + course.getCourseNo() + "] 已被其他课程占用");
            }
            return Result.fail("课程修改失败");
        }
    }

    /**
     * 删除课程（物理删除或逻辑下架，这里建议下架）
     */
    @PostMapping("/admin/delete")
    public Result<String> deleteCourse(@RequestParam Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            return Result.fail("课程不存在");
        }
        course.setCourseStatus(0); // 0 表示下架
        course.setUpdateTime(new Date());
        courseMapper.updateById(course);
        return Result.success("课程已成功下架");
    }

    @PostMapping("/admin/status")
    public Result<String> courseStatus(@RequestParam Long id, @RequestParam Integer status) {
        Course course = courseMapper.selectById(id);
        if (course == null) return Result.fail("课程不存在");
        if (status == null || (status != 0 && status != 1)) return Result.fail("状态必须是0或1");
        course.setCourseStatus(status);
        course.setUpdateTime(new Date());
        courseMapper.updateById(course);
        return Result.success(status == 1 ? "已上架" : "已下架");
    }

    @PostMapping("/admin/remove")
    public Result<String> removeCourse(@RequestParam Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) return Result.fail("课程不存在");

        LambdaQueryWrapper<CourseSelection> selectionQ = new LambdaQueryWrapper<>();
        selectionQ.eq(CourseSelection::getCourseId, id);
        selectionMapper.delete(selectionQ);

        courseMapper.deleteById(id);
        return Result.success("删除成功");
    }

    /**
     * 管理员分页/条件查询所有课程
     */
    @GetMapping("/admin/list")
    public Result<List<Course>> adminList(
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String courseNo,
            @RequestParam(required = false) String courseCategory,
            @RequestParam(required = false) Integer status) {
        
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        if (courseName != null && !courseName.isEmpty()) {
            queryWrapper.like(Course::getCourseName, courseName);
        }
        if (courseNo != null && !courseNo.isEmpty()) {
            queryWrapper.eq(Course::getCourseNo, courseNo);
        }
        if (courseCategory != null && !courseCategory.trim().isEmpty()) {
            queryWrapper.eq(Course::getCourseCategory, courseCategory.trim());
        }
        if (status != null) {
            queryWrapper.eq(Course::getCourseStatus, status);
        }
        
        queryWrapper.orderByDesc(Course::getCreateTime);
        List<Course> list = courseMapper.selectList(queryWrapper);
        fillTeacherNames(list);
        return Result.success(list);
    }

    @PostMapping("/admin/assignTeacher")
    public Result<String> assignTeacher(@RequestParam Long courseId, @RequestParam Long teacherId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) return Result.fail("课程不存在");

        User teacher = userMapper.selectById(teacherId);
        if (teacher == null) return Result.fail("教师不存在");
        if (teacher.getRoleType() == null || teacher.getRoleType() != 2) return Result.fail("只能分配教师角色用户");
        if (teacher.getStatus() != null && teacher.getStatus() == 0) return Result.fail("教师账号已禁用");

        course.setTeacherId(teacherId);
        course.setUpdateTime(new Date());
        courseMapper.updateById(course);
        return Result.success("分配成功");
    }

    @PostMapping("/admin/required/assign")
    public Result<String> assignRequiredCourse(@RequestParam Long studentId, @RequestParam Long courseId) {
        if (studentId == null) return Result.fail("学生ID不能为空");
        if (courseId == null) return Result.fail("课程ID不能为空");
        User student = userMapper.selectById(studentId);
        if (student == null || student.getRoleType() == null || student.getRoleType() != 3) return Result.fail("学生不存在");
        if (student.getStatus() != null && student.getStatus() == 0) return Result.fail("学生账号已禁用");
        Course course = courseMapper.selectById(courseId);
        if (course == null) return Result.fail("课程不存在");
        if (course.getCourseType() == null || course.getCourseType() != 1) return Result.fail("只能分配必修课");
        selectionMapper.addRequiredCourse(studentId, courseId);
        return Result.success("分配成功");
    }

    @PostMapping("/admin/required/remove")
    public Result<String> removeRequiredCourse(@RequestParam Long studentId, @RequestParam Long courseId) {
        if (studentId == null) return Result.fail("学生ID不能为空");
        if (courseId == null) return Result.fail("课程ID不能为空");
        selectionMapper.removeRequiredCourse(studentId, courseId);
        return Result.success("移除成功");
    }

    @GetMapping("/admin/category/list")
    public Result<List<CourseCategory>> categoryList(@RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<CourseCategory> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null) queryWrapper.eq(CourseCategory::getStatus, status);
        queryWrapper.orderByDesc(CourseCategory::getUpdateTime);
        return Result.success(courseCategoryMapper.selectList(queryWrapper));
    }

    @PostMapping("/admin/category/save")
    public Result<String> categorySave(@RequestBody CourseCategory category) {
        if (category == null || category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            return Result.fail("分类名称不能为空");
        }
        String name = category.getCategoryName().trim();
        LambdaQueryWrapper<CourseCategory> exists = new LambdaQueryWrapper<>();
        exists.eq(CourseCategory::getCategoryName, name);
        if (courseCategoryMapper.selectCount(exists) > 0) return Result.fail("分类已存在");

        CourseCategory entity = new CourseCategory();
        entity.setCategoryName(name);
        entity.setStatus(category.getStatus() == null ? 1 : category.getStatus());
        Date now = new Date();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        courseCategoryMapper.insert(entity);
        return Result.success("新增成功");
    }

    @PostMapping("/admin/category/update")
    public Result<String> categoryUpdate(@RequestBody CourseCategory category) {
        if (category == null || category.getId() == null) return Result.fail("分类ID不能为空");
        CourseCategory old = courseCategoryMapper.selectById(category.getId());
        if (old == null) return Result.fail("分类不存在");

        if (category.getCategoryName() != null && !category.getCategoryName().trim().isEmpty()) {
            String name = category.getCategoryName().trim();
            if (!name.equals(old.getCategoryName())) {
                LambdaQueryWrapper<CourseCategory> exists = new LambdaQueryWrapper<>();
                exists.eq(CourseCategory::getCategoryName, name);
                if (courseCategoryMapper.selectCount(exists) > 0) return Result.fail("分类名称已存在");

                LambdaUpdateWrapper<Course> updateCourseCategory = new LambdaUpdateWrapper<>();
                updateCourseCategory.eq(Course::getCourseCategory, old.getCategoryName())
                        .set(Course::getCourseCategory, name);
                courseMapper.update(null, updateCourseCategory);

                old.setCategoryName(name);
            }
        }
        if (category.getStatus() != null) old.setStatus(category.getStatus());
        old.setUpdateTime(new Date());
        courseCategoryMapper.updateById(old);
        return Result.success("修改成功");
    }

    @PostMapping("/admin/category/status")
    public Result<String> categoryStatus(@RequestParam Long id, @RequestParam Integer status) {
        CourseCategory category = courseCategoryMapper.selectById(id);
        if (category == null) return Result.fail("分类不存在");
        if (status == null || (status != 0 && status != 1)) return Result.fail("状态必须是0或1");
        category.setStatus(status);
        category.setUpdateTime(new Date());
        courseCategoryMapper.updateById(category);
        return Result.success(status == 1 ? "已启用" : "已禁用");
    }

    @PostMapping("/admin/category/delete")
    public Result<String> categoryDelete(@RequestParam Long id) {
        CourseCategory category = courseCategoryMapper.selectById(id);
        if (category == null) return Result.fail("分类不存在");
        String name = category.getCategoryName();
        if (name != null && !name.trim().isEmpty()) {
            LambdaQueryWrapper<Course> exists = new LambdaQueryWrapper<>();
            exists.eq(Course::getCourseCategory, name.trim());
            if (courseMapper.selectCount(exists) > 0) return Result.fail("该分类已被课程使用，无法删除");
        }
        courseCategoryMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/admin/stats/overview")
    public Result<Map<String, Object>> statsOverview() {
        Map<String, Object> result = new LinkedHashMap<>();

        LambdaQueryWrapper<Course> totalCourseQuery = new LambdaQueryWrapper<>();
        Long totalCourses = (long) courseMapper.selectCount(totalCourseQuery);

        LambdaQueryWrapper<Course> onlineCourseQuery = new LambdaQueryWrapper<>();
        onlineCourseQuery.eq(Course::getCourseStatus, 1);
        Long onlineCourses = (long) courseMapper.selectCount(onlineCourseQuery);

        LambdaQueryWrapper<Course> offlineCourseQuery = new LambdaQueryWrapper<>();
        offlineCourseQuery.eq(Course::getCourseStatus, 0);
        Long offlineCourses = (long) courseMapper.selectCount(offlineCourseQuery);

        List<Course> allCourses = courseMapper.selectList(new LambdaQueryWrapper<>());
        long totalSelected = 0;
        if (allCourses != null) {
            for (Course c : allCourses) {
                if (c != null && c.getSelectedCount() != null) totalSelected += c.getSelectedCount();
            }
        }

        Map<String, Object> evalCountMap = evaluationStatsMapper.countAll();
        Object totalEvalObj = evalCountMap == null ? null : evalCountMap.get("total");

        result.put("totalCourses", totalCourses);
        result.put("onlineCourses", onlineCourses);
        result.put("offlineCourses", offlineCourses);
        result.put("totalSelected", totalSelected);
        result.put("totalEvaluations", totalEvalObj == null ? 0 : totalEvalObj);
        return Result.success(result);
    }

    @GetMapping("/admin/stats/hotCourses")
    public Result<List<Course>> hotCourses(@RequestParam(required = false) Integer top) {
        int limit = top == null || top <= 0 ? 10 : Math.min(top, 100);
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Course::getSelectedCount).last("LIMIT " + limit);
        return Result.success(courseMapper.selectList(queryWrapper));
    }

    @GetMapping("/admin/stats/evaluationAvg")
    public Result<List<Map<String, Object>>> evaluationAvg(@RequestParam(required = false) Integer top) {
        int limit = top == null || top <= 0 ? 10 : Math.min(top, 100);
        List<Map<String, Object>> stats = evaluationStatsMapper.listCourseAvg();
        if (stats == null || stats.isEmpty()) return Result.success(new ArrayList<>());

        List<Long> courseIds = new ArrayList<>();
        for (Map<String, Object> s : stats) {
            Object id = s.get("courseId");
            if (id instanceof Number) courseIds.add(((Number) id).longValue());
            else if (id != null) {
                try {
                    courseIds.add(Long.parseLong(String.valueOf(id)));
                } catch (Exception ignored) {
                }
            }
        }
        Map<Long, Course> courseMap = new LinkedHashMap<>();
        if (!courseIds.isEmpty()) {
            List<Course> courses = courseMapper.selectBatchIds(courseIds);
            if (courses != null) {
                for (Course c : courses) {
                    if (c != null && c.getId() != null) courseMap.put(c.getId(), c);
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> s : stats) {
            if (result.size() >= limit) break;
            Object idObj = s.get("courseId");
            Long courseId = null;
            if (idObj instanceof Number) courseId = ((Number) idObj).longValue();
            else if (idObj != null) {
                try {
                    courseId = Long.parseLong(String.valueOf(idObj));
                } catch (Exception ignored) {
                }
            }
            Course c = courseId == null ? null : courseMap.get(courseId);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("courseId", courseId);
            row.put("courseNo", c == null ? null : c.getCourseNo());
            row.put("courseName", c == null ? null : c.getCourseName());
            row.put("avgScore", s.get("avgScore"));
            row.put("count", s.get("count"));
            result.add(row);
        }
        return Result.success(result);
    }

    private boolean isDuplicateKey(Throwable throwable) {
        Throwable t = throwable;
        while (t != null) {
            if (t instanceof DuplicateKeyException) return true;
            if (t instanceof SQLIntegrityConstraintViolationException) return true;
            String msg = t.getMessage();
            if (msg != null && msg.contains("Duplicate entry")) return true;
            t = t.getCause();
        }
        return false;
    }

    private Long resolveUserId(Long userId, Long headerUserId) {
        return userId != null ? userId : headerUserId;
    }

    private void fillCourses(List<CourseSelection> selections) {
        if (selections == null || selections.isEmpty()) return;
        List<Long> courseIds = new ArrayList<>();
        for (CourseSelection s : selections) {
            if (s != null && s.getCourseId() != null) courseIds.add(s.getCourseId());
        }
        if (courseIds.isEmpty()) return;
        List<Course> courses = courseMapper.selectBatchIds(courseIds);
        Map<Long, Course> courseMap = new LinkedHashMap<>();
        if (courses != null) {
            for (Course c : courses) {
                if (c != null && c.getId() != null) courseMap.put(c.getId(), c);
            }
        }
        fillTeacherNames(courses);
        for (CourseSelection selection : selections) {
            if (selection == null || selection.getCourseId() == null) continue;
            selection.setCourse(courseMap.get(selection.getCourseId()));
        }
    }

    private void fillTeacherNames(List<Course> courses) {
        if (courses == null || courses.isEmpty()) return;
        List<Long> teacherIds = new ArrayList<>();
        for (Course c : courses) {
            if (c != null && c.getTeacherId() != null) teacherIds.add(c.getTeacherId());
        }
        if (teacherIds.isEmpty()) return;
        List<User> teachers = userMapper.selectBatchIds(teacherIds);
        Map<Long, String> nameMap = new LinkedHashMap<>();
        if (teachers != null) {
            for (User u : teachers) {
                if (u != null && u.getId() != null) nameMap.put(u.getId(), u.getRealName());
            }
        }
        for (Course c : courses) {
            if (c != null && c.getTeacherId() != null) c.setTeacherName(nameMap.get(c.getTeacherId()));
        }
    }

    private List<Course> listRequiredCourses(Long studentId) {
        List<Long> courseIds = selectionMapper.listRequiredCourseIds(studentId);
        List<Course> courses = null;
        if (courseIds != null && !courseIds.isEmpty()) {
            courses = courseMapper.selectBatchIds(courseIds);
        }
        if (courses == null || courses.isEmpty()) {
            LambdaQueryWrapper<Course> q = new LambdaQueryWrapper<>();
            q.eq(Course::getCourseStatus, 1).eq(Course::getCourseType, 1).orderByDesc(Course::getUpdateTime);
            courses = courseMapper.selectList(q);
        }
        if (courses == null) return new ArrayList<>();
        fillTeacherNames(courses);
        return courses;
    }
}
