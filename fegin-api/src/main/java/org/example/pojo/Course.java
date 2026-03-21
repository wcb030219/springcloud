package org.example.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 课程实体类
 */
@Data
@TableName("course")
public class Course implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 课程ID
     */
    private Long id;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 课程编号
     */
    private String courseNo;
    
    /**
     * 授课教师ID
     */
    private Long teacherId;
    
    /**
     * 课程类别
     */
    private String courseCategory;
    
    /**
     * 学分
     */
    private Double credit;
    
    /**
     * 学时
     */
    private Integer classHours;
    
    /**
     * 上课时间
     */
    private String classTime;
    
    /**
     * 上课地点
     */
    private String classLocation;
    
    /**
     * 课程容量
     */
    private Integer capacity;
    
    /**
     * 已选人数
     */
    private Integer selectedCount;
    
    /**
     * 课程状态：0-下架，1-上架
     */
    private Integer courseStatus;
    
    /**
     * 课程描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 授课教师名称
     */
    @TableField(exist = false)
    private String teacherName;
}