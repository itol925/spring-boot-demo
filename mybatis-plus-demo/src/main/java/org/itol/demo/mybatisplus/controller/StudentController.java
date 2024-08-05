package org.itol.demo.mybatisplus.controller;

import jakarta.annotation.Resource;
import org.itol.demo.mybatisplus.dao.Student;
import org.itol.demo.mybatisplus.dto.StudentDTO;
import org.itol.demo.mybatisplus.service.StudentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentController {
    @Resource
    private StudentService studentService;

    @GetMapping(value = "/student/select/{id}", produces = "application/json; charset=utf-8")
    public StudentDTO selectById(@PathVariable long id) {
        Student student = studentService.getById(id);
        return transfer(student);
    }

    StudentDTO transfer(Student student) {
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setId(student.getId());
        studentDTO.setName(student.getName());
        studentDTO.setAge(student.getAge());
        studentDTO.setEmail(student.getEmail());
        return studentDTO;
    }

}
