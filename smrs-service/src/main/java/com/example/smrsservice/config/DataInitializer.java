package com.example.smrsservice.config;

import com.example.smrsservice.service.MajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final MajorService majorService;
    @Override
    public void run(String... args) throws Exception {
              majorService.initializeMajors();
    }
}
