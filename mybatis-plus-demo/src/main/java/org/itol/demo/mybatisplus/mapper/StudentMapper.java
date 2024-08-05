package org.itol.demo.mybatisplus.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.itol.demo.mybatisplus.dao.Student;

public interface StudentMapper extends BaseMapper<Student> {

    // 自定义 mapper 文件，通常用于复杂的 sql 语句，如跨表查询
    // mapper xml 文件可以在 yml 文件里配置，默认是 classpath*/mapper 目录下
    Student queryStudentByName(@Param("name") String name);

    void updateBuCustomSQL(@Param(Constants.WRAPPER) Wrapper<Student> wrapper, @Param("age") int age);
}
