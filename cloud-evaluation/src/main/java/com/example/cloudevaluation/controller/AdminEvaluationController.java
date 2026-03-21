package com.example.cloudevaluation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.cloudevaluation.mapper.EvaluationQuestionMapper;
import com.example.cloudevaluation.mapper.EvaluationTemplateMapper;
import org.example.common.Result;
import org.example.pojo.EvaluationQuestion;
import org.example.pojo.EvaluationTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/v1/admin/evaluation")
public class AdminEvaluationController {

    @Autowired
    private EvaluationTemplateMapper templateMapper;

    @Autowired
    private EvaluationQuestionMapper questionMapper;

    @GetMapping("/template/list")
    public Result<List<EvaluationTemplate>> templateList(@RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<EvaluationTemplate> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null) queryWrapper.eq(EvaluationTemplate::getStatus, status);
        queryWrapper.orderByDesc(EvaluationTemplate::getUpdateTime);
        return Result.success(templateMapper.selectList(queryWrapper));
    }

    @PostMapping("/template/save")
    public Result<String> templateSave(@RequestBody EvaluationTemplate template) {
        if (template == null || template.getTemplateName() == null || template.getTemplateName().trim().isEmpty()) {
            return Result.fail("模板名称不能为空");
        }
        String name = template.getTemplateName().trim();
        LambdaQueryWrapper<EvaluationTemplate> exists = new LambdaQueryWrapper<>();
        exists.eq(EvaluationTemplate::getTemplateName, name);
        if (templateMapper.selectCount(exists) > 0) return Result.fail("模板名称已存在");

        EvaluationTemplate entity = new EvaluationTemplate();
        entity.setTemplateName(name);
        entity.setTemplateType(template.getTemplateType() == null ? 1 : template.getTemplateType());
        entity.setDescription(template.getDescription());
        entity.setStatus(template.getStatus() == null ? 1 : template.getStatus());
        Date now = new Date();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        templateMapper.insert(entity);
        return Result.success("新增成功");
    }

    @PostMapping("/template/update")
    public Result<String> templateUpdate(@RequestBody EvaluationTemplate template) {
        if (template == null || template.getId() == null) return Result.fail("模板ID不能为空");
        EvaluationTemplate old = templateMapper.selectById(template.getId());
        if (old == null) return Result.fail("模板不存在");

        if (template.getTemplateName() != null && !template.getTemplateName().trim().isEmpty()) {
            String name = template.getTemplateName().trim();
            if (!name.equals(old.getTemplateName())) {
                LambdaQueryWrapper<EvaluationTemplate> exists = new LambdaQueryWrapper<>();
                exists.eq(EvaluationTemplate::getTemplateName, name);
                if (templateMapper.selectCount(exists) > 0) return Result.fail("模板名称已存在");
                old.setTemplateName(name);
            }
        }
        if (template.getTemplateType() != null) old.setTemplateType(template.getTemplateType());
        if (template.getDescription() != null) old.setDescription(template.getDescription());
        if (template.getStatus() != null) old.setStatus(template.getStatus());
        old.setUpdateTime(new Date());
        templateMapper.updateById(old);
        return Result.success("修改成功");
    }

    @PostMapping("/template/status")
    public Result<String> templateStatus(@RequestParam Long id, @RequestParam Integer status) {
        EvaluationTemplate template = templateMapper.selectById(id);
        if (template == null) return Result.fail("模板不存在");
        if (status == null || (status != 0 && status != 1)) return Result.fail("状态必须是0或1");
        template.setStatus(status);
        template.setUpdateTime(new Date());
        templateMapper.updateById(template);
        return Result.success(status == 1 ? "已启用" : "已禁用");
    }

    @GetMapping("/question/list")
    public Result<List<EvaluationQuestion>> questionList(@RequestParam Long templateId, @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<EvaluationQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EvaluationQuestion::getTemplateId, templateId);
        if (status != null) queryWrapper.eq(EvaluationQuestion::getStatus, status);
        queryWrapper.orderByAsc(EvaluationQuestion::getQuestionOrder).orderByDesc(EvaluationQuestion::getUpdateTime);
        return Result.success(questionMapper.selectList(queryWrapper));
    }

    @PostMapping("/question/save")
    public Result<String> questionSave(@RequestBody EvaluationQuestion question) {
        if (question == null) return Result.fail("参数不能为空");
        if (question.getTemplateId() == null) return Result.fail("模板ID不能为空");
        if (question.getQuestionContent() == null || question.getQuestionContent().trim().isEmpty()) return Result.fail("题目内容不能为空");
        if (question.getQuestionType() == null) return Result.fail("题目类型不能为空");
        if (question.getQuestionOrder() == null) question.setQuestionOrder(0);
        if (question.getStatus() == null) question.setStatus(1);

        if (templateMapper.selectById(question.getTemplateId()) == null) return Result.fail("模板不存在");

        EvaluationQuestion entity = new EvaluationQuestion();
        entity.setTemplateId(question.getTemplateId());
        entity.setQuestionContent(question.getQuestionContent().trim());
        entity.setQuestionType(question.getQuestionType());
        entity.setQuestionOrder(question.getQuestionOrder());
        entity.setStatus(question.getStatus());
        Date now = new Date();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        questionMapper.insert(entity);
        return Result.success("新增成功");
    }

    @PostMapping("/question/update")
    public Result<String> questionUpdate(@RequestBody EvaluationQuestion question) {
        if (question == null || question.getId() == null) return Result.fail("题目ID不能为空");
        EvaluationQuestion old = questionMapper.selectById(question.getId());
        if (old == null) return Result.fail("题目不存在");

        if (question.getQuestionContent() != null && !question.getQuestionContent().trim().isEmpty()) {
            old.setQuestionContent(question.getQuestionContent().trim());
        }
        if (question.getQuestionType() != null) old.setQuestionType(question.getQuestionType());
        if (question.getQuestionOrder() != null) old.setQuestionOrder(question.getQuestionOrder());
        if (question.getStatus() != null) old.setStatus(question.getStatus());
        old.setUpdateTime(new Date());
        questionMapper.updateById(old);
        return Result.success("修改成功");
    }

    @PostMapping("/question/status")
    public Result<String> questionStatus(@RequestParam Long id, @RequestParam Integer status) {
        EvaluationQuestion q = questionMapper.selectById(id);
        if (q == null) return Result.fail("题目不存在");
        if (status == null || (status != 0 && status != 1)) return Result.fail("状态必须是0或1");
        q.setStatus(status);
        q.setUpdateTime(new Date());
        questionMapper.updateById(q);
        return Result.success(status == 1 ? "已启用" : "已禁用");
    }
}

