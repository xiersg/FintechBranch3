package com.financialfinshieldguard.authenticationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication(scanBasePackages = {"com.financialfinishieldguard.gateutils","com.financialfinshieldguard.authenticationservice"})
@SpringBootApplication
public class AuthenticationServiceApplication {

    public static void main(String[] args) {
        SpringApplication. run(AuthenticationServiceApplication.class, args);
    }

}
