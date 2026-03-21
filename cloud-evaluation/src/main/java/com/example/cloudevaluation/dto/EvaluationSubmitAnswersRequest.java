package com.example.cloudevaluation.dto;

import lombok.Data;

import java.util.List;

@Data
public class EvaluationSubmitAnswersRequest {

    private Long courseId;

    private Long templateId;

    private Integer isAnonymous;

    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        private Long questionId;
        private String answerContent;
        private Integer answerScore;
    }
}

