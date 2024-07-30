package com.itol.demo.mysql.service;


import com.itol.demo.mysql.dao.Student;

public interface StudentService {
    Student getStudentById(long id);
}
