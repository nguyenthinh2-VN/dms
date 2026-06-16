package com.example.be.infrastructure.config;

import com.example.be.application.util.LanguageContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LanguageInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String lan = request.getParameter("lan");
        if (lan == null || lan.trim().isEmpty()) {
            lan = request.getHeader("Accept-Language");
        }
        
        if (lan != null && lan.toUpperCase().contains("TW")) {
            LanguageContextHolder.setLanguage("TW");
        } else {
            LanguageContextHolder.setLanguage("VI");
        }
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        LanguageContextHolder.clear();
    }
}
