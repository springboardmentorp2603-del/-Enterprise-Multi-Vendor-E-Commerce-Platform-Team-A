package com.shopstack.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();

        String uploadPath = uploadDir.toUri().toString();

        System.out.println("Serving uploads from: " + uploadPath);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
        registry.addResourceHandler("/invoice-files/**")
                .addResourceLocations("file:invoice-files/");
        registry.addResourceHandler("/report-files/**")
                .addResourceLocations("file:report-files/");
    }
}