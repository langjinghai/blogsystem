package com.example.springbootproject.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Long id;               // 评论ID
    private Long articleId;        // 所属文章ID
    private String content;        // 评论内容
    private Long authorId;         // 评论人用户ID
    private String authorName;     // 评论人昵称
    private String authorAvatar;   // 评论人头像URL
    private Long parentId;         // 父评论ID
    private String createTime;     // 创建时间
}
