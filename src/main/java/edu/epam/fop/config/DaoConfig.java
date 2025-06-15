package edu.epam.fop.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "edu.epam.fop.dao")
public class DaoConfig {
} 