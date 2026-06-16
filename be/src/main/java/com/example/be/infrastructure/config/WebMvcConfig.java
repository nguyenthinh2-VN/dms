package com.example.be.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;
    private final LanguageInterceptor languageInterceptor;

    public WebMvcConfig(RateLimitInterceptor rateLimitInterceptor, LanguageInterceptor languageInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
        this.languageInterceptor = languageInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**");
        registry.addInterceptor(languageInterceptor)
                .addPathPatterns("/api/**");
    }
}
