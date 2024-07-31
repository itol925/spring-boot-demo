package org.itol.demo.mybatisplus.dao;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 表字段与类字段的约定的映射关系：
 * 驼峰转下划线
 * id 默认为主键
 * 或者用 @TableName @TableId @TableField 显示关联
 * 非数据库字段可以 @TableField(exist = false)
 */
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
