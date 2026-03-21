package org.example.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 选课实体类
 */
@Data
public class CourseSelection implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 选课ID
     */
    private Long id;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 选课时间
     */
    private Date selectionTime;
    
    /**
     * 选课状态：0-已退课，1-已选课
     */
    private Integer selectionStatus;
    
    /**
     * 退课时间
     */
    private Date dropTime;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 学生姓名
     */
    private String studentName;
    
    /**
     * 课程信息
     */
    private Course course;
}