package com.example.springbootproject.controller;

import com.example.springbootproject.pojo.ArticleForm;
import com.example.springbootproject.pojo.Result;
import com.example.springbootproject.pojo.UserDTO;
import com.example.springbootproject.service.IArticleService;
import com.example.springbootproject.utils.UserHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/article")
public class ArticlesController {
    private static final Logger log = LoggerFactory.getLogger(ArticlesController.class);
    @Autowired
    private IArticleService articleService;

    // 分页查询
    @GetMapping("/page")
    public Result page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId
    ) {
        return articleService.page(page, size, keyword, categoryId);
    }

    //热门文章
    @GetMapping("/hot")
    public Result hot(@RequestParam(defaultValue = "5") Integer limit) {
        return articleService.hot(limit);
    }

    //最新文章
    @GetMapping("/latest")
    public Result latest(@RequestParam(defaultValue = "5") Integer limit) {
        return articleService.latest(limit);
    }

    // 文章详情
    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        UserDTO user = UserHolder.getUser();
        return articleService.detail(id, user != null ? user.getId() : null);
    }

    // 我的文章
    @GetMapping("/my")
    public Result myArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        UserDTO user = UserHolder.getUser();
        return articleService.myArticles(page, size, user.getId());
    }
    // 指定用户的文章
    @GetMapping("/user/{userId}")
    public Result userArticles(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return articleService.myArticles(page, size, userId);
    }
    // 发布文章
    @PostMapping
    public Result publish(@RequestBody ArticleForm form) {
        UserDTO user = UserHolder.getUser();
        return articleService.publish(form, user.getId());
    }

    // 编辑文章
    @PutMapping
    public Result update(@RequestBody ArticleForm form) {
        UserDTO user = UserHolder.getUser();
        return articleService.update(form, form.getId(), user.getId());
    }

    // 删除文章
    @DeleteMapping("/{id}")
    public Result remove(@PathVariable Long id) {
        UserDTO user = UserHolder.getUser();
        return articleService.remove(id, user.getId());
    }

    //点赞文章
    @PutMapping("/{id}/like")
    public Result like(@PathVariable("id") Long articleId) {
        return articleService.like(articleId);
    }

    // 增加浏览量
    @PutMapping("/{id}/view")
    public Result addView(@PathVariable("id") Long articleId) {
        return articleService.addView(articleId);
    }
}
