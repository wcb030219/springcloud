package org.example.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("evaluation_answer")
public class EvaluationAnswer implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long evaluationId;

    private Long questionId;

    private String answerContent;

    private Integer answerScore;

    private Date createTime;
}

