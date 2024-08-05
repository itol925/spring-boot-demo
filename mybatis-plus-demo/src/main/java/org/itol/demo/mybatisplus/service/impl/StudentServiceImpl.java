package org.itol.demo.mybatisplus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.itol.demo.mybatisplus.dao.Student;
import org.itol.demo.mybatisplus.mapper.StudentMapper;
import org.itol.demo.mybatisplus.service.StudentService;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {
}
