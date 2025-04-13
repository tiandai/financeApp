package com.example.financeapp.utils;

import com.example.financeapp.model.StockPrice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpResponseUtil {

    public static ResponseEntity<Map<String, Object>> buildPriceResponse(StockPrice stockPrice, String source) {
        Map<String, Object> response = new HashMap<>();
        response.put("ticker", stockPrice.getTicker());
        response.put("price", stockPrice.getPrice());
        response.put("date", StockDateUtil.getReadableDateUtc(stockPrice.getPriceDateTS()));
        response.put("source", source);
        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<Map<String, Object>> buildHistoryResponse(List<StockPrice> history, String source, String ticker) {
        List<Map<String, String>> historyData = history.stream().map(p -> {
            Map<String, String> map = new HashMap<>();
            map.put("date", StockDateUtil.getReadableDateUtc(p.getPriceDateTS()));
            map.put("price", p.getPrice().toString());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("source", source);
        response.put("data", historyData);
        response.put("ticker", ticker);
        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<Map<String, Object>> errorResponse(String errorMessage) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", errorMessage);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
