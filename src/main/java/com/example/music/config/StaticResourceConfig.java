package com.example.music.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String baseDir = Paths.get(System.getProperty("user.dir"), "static").toUri().toString();

        // Serve frontend SPA
        registry.addResourceHandler("/frontend/**")
                .addResourceLocations(baseDir + "frontend/", "classpath:/static/frontend/");
        // Serve admin SPA
        registry.addResourceHandler("/admin/**")
                .addResourceLocations(baseDir + "admin/", "classpath:/static/admin/");
        // Root static resources
        registry.addResourceHandler("/*.ico", "/*.svg", "/*.png", "/*.jpg", "/*.webp")
                .addResourceLocations(baseDir + "frontend/", "classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // SPA root paths
        registry.addViewController("/frontend").setViewName("forward:/frontend/index.html");
        registry.addViewController("/frontend/").setViewName("forward:/frontend/index.html");
        registry.addViewController("/admin").setViewName("forward:/admin/index.html");
        registry.addViewController("/admin/").setViewName("forward:/admin/index.html");
        // SPA fallback: serve index.html for frontend routes
        registry.addViewController("/frontend/{path:[^\\.]*}").setViewName("forward:/frontend/index.html");
        // SPA fallback: serve index.html for admin routes
        registry.addViewController("/admin/{path:[^\\.]*}").setViewName("forward:/admin/index.html");
    }
}
