package com.krokochik.ideasForum.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/privacy").setViewName("privacy");
        registry.addViewController("/googleb8fcdd64aa45ba54.html").setViewName("googleb8fcdd64aa45ba54");
        registry.addViewController("/yandex_f4f03a518326d43b.html").setViewName("yandex_f4f03a518326d43b.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/scripts/**")
                .addResourceLocations("classpath:/static/scripts/");
        registry
                .addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
        registry
                .addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
    }
}
