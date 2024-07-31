package com.itol.demo.mybatis.dao;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
//@Builder
@TableName("student")
@NoArgsConstructor
public class Student implements Serializable {
    @TableId(type = IdType.AUTO)
    private long id;

    private String name;

    private String email;

    private long age;
}
