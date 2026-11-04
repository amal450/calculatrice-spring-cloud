package com.example.historique;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.historique.client")
public class HistoriqueServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HistoriqueServiceApplication.class, args);
    }
}