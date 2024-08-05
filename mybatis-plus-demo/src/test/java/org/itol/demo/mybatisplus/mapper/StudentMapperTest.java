package org.itol.demo.mybatisplus.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.annotation.Resource;
import org.itol.demo.mybatisplus.dao.Student;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class StudentMapperTest {

    @Resource
    private StudentMapper studentMapper;

    @Test
    public void queryById() {
        Student student = studentMapper.selectById(1);
    }

    @Test
    public void queryByCustomMapper() {
        // queryStudentByName 为手写的 wrapper sql
        Student student = studentMapper.queryStudentByName("张三");
    }

    @Test
    public void selectAll() {
        Wrapper<Student> wrapper = new QueryWrapper<Student>().select("*");
        studentMapper.selectList(wrapper);
    }

    @Test
    public void queryByLambda() {
        // Lambda 可以防止硬编码
        Wrapper<Student> wrapper = new LambdaQueryWrapper<Student>()
                .select(Student::getName, Student::getEmail, Student::getAge)
                .like(Student::getName, "王");
        studentMapper.selectList(wrapper);
    }

    @Test
    public void queryByLike() {
        // select * from student where name like %key% and age >= age
        Wrapper<Student> wrapper = new QueryWrapper<Student>()
                .select("*")
                .like("name", "张")
                .ge("age", 6);
        studentMapper.selectList(wrapper);
    }

    @Test
    public void update() {
        Student student = new Student();
        student.setAge(18); // 要更新的字段，默认只更新非空字段
        Wrapper<Student> wrapper = new QueryWrapper<Student>()
                .eq("name", "张三");
        studentMapper.update(student, wrapper);
    }
    @Test
    public void updateBySql() {
        // update age = age + 1 where name = ?
        Wrapper<Student> wrapper = new UpdateWrapper<Student>()
                .setSql("age = age + 1") // 注：这里引入了业务层的逻辑，不太规范
                .eq("name", "Lucy");
        studentMapper.update(null, wrapper);
    }

    @Test
    public void updateBySql2() {
        // MP 对于 where 条件构造能力比较强，但对于 select 构造能力比较弱，而且 select 往往与业务逻辑耦合
        // 下面为 MP 与 手写 mapper 相结合的方式，将 业务逻辑写到 mapper 中
        int age = 1;
        Wrapper<Student> wrapper = new QueryWrapper<Student>().eq("name", "Lucy");
        studentMapper.updateBuCustomSQL(wrapper, age);
    }

    @Test
    public void insert() {
        Student student = new Student();
        student.setName("Lucy");
        student.setAge(34);
        student.setEmail("ly@qq.com");
        studentMapper.insert(student);
    }
}
