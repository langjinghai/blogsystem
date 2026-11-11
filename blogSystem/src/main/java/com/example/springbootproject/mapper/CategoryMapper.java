package com.example.springbootproject.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springbootproject.pojo.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
