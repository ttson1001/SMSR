package com.example.smrsservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ProjectStatusConverter projectStatusConverter;

    public WebConfig(ProjectStatusConverter projectStatusConverter) {
        this.projectStatusConverter = projectStatusConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(projectStatusConverter);
    }


}
