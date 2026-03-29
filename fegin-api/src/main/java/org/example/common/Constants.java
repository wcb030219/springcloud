package org.example.common;

/**
 * 常量定义类
 */
public class Constants {
    
    /**
     * 角色类型
     */
    public static class RoleType {
        public static final int ADMIN = 1; // 管理员
        public static final int TEACHER = 2; // 教师
        public static final int STUDENT = 3; // 学生
    }
    
    /**
     * 用户状态
     */
    public static class UserStatus {
        public static final int DISABLED = 0; // 禁用
        public static final int ENABLED = 1; // 启用
    }
    
    /**
     * 课程状态
     */
    public static class CourseStatus {
        public static final int OFFLINE = 0; // 下架
        public static final int ONLINE = 1; // 上架
    }
    
    /**
     * 选课状态
     */
    public static class SelectionStatus {
        public static final int DROPPED = 0; // 已退课
        public static final int SELECTED = 1; // 已选课
    }
    
    /**
     * 评估状态
     */
    public static class EvaluationStatus {
        public static final int NOT_EVALUATED = 0; // 未评估
        public static final int EVALUATED = 1; // 已评估
    }
    
    /**
     * 成绩状态
     */
    public static class GradeStatus {
        public static final int DELETED = 0; // 已删除
        public static final int NORMAL = 1; // 正常
    }
    
    /**
     * 评估模板类型
     */
    public static class TemplateType {
        public static final int COMMON = 1; // 通用
        public static final int PROFESSIONAL = 2; // 专业
    }
    
    /**
     * 评估题目类型
     */
    public static class QuestionType {
        public static final int SINGLE_CHOICE = 1; // 单选
        public static final int MULTIPLE_CHOICE = 2; // 多选
        public static final int TEXT = 3; // 文本
    }
    
    /**
     * 系统常量
     */
    public static class System {
        public static final String DEFAULT_PASSWORD = "030219"; // 默认密码
        public static final int PAGE_SIZE = 10; // 默认分页大小
        public static final int MAX_PAGE_SIZE = 100; // 最大分页大小
    }
    
    /**
     * 响应状态码
     */
    public static class StatusCode {
        public static final int SUCCESS = 200; // 成功
        public static final int FAIL = 500; // 失败
        public static final int UNAUTHORIZED = 401; // 未授权
        public static final int FORBIDDEN = 403; // 禁止访问
        public static final int NOT_FOUND = 404; // 资源不存在
    }
}