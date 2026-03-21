package org.example.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评估实体类
 */
@Data
public class Evaluation implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 评估ID
     */
    private Long id;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 评估内容
     */
    private String evaluationContent;
    
    /**
     * 评估分数
     */
    private Double evaluationScore;
    
    /**
     * 是否匿名：0-否，1-是
     */
    private Integer isAnonymous;
    
    /**
     * 评估时间
     */
    private Date evaluationTime;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 课程信息
     */
    private Course course;
    
    /**
     * 学生姓名
     */
    private String studentName;
}