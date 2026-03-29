package com.example.cloudcourse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.pojo.CourseSelection;

import java.util.List;

@Mapper
public interface SelectionMapper extends BaseMapper<CourseSelection> {

    @Select("SELECT course_id FROM student_required_course WHERE student_id = #{studentId}")
    List<Long> listRequiredCourseIds(@Param("studentId") Long studentId);

    @Update("INSERT IGNORE INTO student_required_course(student_id, course_id, create_time) VALUES(#{studentId}, #{courseId}, NOW())")
    int addRequiredCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Update("DELETE FROM student_required_course WHERE student_id = #{studentId} AND course_id = #{courseId}")
    int removeRequiredCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
