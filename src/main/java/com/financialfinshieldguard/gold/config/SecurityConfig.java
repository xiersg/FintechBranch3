//package com.financialfinshieldguard.gold.config;
//
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//
//@EnableWebSecurity
//public class SecurityConfig extends WebSecurityConfigurerAdapter{
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .csrf().disable() // 禁用CSRF保护
//                .authorizeRequests()
//                .anyRequest().permitAll(); // 允许所有请求无需认证
//    }
//}