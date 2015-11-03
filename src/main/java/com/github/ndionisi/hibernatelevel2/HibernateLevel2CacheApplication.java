package com.github.ndionisi.hibernatelevel2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAutoConfiguration
@EnableTransactionManagement
public class HibernateLevel2CacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(HibernateLevel2CacheApplication.class, args);
    }
}
