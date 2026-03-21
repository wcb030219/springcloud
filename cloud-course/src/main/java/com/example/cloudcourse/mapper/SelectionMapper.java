package com.example.cloudcourse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.pojo.CourseSelection;

@Mapper
public interface SelectionMapper extends BaseMapper<CourseSelection> {
}
