package org.itol.demo.mybatisplus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.itol.demo.mybatisplus.dao.Student;
import org.itol.demo.mybatisplus.mapper.StudentMapper;
import org.itol.demo.mybatisplus.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {
    @Override
    @Transactional // Transactional 表示事务执行
    public void updateByAnnotation(long id, int age) {
        Student student = getById(id);
        if (student == null) {
            throw new RuntimeException("student not exist!");
        }
        if (student.getAge() < age) {
            throw new RuntimeException("invalid age");
        }
        baseMapper.updateByAnnotation(id, age);
    }
}
