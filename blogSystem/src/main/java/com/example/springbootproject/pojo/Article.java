package com.example.springbootproject.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_article")
public class Article implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键ID（自增）
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 文章标题
    private String title;

    // 文章摘要
    private String summary;

    //文章正文
    private String content;

    //封面图片URL
    private String coverImage;

    // 所属分类ID
    private Long categoryId;

    //作者用户ID
    private Long authorId;

    // 浏览量
    private Integer viewCount;

    // 点赞数
    private Integer likeCount;

    // 评论数
    private Integer commentCount;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;
}
