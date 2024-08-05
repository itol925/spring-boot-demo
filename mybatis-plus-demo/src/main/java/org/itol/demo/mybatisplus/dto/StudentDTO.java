package org.itol.demo.mybatisplus.dto;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("表单实体")
public class StudentDTO {
    private long id;

    private String name;

    private String email;

    private long age;
}
