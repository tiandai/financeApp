package com.example.financeapp;

import com.example.financeapp.controller.StockPriceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class FinanceAppApplication {
    private static final Logger logger = LoggerFactory.getLogger(FinanceAppApplication.class);

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

	@Bean
	public Logger getLogger() {
		return logger;
	}

    public static void main(String[] args) {
		SpringApplication.run(FinanceAppApplication.class, args);
    }
}
