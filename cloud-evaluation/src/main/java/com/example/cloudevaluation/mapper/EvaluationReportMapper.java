package com.example.cloudevaluation.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface EvaluationReportMapper {

    @Select("SELECT course_id AS courseId, AVG(evaluation_score) AS avgScore, COUNT(*) AS count FROM evaluation GROUP BY course_id ORDER BY avgScore DESC")
    List<Map<String, Object>> courseAvgDesc();
}

