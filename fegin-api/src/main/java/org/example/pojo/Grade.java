package org.example.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 成绩实体类
 */
@Data
@TableName("grade")
public class Grade implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 成绩ID
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
     * 成绩
     */
    private Double gradeScore;
    
    /**
     * 录入教师ID
     */
    private Long teacherId;
    
    /**
     * 成绩状态：0-已删除，1-正常
     */
    private Integer gradeStatus;
    
    /**
     * 备注
     */
    private String remarks;
    
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
    @TableField(exist = false)
    private String studentName;
    
    /**
     * 课程信息
     */
    @TableField(exist = false)
    private Course course;
    
    /**
     * 录入教师姓名
     */
    @TableField(exist = false)
    private String teacherName;
}