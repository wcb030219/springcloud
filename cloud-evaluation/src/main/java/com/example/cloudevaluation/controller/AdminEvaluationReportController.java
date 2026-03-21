package com.example.cloudevaluation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.cloudevaluation.mapper.EvaluationMapper;
import com.example.cloudevaluation.mapper.EvaluationReportMapper;
import org.example.common.Result;
import org.example.pojo.Evaluation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin/report")
public class AdminEvaluationReportController {

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private EvaluationReportMapper reportMapper;

    @GetMapping("/courseAvg")
    public Result<List<Map<String, Object>>> courseAvg(@RequestParam(required = false) Integer top) {
        int limit = top == null || top <= 0 ? 20 : Math.min(top, 200);
        List<Map<String, Object>> list = reportMapper.courseAvgDesc();
        if (list == null) list = new ArrayList<>();
        if (list.size() > limit) {
            return Result.success(list.subList(0, limit));
        }
        return Result.success(list);
    }

    @GetMapping("/courseStats")
    public Result<Map<String, Object>> courseStats(@RequestParam Long courseId) {
        LambdaQueryWrapper<Evaluation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Evaluation::getCourseId, courseId);
        List<Evaluation> list = evaluationMapper.selectList(queryWrapper);

        int count = 0;
        double sum = 0.0;
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        int bin1 = 0, bin2 = 0, bin3 = 0, bin4 = 0, bin5 = 0;

        if (list != null) {
            for (Evaluation e : list) {
                if (e.getEvaluationScore() == null) continue;
                double v = e.getEvaluationScore();
                count++;
                sum += v;
                if (v < min) min = v;
                if (v > max) max = v;

                if (v < 1.5) bin1++;
                else if (v < 2.5) bin2++;
                else if (v < 3.5) bin3++;
                else if (v < 4.5) bin4++;
                else bin5++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("courseId", courseId);
        result.put("count", count);
        result.put("avgScore", count == 0 ? 0.0 : (sum / count));
        result.put("minScore", count == 0 ? 0.0 : min);
        result.put("maxScore", count == 0 ? 0.0 : max);

        Map<String, Object> distribution = new LinkedHashMap<>();
        distribution.put("[0,1.5)", bin1);
        distribution.put("[1.5,2.5)", bin2);
        distribution.put("[2.5,3.5)", bin3);
        distribution.put("[3.5,4.5)", bin4);
        distribution.put("[4.5,5]", bin5);
        result.put("distribution", distribution);

        return Result.success(result);
    }
}

