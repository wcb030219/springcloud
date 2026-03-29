-- 学生选课和教学评估系统数据库设计
-- 创建数据库
DROP DATABASE IF EXISTS course_selection_system;
CREATE DATABASE course_selection_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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
    `course_type` TINYINT(1) DEFAULT 2 COMMENT '课程类型：1-必修，2-选修',
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
    KEY `idx_course_type` (`course_type`),
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

-- 学生必修课表（每个学生有自己的必修课）
CREATE TABLE IF NOT EXISTS `student_required_course` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `student_id` BIGINT(20) NOT NULL COMMENT '学生ID',
    `course_id` BIGINT(20) NOT NULL COMMENT '课程ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_course` (`student_id`, `course_id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生必修课表';

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
    UNIQUE KEY `uk_template_name` (`template_name`),
    KEY `idx_template_type` (`template_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估模板表';

-- 课程-评估模板绑定表（每门课指定一个模板）
CREATE TABLE IF NOT EXISTS `course_evaluation_template` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '绑定ID',
    `course_id` BIGINT(20) NOT NULL COMMENT '课程ID',
    `template_id` BIGINT(20) NOT NULL COMMENT '模板ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_course_id` (`course_id`),
    KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程评估模板绑定表';

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
('teacher_zhang', '030219', '张老师', 2, 'T2023001', '13800138001', 'zhang@example.com', 1),
('teacher_li', '030219', '李老师', 2, 'T2023002', '13800138002', 'li@example.com', 1),
('teacher_wang', '030219', '王老师', 2, 'T2023003', '13800138003', 'wang@example.com', 1);

-- 插入初始学生数据
INSERT INTO `user` (`username`, `password`, `real_name`, `role_type`, `student_no`, `phone`, `email`, `status`) VALUES
('student_wang', '030219', '王小明', 3, 'S2023001', '13800138011', 'wangxm@example.com', 1),
('student_li', '030219', '李小红', 3, 'S2023002', '13800138012', 'lixh@example.com', 1),
('student_zhang', '030219', '张伟', 3, 'S2023003', '13800138013', 'zhangw@example.com', 1),
('student_liu', '030219', '刘丽', 3, 'S2023004', '13800138014', 'liul@example.com', 1),
('student_chen', '030219', '陈强', 3, 'S2023005', '13800138015', 'chenq@example.com', 1),
('student_yang', '030219', '杨洋', 3, 'S2023006', '13800138016', 'yangy@example.com', 1);

-- 插入课程分类数据
INSERT INTO `course_category` (`category_name`, `status`) VALUES
('计算机科学', 1),
('数学', 1),
('英语', 1),
('体育', 1);

-- 插入初始课程数据
INSERT INTO `course` (`course_name`, `course_no`, `teacher_id`, `course_category`, `course_type`, `credit`, `class_hours`, `class_time`, `class_location`, `capacity`, `selected_count`, `course_status`, `description`) VALUES
('Java程序设计', 'CS101', 2, '计算机科学', 1, 3.0, 48, '周一 1-2节', '教学楼A101', 50, 3, 1, 'Java编程基础课程，从入门到精通'),
('数据库原理', 'CS102', 2, '计算机科学', 1, 3.0, 48, '周二 3-4节', '教学楼A102', 40, 2, 1, '数据库系统原理与应用，MySQL数据库'),
('数据结构', 'CS103', 3, '计算机科学', 2, 4.0, 64, '周三 5-6节', '教学楼A103', 45, 2, 1, '数据结构与算法分析'),
('计算机网络', 'CS104', 3, '计算机科学', 1, 3.0, 48, '周四 3-4节', '教学楼A104', 50, 1, 1, '计算机网络基础，TCP/IP协议'),
('操作系统', 'CS105', 4, '计算机科学', 2, 4.0, 64, '周五 5-6节', '教学楼A105', 40, 1, 1, '操作系统原理，Linux系统'),
('高等数学', 'MA101', 4, '数学', 1, 4.0, 64, '周一 3-4节', '教学楼B201', 60, 2, 1, '大学高等数学上册'),
('线性代数', 'MA102', 4, '数学', 1, 3.0, 48, '周二 1-2节', '教学楼B202', 55, 1, 1, '线性代数基础'),
('大学英语', 'EN101', 5, '英语', 1, 3.0, 48, '周三 1-2节', '教学楼C301', 50, 3, 1, '大学英语综合教程'),
('体育与健康', 'PE101', 5, '体育', 2, 2.0, 32, '周四 5-6节', '体育馆', 100, 2, 1, '大学体育课程');

-- 插入学生必修课数据
INSERT INTO `student_required_course` (`student_id`, `course_id`) VALUES
(4, 1), (4, 2), (4, 6), (4, 7), (4, 8),
(5, 1), (5, 2), (5, 6), (5, 7), (5, 8),
(6, 1), (6, 3), (6, 6), (6, 8),
(7, 2), (7, 3), (7, 6), (7, 7), (7, 9),
(8, 1), (8, 4), (8, 6), (8, 8), (8, 9),
(9, 3), (9, 5), (9, 7), (9, 8), (9, 9);

-- 插入选课数据（已选课状态）
INSERT INTO `course_selection` (`student_id`, `course_id`, `selection_status`) VALUES
(4, 1, 1), (4, 2, 1), (4, 6, 1), (4, 7, 1), (4, 8, 1),
(5, 1, 1), (5, 2, 1), (5, 6, 1), (5, 7, 1), (5, 8, 1),
(6, 1, 1), (6, 3, 1), (6, 6, 1), (6, 8, 1),
(7, 2, 1), (7, 3, 1), (7, 6, 1), (7, 7, 1), (7, 9, 1),
(8, 1, 1), (8, 4, 1), (8, 6, 1), (8, 8, 1), (8, 9, 1),
(9, 3, 1), (9, 5, 1), (9, 7, 1), (9, 8, 1), (9, 9, 1);

-- 插入初始评估模板数据
INSERT INTO `evaluation_template` (`template_name`, `template_type`, `description`, `status`) VALUES
('通用教学评估模板', 1, '适用于所有课程的教学评估，包含课程内容、教学方法、教师讲解等方面', 1),
('专业课程评估模板', 2, '适用于专业课程的深度评估，增加专业性指标', 1),
('公共课评估模板', 1, '适用于公共基础课程评估', 1);

-- 课程-评估模板绑定
INSERT INTO `course_evaluation_template` (`course_id`, `template_id`) VALUES
(1, 1), (2, 1), (3, 2), (4, 1), (5, 2),
(6, 3), (7, 3), (8, 3), (9, 3);

-- 插入评估题目数据（通用模板）
INSERT INTO `evaluation_question` (`template_id`, `question_content`, `question_type`, `question_order`, `status`) VALUES
(1, '课程内容是否充实完整？', 1, 1, 1),
(1, '教学方法是否灵活多样？', 1, 2, 1),
(1, '教师讲解是否清晰易懂？', 1, 3, 1),
(1, '课程难度是否适中？', 1, 4, 1),
(1, '师生互动是否充分？', 1, 5, 1),
(1, '请对本课程提出宝贵建议：', 3, 6, 1);

-- 插入评估题目数据（专业模板）
INSERT INTO `evaluation_question` (`template_id`, `question_content`, `question_type`, `question_order`, `status`) VALUES
(2, '课程专业性是否突出？', 1, 1, 1),
(2, '理论与实践结合如何？', 1, 2, 1),
(2, '案例分析是否有助于理解？', 1, 3, 1),
(2, '课程前沿性如何？', 1, 4, 1),
(2, '请详细描述对本课程的改进建议：', 3, 5, 1);

-- 插入评估题目数据（公共课模板）
INSERT INTO `evaluation_question` (`template_id`, `question_content`, `question_type`, `question_order`, `status`) VALUES
(3, '课程基础内容是否扎实？', 1, 1, 1),
(3, '教学方式是否适合公共课？', 1, 2, 1),
(3, '学习收获是否明显？', 1, 3, 1),
(3, '请写出对本课程的意见和建议：', 3, 4, 1);

-- 插入成绩数据
INSERT INTO `grade` (`student_id`, `course_id`, `grade_score`, `teacher_id`, `grade_status`, `remarks`) VALUES
(4, 1, 85.5, 2, 1, '表现良好'),
(4, 2, 88.0, 2, 1, '成绩优秀'),
(4, 6, 92.0, 4, 1, '数学成绩突出'),
(4, 7, 78.5, 4, 1, '需加强练习'),
(5, 1, 90.5, 2, 1, '表现优秀'),
(5, 2, 86.0, 2, 1, '学习认真'),
(5, 6, 95.0, 4, 1, '数学天赋高'),
(6, 1, 82.0, 2, 1, '课堂活跃'),
(6, 3, 89.5, 3, 1, '算法思维好'),
(7, 2, 84.0, 2, 1, '数据库基础扎实'),
(7, 3, 91.0, 3, 1, '数据结构掌握好'),
(8, 1, 87.5, 2, 1, '编程能力强'),
(8, 4, 83.0, 3, 1, '网络知识丰富'),
(9, 3, 94.0, 3, 1, '算法能力突出'),
(9, 5, 88.5, 4, 1, '操作系统理解深入');

-- 插入评估数据
INSERT INTO `evaluation` (`course_id`, `student_id`, `evaluation_content`, `evaluation_score`, `is_anonymous`) VALUES
(1, 4, '课程内容充实，老师讲解清晰，收获很大。', 4.5, 1),
(1, 5, 'Java入门的好课程，推荐！', 4.8, 1),
(1, 6, '老师很负责，代码示例丰富。', 4.6, 1),
(1, 8, '教学方式生动，易于理解。', 4.3, 1),
(2, 4, '数据库原理讲解透彻，实践性强。', 4.7, 1),
(2, 5, 'SQL练习很有效，希望增加更多项目实践。', 4.4, 1),
(2, 7, '老师备课充分，答疑耐心。', 4.5, 1),
(3, 6, '数据结构课程很有挑战性，但收获满满。', 4.8, 1),
(3, 7, '算法讲解清晰，代码质量高。', 4.6, 1),
(3, 9, '老师水平很高，深入浅出。', 4.9, 1),
(4, 8, '计算机网络知识实用，对工作有帮助。', 4.2, 1),
(5, 9, '操作系统课程让我对Linux有了深入了解。', 4.5, 1),
(6, 4, '高数老师讲得很细致，一步一步推导。', 4.6, 1),
(6, 5, '例题丰富，有助于理解和巩固。', 4.4, 1),
(6, 7, '课程难度适中，适合打基础。', 4.3, 1),
(6, 8, '老师很有耐心，答疑及时。', 4.5, 1),
(7, 4, '线性代数抽象，但老师讲解清晰。', 4.4, 1),
(7, 9, '矩阵运算讲解透彻。', 4.7, 1),
(8, 4, '英语课活动丰富，口语提升明显。', 4.5, 1),
(8, 5, '课文分析深入，词汇量提升大。', 4.6, 1),
(8, 6, '老师发音标准，学习氛围好。', 4.3, 1),
(8, 9, '阅读和写作能力都有提高。', 4.4, 1),
(9, 7, '体育课活动多样，锻炼效果好。', 4.8, 1),
(9, 8, '老师专业，篮球技术提升大。', 4.6, 1),
(9, 9, '体能训练科学，身体素质提高明显。', 4.7, 1);

-- 插入评估答案数据（针对每条评估的详细答案）
INSERT INTO `evaluation_answer` (`evaluation_id`, `question_id`, `answer_content`, `answer_score`) VALUES
(1, 1, 'A', 5), (1, 2, 'A', 5), (1, 3, 'A', 4), (1, 4, 'B', 4), (1, 5, 'B', 4),
(2, 1, 'A', 5), (2, 2, 'A', 5), (2, 3, 'A', 5), (2, 4, 'A', 5), (2, 5, 'A', 5),
(3, 1, 'A', 5), (3, 2, 'B', 4), (3, 3, 'A', 5), (3, 4, 'B', 4), (3, 5, 'B', 4),
(4, 1, 'A', 4), (4, 2, 'B', 4), (4, 3, 'B', 4), (4, 4, 'B', 4), (4, 5, 'C', 5),
(5, 1, 'A', 5), (5, 2, 'A', 5), (5, 3, 'A', 5), (5, 4, 'A', 4), (5, 5, 'B', 4),
(6, 1, 'A', 4), (6, 2, 'B', 4), (6, 3, 'A', 5), (6, 4, 'B', 4), (6, 5, 'C', 4),
(7, 1, 'A', 5), (7, 2, 'A', 5), (7, 3, 'A', 4), (7, 4, 'B', 4), (7, 5, 'A', 5),
(8, 1, 'A', 5), (8, 2, 'A', 5), (8, 3, 'A', 5), (8, 4, 'A', 4), (8, 5, 'A', 5),
(9, 1, 'A', 5), (9, 2, 'A', 4), (9, 3, 'A', 5), (9, 4, 'B', 4), (9, 5, 'B', 4),
(10, 1, 'A', 5), (10, 2, 'A', 5), (10, 3, 'A', 5), (10, 4, 'A', 5), (10, 5, 'A', 5);
