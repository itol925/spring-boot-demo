package org.itol.demo.mybatisplus.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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

    @GetMapping(value = "/student/all", produces = "application/json; charset=utf-8")
    public List<Student> selectAll() {
        Wrapper<Student> wrapper = new QueryWrapper<Student>().select("*");
        return studentMapper.selectList(wrapper);
    }

    @GetMapping(value = "/student/query/{key}", produces = "application/json; charset=utf-8")
    public List<Student> queryByName(@PathVariable String key) {
        // select * from student where name like %key%
        Wrapper<Student> wrapper = new QueryWrapper<Student>()
                .select("*")
                .like("name", key);
        return studentMapper.selectList(wrapper);
    }

    @GetMapping(value = "/student/query2/{key}", produces = "application/json; charset=utf-8")
    public List<Student> queryByName2(@PathVariable String key) {
        // Lambda 可以防止硬编码
        Wrapper<Student> wrapper = new LambdaQueryWrapper<Student>()
                .select(Student::getName, Student::getEmail, Student::getAge)
                .like(Student::getName, key);
        return studentMapper.selectList(wrapper);
    }

    @GetMapping(value = "/student/query2/{key}/{age}", produces = "application/json; charset=utf-8")
    public List<Student> queryByName(@PathVariable String key, @PathVariable long age) {
        // select * from student where name like %key% and age >= age
        Wrapper<Student> wrapper = new QueryWrapper<Student>()
                .select("*")
                .like("name", key)
                .ge("age", age);
        return studentMapper.selectList(wrapper);
    }


    @GetMapping(value = "/student/update/{name}/{age}", produces = "application/json; charset=utf-8")
    public int updateAge(@PathVariable String name, @PathVariable long age) {
        Student student = new Student();
        student.setAge(age); // 要更新的字段
        Wrapper<Student> wrapper = new QueryWrapper<Student>()
                .eq("name", name);
        return studentMapper.update(student, wrapper);
    }
    @GetMapping(value = "/student/update2/{name}", produces = "application/json; charset=utf-8")
    public int updateAge2(@PathVariable String name) {
        // update age = age + 1 where name = ?
        Wrapper<Student> wrapper = new UpdateWrapper<Student>()
                .setSql("age = age + 1")
                .eq("name", name);
        return studentMapper.update(null, wrapper);
    }
    @PostMapping(value = "/student/insert", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void insert(Student student) {
        studentMapper.insert(student);
    }
}
