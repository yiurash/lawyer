package com.feldsher;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.feldsher.mapper")
public class FeldsherApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeldsherApplication.class, args);
    }

}
