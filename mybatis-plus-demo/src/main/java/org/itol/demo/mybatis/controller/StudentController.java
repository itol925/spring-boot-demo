package org.itol.demo.mybatis.controller;

import jakarta.annotation.Resource;
import org.itol.demo.mybatis.dao.Student;
import org.itol.demo.mybatis.mapper.StudentMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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
