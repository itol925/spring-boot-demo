package org.itol.demo.mysql.service;


import org.itol.demo.mysql.dao.Student;

public interface StudentService {
    Student getStudentById(long id);
}
