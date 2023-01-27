package com.krokochik.ideasForum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/terminal").setViewName("terminal");
        registry.addViewController("/googleb8fcdd64aa45ba54.html").setViewName("googleb8fcdd64aa45ba54");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/scripts/**")
                .addResourceLocations("classpath:/static/scripts/");
        registry
                .addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
    }
}
