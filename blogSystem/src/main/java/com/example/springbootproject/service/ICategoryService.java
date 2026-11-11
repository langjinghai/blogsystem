package com.example.springbootproject.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springbootproject.pojo.Category;
import com.example.springbootproject.pojo.Result;

public interface ICategoryService extends IService<Category> {
    Result listCategories();
}
