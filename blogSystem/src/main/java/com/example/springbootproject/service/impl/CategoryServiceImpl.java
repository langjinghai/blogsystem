package com.example.springbootproject.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootproject.mapper.CategoryMapper;
import com.example.springbootproject.pojo.Category;
import com.example.springbootproject.pojo.Result;
import com.example.springbootproject.service.ICategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Override
    public Result listCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder);
        List<Category> list = list(wrapper);
        return Result.ok(list);
    }
}
