package org.itol.demo.mysql.service;

import org.itol.demo.mysql.dao.Student;
import org.itol.demo.mysql.dao.StudentRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    @Resource
    private StudentRepository studentRepository;

    @Override
    public Student insert(Student student) {
        return studentRepository.save(student);
    }

    @Override
    public void delete(long id) {
        studentRepository.deleteById(id);
    }

    @Override
    public Student update(Student student) {
        if (student.getId() == null) {
            throw new IllegalArgumentException("id is null");
        }
        Student old = studentRepository.findById(student.getId()).orElseThrow(() -> new RuntimeException("Student not found"));
        if (student.getName() != null) {
            old.setName(student.getName());
        }
        if (student.getEmail() != null) {
            old.setEmail(student.getEmail());
        }
        if (student.getAge() != null) {
            old.setAge(student.getAge());
        }
        return studentRepository.save(old);
    }

    @Override
    public Student select(long id) {
        return studentRepository.findById(id).orElse(null);
    }

    @Override
    public List<Student> selectAll() {
        return studentRepository.findAll();
    }
}
