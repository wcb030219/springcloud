package org.example.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评估题目实体类
 */
@Data
@TableName("evaluation_question")
public class EvaluationQuestion implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 题目ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 模板ID
     */
    private Long templateId;
    
    /**
     * 题目内容
     */
    private String questionContent;
    
    /**
     * 题目类型：1-单选，2-多选，3-文本
     */
    private Integer questionType;
    
    /**
     * 题目顺序
     */
    private Integer questionOrder;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}
