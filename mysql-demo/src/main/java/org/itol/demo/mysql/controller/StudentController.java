package org.itol.demo.mysql.controller;

import org.itol.demo.mysql.dao.Student;
import org.itol.demo.mysql.service.StudentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {
    @Resource
    private StudentService studentService;
    @PostMapping(value = "/insert")
    public Student insert(@RequestBody Student student) {
        return studentService.insert(student);
    }

    @DeleteMapping(value = "/delete/{id}")
    public void delete(@PathVariable long id) {
        studentService.delete(id);
    }

    @PostMapping(value = "/update")
    public Student update(@RequestBody Student student) {
        return studentService.update(student);
    }

    @GetMapping(value = "/select/{id}", produces = "application/json; charset=utf-8")
    public Student select(@PathVariable long id) {
        return studentService.select(id);
    }

    @GetMapping(value = "/all", produces = "application/json; charset=utf-8")
    public List<Student> selectAll() {
        return studentService.selectAll();
    }
}
