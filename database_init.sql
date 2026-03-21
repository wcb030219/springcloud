-- 学生选课和教学评估系统数据库设计
-- 创建数据库
CREATE DATABASE IF NOT EXISTS course_selection_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE course_selection_system;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `role_type` TINYINT(1) NOT NULL COMMENT '角色类型：1-管理员，2-教师，3-学生',
    `student_no` VARCHAR(20) DEFAULT NULL COMMENT '学号/工号',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系方式',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_student_no` (`student_no`),
    KEY `idx_role_type` (`role_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 课程表
CREATE TABLE IF NOT EXISTS `course` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '课程ID',
    `course_name` VARCHAR(100) NOT NULL COMMENT '课程名称',
    `course_no` VARCHAR(50) NOT NULL COMMENT '课程编号',
    `teacher_id` BIGINT(20) NOT NULL COMMENT '授课教师ID',
    `course_category` VARCHAR(50) DEFAULT NULL COMMENT '课程类别',
    `credit` DECIMAL(3,1) DEFAULT 0.0 COMMENT '学分',
    `class_hours` INT(11) DEFAULT 0 COMMENT '学时',
    `class_time` VARCHAR(100) DEFAULT NULL COMMENT '上课时间',
    `class_location` VARCHAR(100) DEFAULT NULL COMMENT '上课地点',
    `capacity` INT(11) DEFAULT 0 COMMENT '课程容量',
    `selected_count` INT(11) DEFAULT 0 COMMENT '已选人数',
    `course_status` TINYINT(1) DEFAULT 1 COMMENT '课程状态：0-下架，1-上架',
    `description` TEXT DEFAULT NULL COMMENT '课程描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_course_no` (`course_no`),
    KEY `idx_teacher_id` (`teacher_id`),
    KEY `idx_course_category` (`course_category`),
    KEY `idx_course_status` (`course_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 课程分类表
CREATE TABLE IF NOT EXISTS `course_category` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_name` (`category_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程分类表';

-- 选课表
CREATE TABLE IF NOT EXISTS `course_selection` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '选课ID',
    `student_id` BIGINT(20) NOT NULL COMMENT '学生ID',
    `course_id` BIGINT(20) NOT NULL COMMENT '课程ID',
    `selection_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    `selection_status` TINYINT(1) DEFAULT 1 COMMENT '选课状态：0-已退课，1-已选课',
    `drop_time` DATETIME DEFAULT NULL COMMENT '退课时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_course` (`student_id`, `course_id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_selection_status` (`selection_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课表';

-- 评估表
CREATE TABLE IF NOT EXISTS `evaluation` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '评估ID',
    `course_id` BIGINT(20) NOT NULL COMMENT '课程ID',
    `student_id` BIGINT(20) NOT NULL COMMENT '学生ID',
    `evaluation_content` TEXT DEFAULT NULL COMMENT '评估内容',
    `evaluation_score` DECIMAL(3,1) DEFAULT 0.0 COMMENT '评估分数',
    `is_anonymous` TINYINT(1) DEFAULT 1 COMMENT '是否匿名：0-否，1-是',
    `evaluation_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评估时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_course_evaluation` (`student_id`, `course_id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_is_anonymous` (`is_anonymous`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估表';

-- 成绩表
CREATE TABLE IF NOT EXISTS `grade` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '成绩ID',
    `student_id` BIGINT(20) NOT NULL COMMENT '学生ID',
    `course_id` BIGINT(20) NOT NULL COMMENT '课程ID',
    `grade_score` DECIMAL(5,2) DEFAULT 0.00 COMMENT '成绩',
    `teacher_id` BIGINT(20) NOT NULL COMMENT '录入教师ID',
    `grade_status` TINYINT(1) DEFAULT 1 COMMENT '成绩状态：0-已删除，1-正常',
    `remarks` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_course_grade` (`student_id`, `course_id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_teacher_id` (`teacher_id`),
    KEY `idx_grade_status` (`grade_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';

-- 评估模板表
CREATE TABLE IF NOT EXISTS `evaluation_template` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `template_type` TINYINT(1) DEFAULT 1 COMMENT '模板类型：1-通用，2-专业',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_template_type` (`template_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估模板表';

-- 评估题目表
CREATE TABLE IF NOT EXISTS `evaluation_question` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '题目ID',
    `template_id` BIGINT(20) NOT NULL COMMENT '模板ID',
    `question_content` VARCHAR(500) NOT NULL COMMENT '题目内容',
    `question_type` TINYINT(1) DEFAULT 1 COMMENT '题目类型：1-单选，2-多选，3-文本',
    `question_order` INT(11) DEFAULT 0 COMMENT '题目顺序',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_template_id` (`template_id`),
    KEY `idx_question_order` (`question_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估题目表';

-- 评估答案表
CREATE TABLE IF NOT EXISTS `evaluation_answer` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '答案ID',
    `evaluation_id` BIGINT(20) NOT NULL COMMENT '评估ID',
    `question_id` BIGINT(20) NOT NULL COMMENT '题目ID',
    `answer_content` TEXT DEFAULT NULL COMMENT '答案内容',
    `answer_score` INT(11) DEFAULT 0 COMMENT '答案分数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_evaluation_id` (`evaluation_id`),
    KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估答案表';

-- 插入初始管理员数据
INSERT INTO `user` (`username`, `password`, `real_name`, `role_type`, `student_no`, `phone`, `email`, `status`) VALUES
('admin', '030219', '系统管理员', 1, 'ADMIN001', '13800138000', 'admin@example.com', 1);

-- 插入初始教师数据
INSERT INTO `user` (`username`, `password`, `real_name`, `role_type`, `student_no`, `phone`, `email`, `status`) VALUES
('teacher1', '030219', '张老师', 2, 'T2023001', '13800138001', 'teacher1@example.com', 1),
('teacher2', '030219', '李老师', 2, 'T2023002', '13800138002', 'teacher2@example.com', 1);

-- 插入初始学生数据
INSERT INTO `user` (`username`, `password`, `real_name`, `role_type`, `student_no`, `phone`, `email`, `status`) VALUES
('student1', '030219', '王小明', 3, 'S2023001', '13800138003', 'student1@example.com', 1),
('student2', '030219', '李小红', 3, 'S2023002', '13800138004', 'student2@example.com', 1),
('student3', '030219', '张伟', 3, 'S2023003', '13800138005', 'student3@example.com', 1);

UPDATE `user` SET `password` = '030219';

-- 插入初始课程数据
INSERT INTO `course` (`course_name`, `course_no`, `teacher_id`, `course_category`, `credit`, `class_hours`, `class_time`, `class_location`, `capacity`, `selected_count`, `course_status`, `description`) VALUES
('Java程序设计', 'CS101', 2, '计算机科学', 3.0, 48, '周一 1-2节', '教学楼A101', 50, 0, 1, 'Java编程基础课程'),
('数据库原理', 'CS102', 2, '计算机科学', 3.0, 48, '周二 3-4节', '教学楼A102', 40, 0, 1, '数据库系统原理与应用'),
('数据结构', 'CS103', 2, '计算机科学', 4.0, 64, '周三 1-2节', '教学楼A103', 45, 0, 1, '数据结构与算法'),
('计算机网络', 'CS104', 3, '计算机科学', 3.0, 48, '周四 3-4节', '教学楼A104', 50, 0, 1, '计算机网络基础'),
('操作系统', 'CS105', 3, '计算机科学', 4.0, 64, '周五 1-2节', '教学楼A105', 40, 0, 1, '操作系统原理');

INSERT IGNORE INTO `course_category` (`category_name`, `status`) VALUES
('计算机科学', 1);

-- 插入初始评估模板数据
INSERT INTO `evaluation_template` (`template_name`, `template_type`, `description`, `status`) VALUES
('通用教学评估模板', 1, '适用于所有课程的教学评估', 1),
('专业课程评估模板', 2, '适用于专业课程的深度评估', 1);

-- 插入初始评估题目数据
INSERT INTO `evaluation_question` (`template_id`, `question_content`, `question_type`, `question_order`, `status`) VALUES
(1, '课程内容是否充实？', 1, 1, 1),
(1, '教学方法是否得当？', 1, 2, 1),
(1, '教师讲解是否清晰？', 1, 3, 1),
(1, '课程难度是否适中？', 1, 4, 1),
(1, '请对本课程提出建议：', 3, 5, 1);
