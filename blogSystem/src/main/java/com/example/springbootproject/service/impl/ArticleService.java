package com.example.springbootproject.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootproject.mapper.ArticleMapper;
import com.example.springbootproject.mapper.CategoryMapper;
import com.example.springbootproject.mapper.UserMapper;
import com.example.springbootproject.pojo.*;
import com.example.springbootproject.service.IArticleService;
import com.example.springbootproject.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.springbootproject.utils.Constants.BLOG_LIKED_KEY;

@Slf4j
@Service
public class ArticleService extends ServiceImpl<ArticleMapper, Article> implements IArticleService {
@Autowired
private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    // ==================== 分页查询 ====================
    @Override
    public Result page(Integer pageNum, Integer size, String keyword, Long categoryId) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .like(keyword != null && !keyword.isEmpty(), Article::getTitle, keyword)
                .eq(categoryId != null, Article::getCategoryId, categoryId)
                .orderByDesc(Article::getCreateTime);

        Page<Article> p = new Page<>(pageNum, size);
        page(p, wrapper);

        List<Article> records = p.getRecords();
        List<ArticleDTO> dtoList = toDTOList(records, null);
        Page<ArticleDTO> dtoPage = new Page<>(pageNum, size, p.getTotal());
        dtoPage.setRecords(dtoList);

        return Result.ok(dtoPage);
    }

    // ==================== 热门文章 ====================
    @Override
    public Result hot(Integer limit) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .orderByDesc(Article::getViewCount)
                .last("LIMIT " + limit);

        List<Article> articles = list(wrapper);
        return Result.ok(toDTOList(articles, null));
    }

    // ==================== 最新文章 ====================
    @Override
    public Result latest(Integer limit) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .orderByDesc(Article::getCreateTime)
                .last("LIMIT " + limit);

        List<Article> articles = list(wrapper);
        return Result.ok(toDTOList(articles, null));
    }

    // ==================== 文章详情 ====================
    @Override
    public Result detail(Long id, Long currentUserId) {
        Article article = getById(id);
        if (article == null) {
            return Result.fail("文章不存在");
        }
        ArticleDTO dto = toDTO(article, currentUserId);
          dto.setIsLiked(IsArticleLike(article));
        return Result.ok(dto);
    }

    // ==================== 我的文章 ====================
    @Override
    public Result myArticles(Integer pageNum, Integer size, Long authorId) {
        LambdaQueryWrapper<Article> wrapper=new LambdaQueryWrapper<Article>().
                eq(Article::getAuthorId,authorId).
                orderByDesc(Article::getCreateTime);
        Page<Article> p=new Page<>(pageNum, size);
        page(p,wrapper);
        List<Article> articles = p.getRecords();
        List<ArticleDTO> dtoList = toDTOList(articles, authorId);
        Page<ArticleDTO> dtoPage = new Page<>(pageNum, size, p.getTotal());
        dtoPage.setRecords(dtoList);
        return Result.ok(dtoPage);
    }

    // ==================== 发布文章 ====================
    @Override
    public Result publish(ArticleForm form, Long authorId) {
        Article article = new Article();
        article.setTitle(form.getTitle());
        article.setSummary(form.getSummary());
        article.setContent(form.getContent());
        article.setCoverImage(form.getCoverImage());
        article.setCategoryId(form.getCategoryId());
        article.setAuthorId(authorId);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        save(article);
        log.info("新文章发布成功，id={}, authorId={}", article.getId(), authorId);
        return Result.ok(article.getId());
    }

    // ==================== 编辑文章 ====================
    @Override
    public Result update(ArticleForm form, Long articleId, Long authorId) {
        Article article = getById(articleId);
        if (article == null) {
            return Result.fail("文章不存在");
        }
        if (!article.getAuthorId().equals(authorId)) {
            return Result.fail("无权修改他人文章");
        }
        article.setTitle(form.getTitle());
        article.setSummary(form.getSummary());
        article.setContent(form.getContent());
        article.setCoverImage(form.getCoverImage());
        article.setCategoryId(form.getCategoryId());
        updateById(article);
        log.info("文章更新成功，id={}", articleId);
        return Result.ok();
    }

    // ==================== 删除文章 ====================
    @Override
    public Result remove(Long articleId, Long authorId) {
        Article article = getById(articleId);
        if (article == null) {
            return Result.fail("文章不存在");
        }
        if (!article.getAuthorId().equals(authorId)) {
            return Result.fail("无权删除他人文章");
        }
        removeById(articleId);
        log.info("文章已删除，id={}", articleId);
        return Result.ok();
    }
    // ==================== 点赞功能 ====================
    @Override
    public Result like(Long articleId) {
        Long id = UserHolder.getUser().getId();
        String key=BLOG_LIKED_KEY+articleId.toString();
        Boolean success = stringRedisTemplate.opsForSet().isMember(key, id.toString());
        if (Boolean.TRUE.equals(success)) {
            update().setSql("like_count=like_count-1").eq("id", articleId).update();
            stringRedisTemplate.opsForSet().remove(key, id.toString());
        }else{
            update().setSql("like_count=like_count+1").eq("id", articleId).update();
            stringRedisTemplate.opsForSet().add(key, id.toString());
        }
        return Result.ok() ;
    }

    // ==================== 浏览量 ====================
    @Override
    public Result addView(Long articleId) {
        update().setSql("view_count = view_count + 1").eq("id", articleId).update();
        return Result.ok();
    }

    // ==================== 内部转换方法 ====================

    //转化单条文章
    private ArticleDTO toDTO(Article a, Long currentUserId) {
        ArticleDTO dto = BeanUtil.copyProperties(a, ArticleDTO.class);
        dto.setCreateTime(a.getCreateTime() != null ? a.getCreateTime().toString() : null);
        dto.setUpdateTime(a.getUpdateTime() != null ? a.getUpdateTime().toString() : null);

        // 填充作者名和头像
        if (a.getAuthorId() != null) {
            User author = userMapper.selectById(a.getAuthorId());
            if (author != null) {
                dto.setAuthorName(author.getNickName());
                dto.setAuthorAvatar(author.getIcon());
            }
        }

        // 填充分类名
        if (a.getCategoryId() != null) {
            Category cat = categoryMapper.selectById(a.getCategoryId());
            if (cat != null) {
                dto.setCategoryName(cat.getName());
            }
        }



        return dto;

    }
    //批量转换
    private List<ArticleDTO> toDTOList(List<Article> articles, Long currentUserId) {
        if (articles == null || articles.isEmpty()) return new ArrayList<>();

        // 批量取用户ID和分类ID
        Set<Long> authorIds = articles.stream().map(Article::getAuthorId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> categoryIds = articles.stream().map(Article::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());

        // 批量查询
        Map<Long, User> userMap = new HashMap<>();
        if (!authorIds.isEmpty()) {
            userMap = userMapper.selectBatchIds(authorIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        }

        Map<Long, Category> catMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            catMap = categoryMapper.selectBatchIds(categoryIds).stream()
                    .collect(Collectors.toMap(Category::getId, c -> c, (a, b) -> a));
        }

        Map<Long, User> finalUserMap = userMap;
        Map<Long, Category> finalCatMap = catMap;

        return articles.stream().map(a -> {
            ArticleDTO dto = BeanUtil.copyProperties(a, ArticleDTO.class);
            dto.setCreateTime(a.getCreateTime() != null ? a.getCreateTime().toString() : null);
            dto.setUpdateTime(a.getUpdateTime() != null ? a.getUpdateTime().toString() : null);

            User author = finalUserMap.get(a.getAuthorId());
            if (author != null) {
                dto.setAuthorName(author.getNickName());
                dto.setAuthorAvatar(author.getIcon());
            }

            Category cat = finalCatMap.get(a.getCategoryId());
            if (cat != null) {
                dto.setCategoryName(cat.getName());
            }

            // 填充点赞状态
            dto.setIsLiked(IsArticleLike(a));

            return dto;
        }).collect(Collectors.toList());

    }
    // 判断当前用户是否已点赞该文章
    private Boolean IsArticleLike(Article article) {
        Long userId = UserHolder.getUser().getId();
        String key = BLOG_LIKED_KEY + article.getId().toString();
        Boolean success = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        return Boolean.TRUE.equals(success);
    }
}



