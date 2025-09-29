package com.financialfinshieldguard.aiservice.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AiServiceController {


    @GetMapping("/test")
    public String test() {
        return "test";
    }
}
