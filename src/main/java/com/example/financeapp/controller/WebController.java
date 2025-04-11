package com.example.financeapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebController {

    @GetMapping("/hello")
    public String sayHello(@RequestParam(value = "myName", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

    @GetMapping("/lastprice")
    public String lastPrice(@RequestParam(value = "ticker", defaultValue = "%5EGSPC") String ticker) {
        return String.format("Hello %s!", ticker);
    }

    @GetMapping("/history")
    public String history(@RequestParam(value = "days", defaultValue = "30") String days) {
        return String.format("Hello %s!", days);
    }
}
