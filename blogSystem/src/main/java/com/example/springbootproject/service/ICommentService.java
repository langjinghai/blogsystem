package com.example.springbootproject.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springbootproject.pojo.Comment;
import com.example.springbootproject.pojo.CommentForm;
import com.example.springbootproject.pojo.Result;

public interface ICommentService extends IService<Comment> {
    // 发表评论
    Result saveComment(CommentForm commentForm);

    // 分页查文章评论
    Result pageByArticle(Long articleId, Integer page, Integer size);

    // 删除评论
    Result deleteById(Long commentId);
}
