package com.example.financeapp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import com.example.financeapp.model.StockPrice;
import com.example.financeapp.repository.StockPriceRepository;
import com.example.financeapp.utils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class StockPriceController {
    @Autowired
    Logger logger;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private StockPriceRepository stockPriceRepository;

    @GetMapping("/lastprice/{ticker}")
    public ResponseEntity<Map<String, Object>> lastPrice(@Valid @PathVariable(value = "ticker") String param) {
        Map<String, Object> responseData = new HashMap<>();
        String ticker = InputUtil.sanitize(param);

        if(!InputUtil.isValidTicker(ticker)) {
            responseData.put("error", "invalid ticker: " + ticker);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseData);
        }

        logger.info("lastPrice: Fetching stock data for: {}", ticker);
        responseData.put("ticker", ticker);

        try {
            long lastWorkDayTS = StockDateUtil.getLastWorkdayTS();
            StockPrice stockPrice = stockPriceRepository.getLastPrice(ticker, lastWorkDayTS);

            // if found in db, return
            if (stockPrice != null) {
                responseData.put("price", stockPrice.getPrice());
                responseData.put("date", StockDateUtil.getReadableDateUtc(stockPrice.getPriceDateTS()));
                responseData.put("source", "database");
                return ResponseEntity.ok(responseData);
            }

            // if not in database yet, get from external API
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("User-Agent", "Chrome/122.0.0.0");

            HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
            String url = "https://query2.finance.yahoo.com/v8/finance/chart/" + ticker;

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

            // only enter if 200 is received, otherwise goes to exception
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            long stockDateTS = root.findValue("currentTradingPeriod").findValue("pre").findValue("end").asLong();
            String rmPrice = root.findValue("regularMarketPrice").asText();

            // save stock data to DB
            StockPrice newStockPrice = new StockPrice();
            newStockPrice.setTicker(ticker);
            newStockPrice.setPrice(rmPrice);
            newStockPrice.setPriceDateTS(stockDateTS);
            stockPriceRepository.save(newStockPrice);

            // prepare and send response
            responseData.put("price", rmPrice);
            responseData.put("date", StockDateUtil.getReadableDateUtc(stockDateTS));
            responseData.put("source", "Yahoo Finance");
            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            logger.error("lastPrice: Error fetching data for {}: {}", ticker, e.getMessage());

            responseData.put("error", "Cannot find last price for " + ticker);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseData);
        }
    }

    @GetMapping("/history/{ticker}")
    public ResponseEntity<Map<String, Object>> history( @Valid @PathVariable(value = "ticker") String ticker) {
        Map<String, Object> responseData = new HashMap<>();

        ticker = InputUtil.sanitize(ticker);
        if(!InputUtil.isValidTicker(ticker)) {
            responseData.put("error", "invalid ticker: " + ticker);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseData);
        }

        logger.info("history: Fetching stock data for: {}", ticker);
        responseData.put("symbol", ticker);

        try {
            LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
            long startOfDayTS = oneMonthAgo.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
            int numOfWDays = StockDateUtil.getNumberOfWorkdays(31);

            List<StockPrice> history = stockPriceRepository.getHistoryPrice(ticker, startOfDayTS);

            // return if DB has at least last number of workdays of data
            if (history.size() >= numOfWDays) {
                List<Map<String, String>> historyData = new ArrayList<>();
                for(StockPrice stockPrice : history) {
                    Map<String, String> data = new HashMap<>();
                    data.put("date", StockDateUtil.getReadableDateUtc(stockPrice.getPriceDateTS()));
                    data.put("price", stockPrice.getPrice());
                    historyData.add(data);
                }
                responseData.put("source", "database");
                responseData.put("data", historyData);
                return ResponseEntity.ok(responseData);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("User-Agent", "Chrome/122.0.0.0");

            HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
            String url = "https://query2.finance.yahoo.com/v8/finance/chart/" + ticker + "?range=1mo&interval=1d";

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode priceNode = root.findValue("close");
            JsonNode dateTSNode = root.findValue("timestamp");

            // create lists and map to store and process data
            List<Map<String, String>> results = new ArrayList<>();
            List<StockPrice> toSave = new ArrayList<>();
            Map<Long, StockPrice> existing = history.stream().collect(Collectors.toMap(StockPrice::getPriceDateTS, p -> p));

            // iterate data array to prepare response data and object to save in DB
            for (int i = 0; i < priceNode.size(); i++) {
                String price = priceNode.get(i).asText();
                long dateTS = dateTSNode.get(i).asLong();

                Map< String, String > map = new HashMap<>();
                map.put("date", StockDateUtil.getReadableDateUtc(dateTS));
                map.put("price", price);
                results.add(map);

                StockPrice stockPrice = existing.getOrDefault(dateTS, new StockPrice());
                stockPrice.setPrice(price);
                stockPrice.setPriceDateTS(dateTS);
                stockPrice.setTicker(ticker);
                toSave.add(stockPrice);
            }
            // update DB
            stockPriceRepository.saveAll(toSave); // performs insert or update

            // return result
            responseData.put("source", "Yahoo Finance");
            responseData.put("data", results);
            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            logger.error("history: Error fetching data for {}: {}", ticker, e.getMessage());

            responseData.put("error", "Cannot find historical price for " + ticker);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseData);
        }
    }
}
