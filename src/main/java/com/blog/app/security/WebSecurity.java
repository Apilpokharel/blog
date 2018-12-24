package com.blog.app.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {


    public WebSecurity (){

    }

    protected void configure(HttpSecurity http) throws Exception{


        http.cors().and().csrf().disable()
                .authorizeRequests()
                .antMatchers("/manage/gateway","/", "/register", "/login","/blog/{haa}" ,"/addblog", "/verification","/user","/verification/{text}","/update_user","/forgetPass", "/forgetPass1/{text}","/forgetAccount","/setPass","/PassReset","/tag/comparision","/tag/statics","/error_page1","/user/suggestion","/sumUser/Add", "/falUser/Remove").permitAll()
                .antMatchers("/js/**", "/css/**", "/img/**").permitAll()

                .antMatchers("/user/**").hasAnyRole("USER")



        ;



    }
}
