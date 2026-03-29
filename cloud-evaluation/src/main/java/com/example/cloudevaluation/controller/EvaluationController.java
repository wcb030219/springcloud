package com.example.cloudevaluation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.cloudevaluation.dto.EvaluationSubmitAnswersRequest;
import com.example.cloudevaluation.mapper.CourseMapper;
import com.example.cloudevaluation.mapper.EvaluationAnswerMapper;
import com.example.cloudevaluation.mapper.EvaluationMapper;
import com.example.cloudevaluation.mapper.EvaluationQuestionMapper;
import com.example.cloudevaluation.mapper.EvaluationTemplateMapper;
import org.example.common.Result;
import org.example.pojo.Course;
import org.example.pojo.EvaluationAnswer;
import org.example.pojo.Evaluation;
import org.example.pojo.EvaluationQuestion;
import org.example.pojo.EvaluationTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationQuestionMapper questionMapper;

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private EvaluationTemplateMapper templateMapper;

    @Autowired
    private EvaluationAnswerMapper answerMapper;

    @Autowired
    private CourseMapper courseMapper;

    /**
     * 获取所有可用的评估指标/题目
     */
    @GetMapping("/questions")
    public Result<List<EvaluationQuestion>> getQuestions(
            @RequestParam(required = false) Long templateId,
            @RequestParam(required = false) Long courseId) {
        if (courseId != null) {
            templateId = courseMapper.getEvaluationTemplateIdByCourseId(courseId);
            if (templateId == null) return Result.fail("该课程未配置评估模板");
        }
        LambdaQueryWrapper<EvaluationQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EvaluationQuestion::getStatus, 1) // 启用状态
                    .orderByAsc(EvaluationQuestion::getQuestionOrder);
        if (templateId != null) queryWrapper.eq(EvaluationQuestion::getTemplateId, templateId);
        
        List<EvaluationQuestion> questions = questionMapper.selectList(queryWrapper);
        return Result.success(questions);
    }

    @GetMapping("/templates")
    public Result<List<EvaluationTemplate>> templates() {
        LambdaQueryWrapper<EvaluationTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EvaluationTemplate::getStatus, 1).orderByDesc(EvaluationTemplate::getUpdateTime);
        return Result.success(templateMapper.selectList(queryWrapper));
    }

    @GetMapping("/courseTemplate")
    public Result<EvaluationTemplate> courseTemplate(@RequestParam Long courseId) {
        if (courseId == null) return Result.fail("课程ID不能为空");
        Long templateId = courseMapper.getEvaluationTemplateIdByCourseId(courseId);
        if (templateId == null) return Result.success(null);
        return Result.success(templateMapper.selectById(templateId));
    }

    /**
     * 学生提交匿名教学评估
     */
    @PostMapping("/submit")
    public Result<String> submitEvaluation(
            @RequestBody Evaluation evaluation,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        if (userId == null) userId = headerUserId;
        if (userId == null) return Result.fail("未登录");
        // 1. 校验参数
        if (evaluation.getCourseId() == null || evaluation.getEvaluationScore() == null) {
            return Result.fail("课程ID和评分不能为空");
        }

        // 2. 检查是否已经评估过（防止重复评估）
        LambdaQueryWrapper<Evaluation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Evaluation::getCourseId, evaluation.getCourseId())
                    .eq(Evaluation::getStudentId, userId);
        Integer count = evaluationMapper.selectCount(queryWrapper);
        if (count > 0) {
            return Result.fail("您已经对该课程提交过评估，请勿重复提交");
        }

        // 3. 填充数据并保存
        evaluation.setStudentId(userId);
        evaluation.setEvaluationTime(new Date());
        evaluation.setCreateTime(new Date());
        evaluation.setIsAnonymous(1); // 默认匿名
        
        evaluationMapper.insert(evaluation);
        return Result.success("评估提交成功，感谢您的反馈！");
    }

    @PostMapping("/submitAnswers")
    public Result<String> submitAnswers(
            @RequestBody EvaluationSubmitAnswersRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return Result.fail("未登录");
        if (request == null || request.getCourseId() == null) return Result.fail("课程ID不能为空");
        if (request.getAnswers() == null || request.getAnswers().isEmpty()) return Result.fail("答案不能为空");

        Long templateId = courseMapper.getEvaluationTemplateIdByCourseId(request.getCourseId());
        if (templateId == null) templateId = request.getTemplateId();
        if (templateId == null) return Result.fail("该课程未配置评估模板");

        LambdaQueryWrapper<Evaluation> exists = new LambdaQueryWrapper<>();
        exists.eq(Evaluation::getCourseId, request.getCourseId()).eq(Evaluation::getStudentId, userId);
        if (evaluationMapper.selectCount(exists) > 0) {
            return Result.fail("您已经对该课程提交过评估，请勿重复提交");
        }

        Date now = new Date();

        int scoreCount = 0;
        int scoreSum = 0;
        StringBuilder contentBuilder = new StringBuilder();

        for (EvaluationSubmitAnswersRequest.AnswerItem item : request.getAnswers()) {
            if (item == null || item.getQuestionId() == null) return Result.fail("题目ID不能为空");
            EvaluationQuestion q = questionMapper.selectById(item.getQuestionId());
            if (q == null || q.getStatus() == null || q.getStatus() != 1) return Result.fail("题目不存在或已禁用");
            if (q.getTemplateId() == null || !q.getTemplateId().equals(templateId)) return Result.fail("题目不属于当前课程模板");

            if (item.getAnswerScore() != null) {
                int v = item.getAnswerScore();
                if (v < 0 || v > 5) return Result.fail("评分必须在0~5之间");
                scoreSum += v;
                scoreCount++;
            }

            if (item.getAnswerContent() != null && !item.getAnswerContent().trim().isEmpty()) {
                if (contentBuilder.length() > 0) contentBuilder.append(" | ");
                String c = item.getAnswerContent().trim();
                if (c.length() > 50) c = c.substring(0, 50);
                contentBuilder.append(c);
            }
        }

        double avg = scoreCount == 0 ? 0.0 : (double) scoreSum / scoreCount;

        Evaluation evaluation = new Evaluation();
        evaluation.setCourseId(request.getCourseId());
        evaluation.setStudentId(userId);
        evaluation.setIsAnonymous(request.getIsAnonymous() == null ? 1 : request.getIsAnonymous());
        evaluation.setEvaluationScore(avg);
        evaluation.setEvaluationContent(contentBuilder.length() == 0 ? null : contentBuilder.toString());
        evaluation.setEvaluationTime(now);
        evaluation.setCreateTime(now);
        evaluation.setUpdateTime(now);
        evaluationMapper.insert(evaluation);

        Long evaluationId = evaluation.getId();
        for (EvaluationSubmitAnswersRequest.AnswerItem item : request.getAnswers()) {
            EvaluationAnswer answer = new EvaluationAnswer();
            answer.setEvaluationId(evaluationId);
            answer.setQuestionId(item.getQuestionId());
            answer.setAnswerContent(item.getAnswerContent());
            answer.setAnswerScore(item.getAnswerScore());
            answer.setCreateTime(now);
            answerMapper.insert(answer);
        }

        return Result.success("评估提交成功，感谢您的反馈！");
    }

    @GetMapping("/list")
    public Result<List<Evaluation>> teacherEvaluationList(
            @RequestParam Long courseId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return Result.fail("未登录");

        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getTeacherId() == null || !course.getTeacherId().equals(userId)) {
            return Result.fail("无权限查看该课程评教");
        }

        LambdaQueryWrapper<Evaluation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Evaluation::getCourseId, courseId).orderByDesc(Evaluation::getEvaluationTime);
        List<Evaluation> list = evaluationMapper.selectList(queryWrapper);
        if (list == null) list = new ArrayList<>();
        for (Evaluation e : list) {
            if (e.getIsAnonymous() != null && e.getIsAnonymous() == 1) {
                e.setStudentId(null);
                e.setStudentName("匿名");
            }
        }
        return Result.success(list);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> teacherEvaluationStats(
            @RequestParam Long courseId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return Result.fail("未登录");

        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getTeacherId() == null || !course.getTeacherId().equals(userId)) {
            return Result.fail("无权限查看该课程评教统计");
        }

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
