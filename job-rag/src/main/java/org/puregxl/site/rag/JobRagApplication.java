package org.puregxl.site.rag;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@MapperScan("org.puregxl.site.rag.dao.mapper")
public class JobRagApplication {
    public static void main(String[] args) {
        SpringApplication.run(JobRagApplication.class);
    }
}
