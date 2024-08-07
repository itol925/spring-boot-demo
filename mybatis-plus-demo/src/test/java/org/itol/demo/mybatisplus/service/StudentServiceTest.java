package org.itol.demo.mybatisplus.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.itol.demo.mybatisplus.dao.Student;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class StudentServiceTest {
    @Resource
    private StudentService studentService;

    @Test
    public void query() {
        studentService.count();
    }

    @Test
    public void list() {
        studentService.list();
    }

    @Test
    public void getById() {
        studentService.getById(1);
    }

    @Test
    public void listByIds() {
        studentService.listByIds(Arrays.asList(1, 2, 3));
    }

    @Test
    public void update() {
        Student student = new Student();
        student.setAge(19); // 要更新的字段，默认只更新非空字段
        Wrapper<Student> wrapper = new QueryWrapper<Student>()
                .eq("name", "张三");
        studentService.update(student, wrapper);
    }

    @Test
    public void updateByAnnotation() {
        studentService.updateByAnnotation(6, 1);
    }

    @Test
    public void insert() {
        Student student = new Student();
        student.setName("Lily");
        student.setAge(18);
        student.setEmail("ll@qq.com");
        studentService.save(student);
    }

    @Test
    public void lambdaQuery() {
        // lambda 查询，condition 可以判空，如果name传了空值则不作为条件
        String name = "王";
        Integer maxAge = 20;
        Integer minAge = 5;
        List<Student> students = studentService.lambdaQuery()
                .like(name != null, Student::getName, name)
                .le(maxAge != null, Student::getAge, maxAge)
                .ge(minAge != null, Student::getAge, minAge)
                .list();
    }

    @Test
    public void lambdaUpdate() {
        long updateId = 1;
        Student student = studentService.getById(updateId);
        // 更新 id 为 1 的 student 的 age(加 1)，如果年龄大于 18 了，就再更新 email
        studentService.lambdaUpdate()
                // 下面是 set 语句
                .set(Student::getAge, student.getAge() + 1)
                .set(student.getAge() > 18, Student::getEmail, "haha@qq.com")
                // 下面是 where 语句
                .eq(Student::getId, updateId)
                // 下面语句是执行，没有 update 将不会执行
                .update();
    }

    @Test
    public void page() {
        // 排序无效？？？
        Page<Student> page = Page.of(0, 2);
        page.addOrder(OrderItem.asc("age"));
        page.addOrder(OrderItem.asc("id"));
        Page<Student> result = studentService.page(page);

        System.out.println("total = " + result.getTotal());
        System.out.println("pages = " + result.getPages());
        List<Student> students = result.getRecords();
        students.forEach(System.out::println);
    }
}
