package com.example.cloudevaluation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.cloudevaluation.mapper.CourseMapper;
import com.example.cloudevaluation.mapper.GradeMapper;
import org.example.common.Result;
import org.example.pojo.Course;
import org.example.pojo.Grade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/v1/grade")
public class GradeController {

    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private CourseMapper courseMapper;

    /**
     * 查询个人成绩（学生专用）
     */
    @GetMapping("/my")
    public Result<List<Grade>> getMyGrades(
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        if (userId == null) userId = headerUserId;
        if (userId == null) return Result.fail("未登录");
        // 直接按 userId (即 studentId) 查询成绩表
        LambdaQueryWrapper<Grade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Grade::getStudentId, userId)
                    .eq(Grade::getGradeStatus, 1) // 1-正常状态的成绩
                    .orderByDesc(Grade::getCreateTime);
        
        List<Grade> grades = gradeMapper.selectList(queryWrapper);
        return Result.success(grades);
    }

    /**
     * 教师录入/修改成绩
     */
    @PostMapping("/submit")
    public Result<String> submitGrade(
            @RequestBody Grade grade,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        if (userId == null) userId = headerUserId;
        if (userId == null) return Result.fail("未登录");
        // 1. 校验参数
        if (grade.getStudentId() == null || grade.getCourseId() == null || grade.getGradeScore() == null) {
            return Result.fail("学生ID、课程ID和成绩不能为空");
        }

        // 2. 检查是否已经录入过成绩
        LambdaQueryWrapper<Grade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Grade::getStudentId, grade.getStudentId())
                    .eq(Grade::getCourseId, grade.getCourseId());
        Grade existingGrade = gradeMapper.selectOne(queryWrapper);

        if (existingGrade != null) {
            // 3. 如果已存在，则执行更新
            existingGrade.setGradeScore(grade.getGradeScore());
            existingGrade.setTeacherId(userId); // 更新最后录入的教师
            existingGrade.setUpdateTime(new Date());
            existingGrade.setRemarks(grade.getRemarks());
            gradeMapper.updateById(existingGrade);
            return Result.success("成绩更新成功");
        } else {
            // 4. 如果不存在，则执行插入
            grade.setTeacherId(userId);
            grade.setGradeStatus(1); // 1-正常
            grade.setCreateTime(new Date());
            gradeMapper.insert(grade);
            return Result.success("成绩录入成功");
        }
    }

    @GetMapping("/list")
    public Result<List<Grade>> listGrades(
            @RequestParam(required = false) Long courseId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return Result.fail("未登录");

        LambdaQueryWrapper<Grade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Grade::getTeacherId, userId)
                .eq(Grade::getGradeStatus, 1)
                .orderByDesc(Grade::getUpdateTime);
        if (courseId != null) queryWrapper.eq(Grade::getCourseId, courseId);
        return Result.success(gradeMapper.selectList(queryWrapper));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportGrades(
            @RequestParam Long courseId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(401).body(new byte[0]);
        }

        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getTeacherId() == null || !course.getTeacherId().equals(userId)) {
            return ResponseEntity.status(403).body(new byte[0]);
        }

        LambdaQueryWrapper<Grade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Grade::getTeacherId, userId)
                .eq(Grade::getCourseId, courseId)
                .eq(Grade::getGradeStatus, 1)
                .orderByDesc(Grade::getUpdateTime);
        List<Grade> list = gradeMapper.selectList(queryWrapper);

        StringBuilder sb = new StringBuilder();
        sb.append("studentId,courseId,gradeScore,remarks\n");
        if (list != null) {
            for (Grade g : list) {
                sb.append(g.getStudentId() == null ? "" : g.getStudentId()).append(",");
                sb.append(g.getCourseId() == null ? "" : g.getCourseId()).append(",");
                sb.append(g.getGradeScore() == null ? "" : g.getGradeScore()).append(",");
                String remarks = g.getRemarks() == null ? "" : g.getRemarks().replace("\n", " ").replace("\r", " ");
                sb.append(remarks).append("\n");
            }
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        String filename = "grades-course-" + courseId + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=utf-8"))
                .body(bytes);
    }

    @PostMapping("/import")
    public Result<String> importGrades(
            @RequestParam Long courseId,
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return Result.fail("未登录");
        if (file == null || file.isEmpty()) return Result.fail("文件不能为空");

        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getTeacherId() == null || !course.getTeacherId().equals(userId)) {
            return Result.fail("无权限导入该课程成绩");
        }

        int success = 0;
        int failed = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                if (first) {
                    first = false;
                    if (trimmed.toLowerCase().contains("studentid")) continue;
                }
                String[] parts = trimmed.split(",", -1);
                if (parts.length < 2) {
                    failed++;
                    continue;
                }
                Long studentId;
                Double score;
                try {
                    studentId = Long.parseLong(parts[0].trim());
                    score = Double.parseDouble(parts[1].trim());
                } catch (Exception e) {
                    failed++;
                    continue;
                }
                String remarks = parts.length >= 3 ? parts[2].trim() : null;

                Grade grade = new Grade();
                grade.setStudentId(studentId);
                grade.setCourseId(courseId);
                grade.setGradeScore(score);
                grade.setRemarks(remarks);

                Result<String> r = submitGrade(grade, userId, userId);
                if (r != null && r.getCode() == 200) {
                    success++;
                } else {
                    failed++;
                }
            }
        } catch (Exception e) {
            return Result.fail("导入失败");
        }

        return Result.success("导入完成：成功 " + success + " 条，失败 " + failed + " 条");
    }
}
