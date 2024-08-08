package org.itol.demo.mysql.service;


import org.itol.demo.mysql.dao.Student;

import java.util.List;

public interface StudentService {
    Student insert(Student student);

    void delete(long id);

    Student update(Student student);

    Student select(long id);

    List<Student> selectAll();




}
