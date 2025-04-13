package com.example.financeapp.controller;

import com.example.financeapp.service.StockPriceService;
import com.example.financeapp.utils.InputUtil;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class StockPriceController {

    private final StockPriceService stockPriceService;

    @Autowired
    public StockPriceController(StockPriceService stockPriceService) {
        this.stockPriceService = stockPriceService;
    }

    @GetMapping("/lastprice/{ticker}")
    public ResponseEntity<Map<String, Object>> getLastPrice(@Valid @PathVariable String ticker) {
        try {
            // call service to get data
            Map<String, Object> result = stockPriceService.getLastPrice(ticker);

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Stock price not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/history/{ticker}")
    public ResponseEntity<Map<String, Object>> getHistory(@Valid @PathVariable String ticker) {
        try {
            // call service to get data
            Map<String, Object> result = stockPriceService.getHistory(ticker);

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Stock price not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal error occurred"));
        }
    }
}
