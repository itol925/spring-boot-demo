package org.itol.demo.mybatisplus.controller;

import jakarta.annotation.Resource;
import org.itol.demo.mybatisplus.dao.Student;
import org.itol.demo.mybatisplus.dto.StudentDTO;
import org.itol.demo.mybatisplus.service.StudentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student")
public class StudentController {
    @Resource
    private StudentService studentService;

    @PostMapping
    public void save(@RequestBody StudentDTO studentDTO) {
        Student student = transfer(studentDTO);
        studentService.save(student);
    }

    @GetMapping(value = "/student/select/{id}", produces = "application/json; charset=utf-8")
    public StudentDTO selectById(@PathVariable long id) {
        Student student = studentService.getById(id);
        return transfer(student);
    }

    private StudentDTO transfer(Student student) {
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setId(student.getId());
        studentDTO.setName(student.getName());
        studentDTO.setAge(student.getAge());
        studentDTO.setEmail(student.getEmail());
        return studentDTO;
    }

    private Student transfer(StudentDTO studentDTO) {
        Student student = new Student();
        student.setId(studentDTO.getId());
        student.setName(studentDTO.getName());
        student.setAge(studentDTO.getAge());
        student.setEmail(studentDTO.getEmail());
        return student;
    }
}
