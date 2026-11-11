package com.example.springbootproject.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_comment")
public class Comment {
    // 评论ID
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 所属文章ID
    private Long articleId;

    // 评论内容
    private String content;

    // 评论人用户ID
    private Long authorId;

    // 父评论ID
    private Long parentId;

    // 创建时间
    private LocalDateTime createTime;}
