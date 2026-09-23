package com.petboarding;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@MapperScan("com.petboarding.mapper")
@ConfigurationPropertiesScan
public class PetBoardingApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetBoardingApplication.class, args);
    }
}
