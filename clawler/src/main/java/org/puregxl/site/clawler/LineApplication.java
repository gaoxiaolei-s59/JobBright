package org.puregxl.site.clawler;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.puregxl.site.clawler", "org.puregxl.site.framework"})
@MapperScan("org.puregxl.site.clawler.mapper")
public class LineApplication {

    public static void main(String[] args) {
        SpringApplication.run(LineApplication.class, args);
    }
}
