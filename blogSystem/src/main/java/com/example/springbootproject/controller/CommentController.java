package com.example.springbootproject.controller;

import com.example.springbootproject.pojo.CommentForm;
import com.example.springbootproject.pojo.Result;
import com.example.springbootproject.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    // 发表评论
    @PostMapping
    public Result saveComment(@RequestBody CommentForm commentForm) {
        return commentService.saveComment(commentForm);
    }

    // 分页查文章评论
    @GetMapping("/article/{articleId}")
    public Result pageByArticle(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return commentService.pageByArticle(articleId, page, size);
    }

    // 删除评论
    @DeleteMapping("/{id}")
    public Result deleteComment(@PathVariable Long id) {
        return commentService.deleteById(id);
    }
}
