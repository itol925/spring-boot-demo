package com.itol.demo.mysql.service;

import com.itol.demo.mysql.dao.Student;
import com.itol.demo.mysql.dao.StudentRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentService {

    @Resource
    private StudentRepository studentRepository;

    @Override
    public Student getStudentById(long id) {
        return studentRepository.findById(id).orElse(null);
    }
}
