package com.example.cloudcourse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface EvaluationStatsMapper {

    @Select("SELECT course_id AS courseId, AVG(evaluation_score) AS avgScore, COUNT(*) AS count FROM evaluation GROUP BY course_id")
    List<Map<String, Object>> listCourseAvg();

    @Select("SELECT COUNT(*) AS total FROM evaluation")
    Map<String, Object> countAll();
}

