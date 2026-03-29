package com.example.cloudevaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.pojo.Course;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    @Select("SELECT template_id FROM course_evaluation_template WHERE course_id = #{courseId} LIMIT 1")
    Long getEvaluationTemplateIdByCourseId(@Param("courseId") Long courseId);

    @Update("INSERT INTO course_evaluation_template(course_id, template_id, create_time, update_time) " +
            "VALUES(#{courseId}, #{templateId}, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE template_id = VALUES(template_id), update_time = NOW()")
    int upsertEvaluationTemplateForCourse(@Param("courseId") Long courseId, @Param("templateId") Long templateId);

    @Update("DELETE FROM course_evaluation_template WHERE template_id = #{templateId}")
    int deleteCourseTemplateBindingsByTemplateId(@Param("templateId") Long templateId);

    @Select("SELECT c.* FROM course c " +
            "JOIN course_evaluation_template cet ON c.id = cet.course_id " +
            "WHERE cet.template_id = #{templateId} " +
            "ORDER BY cet.update_time DESC")
    List<Course> listCoursesByTemplateId(@Param("templateId") Long templateId);

    @Update("DELETE FROM course_evaluation_template WHERE course_id = #{courseId} AND template_id = #{templateId}")
    int unbindCourseTemplate(@Param("courseId") Long courseId, @Param("templateId") Long templateId);
}
