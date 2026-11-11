package com.example.springbootproject.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentForm {
    //文章id
    private Long articleId;
    //评论内容
    private String content;
    //父id
    private Long parentId;
}
