package com.example.music.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve frontend SPA
        registry.addResourceHandler("/frontend/**")
                .addResourceLocations("file:static/frontend/", "classpath:/static/frontend/");
        // Serve admin SPA
        registry.addResourceHandler("/admin/**")
                .addResourceLocations("file:static/admin/", "classpath:/static/admin/");
        // Root static resources
        registry.addResourceHandler("/*.ico", "/*.svg", "/*.png", "/*.jpg", "/*.webp")
                .addResourceLocations("file:static/frontend/", "classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // SPA fallback: serve index.html for frontend routes
        registry.addViewController("/frontend/{path:[^\\.]*}")
                .setViewName("forward:/frontend/index.html");
        // SPA fallback: serve index.html for admin routes
        registry.addViewController("/admin/{path:[^\\.]*}")
                .setViewName("forward:/admin/index.html");
    }
}
