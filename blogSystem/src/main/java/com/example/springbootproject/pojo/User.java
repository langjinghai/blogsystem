package com.example.springbootproject.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体。
 * 映射表 tb_user，存储登录账号与基础资料。
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

     //主键，id自增
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String phone;

    @JsonIgnore
    private String password;

    private String nickName;

    private String icon;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
