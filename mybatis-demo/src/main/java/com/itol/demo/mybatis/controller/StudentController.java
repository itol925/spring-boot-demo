package com.itol.demo.mybatis.controller;

import com.itol.demo.mybatis.dao.Student;
import com.itol.demo.mybatis.mapper.StudentMapper;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StudentController {
    @Resource
    private StudentMapper studentMapper;

    @GetMapping(value = "/student/select/{id}", produces = "application/json; charset=utf-8")
    public Student selectById(@PathVariable long id) {
        return studentMapper.selectById(id);
    }

    @PostMapping(value = "/student/insert", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void insert(Student student) {
        studentMapper.insert(student);
    }
}
