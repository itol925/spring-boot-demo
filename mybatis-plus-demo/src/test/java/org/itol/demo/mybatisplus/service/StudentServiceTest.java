package org.itol.demo.mybatisplus.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.itol.demo.mybatisplus.dao.Student;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
public class StudentServiceTest {
    @Resource
    private StudentService studentService;

    @Test
    public void query() {
        studentService.count();
    }

    @Test
    public void list() {
        studentService.list();
    }

    @Test
    public void getById() {
        studentService.getById(1);
    }

    @Test
    public void listByIds() {
        studentService.listByIds(Arrays.asList(1, 2, 3));
    }

    @Test
    public void update() {
        Student student = new Student();
        student.setAge(19); // 要更新的字段，默认只更新非空字段
        Wrapper<Student> wrapper = new QueryWrapper<Student>()
                .eq("name", "张三");
        studentService.update(student, wrapper);
    }

    @Test
    public void insert() {
        Student student = new Student();
        student.setName("Lily");
        student.setAge(18);
        student.setEmail("ll@qq.com");
        studentService.save(student);
    }
}
