package com.example.springbootproject.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootproject.mapper.ArticleMapper;
import com.example.springbootproject.mapper.CommentMapper;
import com.example.springbootproject.mapper.UserMapper;
import com.example.springbootproject.pojo.Article;
import com.example.springbootproject.pojo.Comment;
import com.example.springbootproject.pojo.CommentDTO;
import com.example.springbootproject.pojo.CommentForm;
import com.example.springbootproject.pojo.Result;
import com.example.springbootproject.pojo.User;
import com.example.springbootproject.pojo.UserDTO;
import com.example.springbootproject.service.ICommentService;
import com.example.springbootproject.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentService extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserMapper userMapper;

    // ==================== 发表评论 ====================
    @Override
    public Result saveComment(CommentForm commentForm) {
        // 1. 获取当前用户
        UserDTO user = UserHolder.getUser();
        Long authorId = user.getId();
        String nickName = user.getNickName();
        String icon = user.getIcon();

        // 2. CommentForm → Comment，设置作者 + 时间
        Comment comment = BeanUtil.copyProperties(commentForm, Comment.class);
        comment.setAuthorId(authorId);
        comment.setCreateTime(LocalDateTime.now());

        // 3. 入库
        save(comment);

        // 4. 更新文章评论数 +1
        articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .setSql("comment_count = comment_count + 1")
                .eq(Article::getId, commentForm.getArticleId()));

        // 5. Comment → CommentDTO，填充作者信息，返回新评论ID
        CommentDTO dto = BeanUtil.copyProperties(comment, CommentDTO.class);
        dto.setCreateTime(comment.getCreateTime().toString());
        dto.setAuthorName(nickName);
        dto.setAuthorAvatar(icon);

        return Result.ok(dto);
    }

    // ==================== 分页查评论 ====================
    @Override
    public Result pageByArticle(Long articleId, Integer pageNum, Integer size) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getArticleId, articleId)
                .orderByDesc(Comment::getCreateTime);

        Page<Comment> p = new Page<>(pageNum, size);
        page(p, wrapper);

        // 批量查作者信息
        Set<Long> authorIds = p.getRecords().stream()
                .map(Comment::getAuthorId).collect(Collectors.toSet());
        final Map<Long, User> userMap;
        if (!authorIds.isEmpty()) {
            userMap = userMapper.selectBatchIds(authorIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        } else {
            userMap = new HashMap<>();
        }

        List<CommentDTO> dtoList = p.getRecords().stream().map(c -> {
            CommentDTO dto = BeanUtil.copyProperties(c, CommentDTO.class);
            dto.setCreateTime(c.getCreateTime() != null ? c.getCreateTime().toString() : null);
            User author = userMap.get(c.getAuthorId());
            if (author != null) {
                dto.setAuthorName(author.getNickName());
                dto.setAuthorAvatar(author.getIcon());
            }
            return dto;
        }).collect(Collectors.toList());

        Page<CommentDTO> dtoPage = new Page<>(pageNum, size, p.getTotal());
        dtoPage.setRecords(dtoList);
        return Result.ok(dtoPage);
    }

    // ==================== 删除评论 ====================
    @Override
    public Result deleteById(Long commentId) {
        Long userId = UserHolder.getUser().getId();
        Comment comment = getById(commentId);
        if (comment == null) {
            return Result.fail("评论不存在");
        }
        if (!comment.getAuthorId().equals(userId)) {
            return Result.fail("只能删除自己的评论");
        }
        // 物理删除
        removeById(commentId);

        // 更新文章评论数 -1
        articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .setSql("comment_count = comment_count - 1")
                .eq(Article::getId, comment.getArticleId()));

        return Result.ok();
    }
}
