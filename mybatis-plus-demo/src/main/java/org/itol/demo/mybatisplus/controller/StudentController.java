package org.itol.demo.mybatisplus.controller;

import jakarta.annotation.Resource;
import org.itol.demo.mybatisplus.dao.Student;
import org.itol.demo.mybatisplus.mapper.StudentMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class StudentController {
    @Resource
    private StudentMapper studentMapper;

    @GetMapping(value = "/student/select/{id}", produces = "application/json; charset=utf-8")
    public Student selectById(@PathVariable long id) {
        return studentMapper.selectById(id);
    }

    @GetMapping(value = "/student/batch", produces = "application/json; charset=utf-8")
    public List<Student> selectBatch() {
        return studentMapper.selectBatchIds(Arrays.asList(1, 2));
    }

    @PostMapping(value = "/student/insert", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void insert(Student student) {
        studentMapper.insert(student);
    }
}
