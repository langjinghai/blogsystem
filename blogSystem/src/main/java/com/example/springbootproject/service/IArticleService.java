package com.example.springbootproject.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springbootproject.pojo.Article;
import com.example.springbootproject.pojo.ArticleForm;
import com.example.springbootproject.pojo.Result;

public interface IArticleService extends IService<Article> {
    Result page(Integer page, Integer size, String keyword, Long categoryId);
    Result hot(Integer limit);
    Result latest(Integer limit);
    Result detail(Long id, Long currentUserId);
    Result myArticles(Integer page, Integer size, Long authorId);
    Result publish(ArticleForm form, Long authorId);
    Result update(ArticleForm form, Long articleId, Long authorId);
    Result remove(Long articleId, Long authorId);
    Result like(Long articleId);
    Result addView(Long articleId);
}
