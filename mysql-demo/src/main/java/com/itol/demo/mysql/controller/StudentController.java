package com.itol.demo.mysql.controller;

import com.itol.demo.mysql.dao.Student;
import com.itol.demo.mysql.service.StudentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentController {
    @Resource
    private StudentService studentService;

    @GetMapping("/student/{id}")
    public Student getById(@PathVariable long id) {
        return studentService.getStudentById(id);
    }
}
