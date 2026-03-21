package org.example.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("evaluation_template")
public class EvaluationTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String templateName;

    private Integer templateType;

    private String description;

    private Integer status;

    private Date createTime;

    private Date updateTime;
}

