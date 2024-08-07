package org.itol.demo.mysql.controller;

import org.itol.demo.mysql.dao.Student;
import org.itol.demo.mysql.service.StudentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentController {
    @Resource
    private StudentService studentService;

    @GetMapping(value = "/student/{id}", produces = "application/json; charset=utf-8")
    public Student getById(@PathVariable long id) {
        return studentService.getStudentById(id);
    }
}
