// StockPriceService.java
package com.example.financeapp.service;

import com.example.financeapp.model.StockPrice;
import com.example.financeapp.repository.StockPriceRepository;
import com.example.financeapp.utils.InputUtil;
import com.example.financeapp.utils.StockDateUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StockPriceService {

    private static final Logger logger = LoggerFactory.getLogger(StockPriceService.class);

    @Autowired
    private StockPriceRepository stockPriceRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    public Map<String, Object> getLastPrice(String param) throws Exception {
        Map<String, Object> responseData = new HashMap<>();

        String ticker = InputUtil.sanitize(param);
        if (!InputUtil.isValidTicker(ticker)) {
            throw new ValidationException("Invalid ticker: " + ticker);
        }

        responseData.put("ticker", ticker);

        // get lastWorkday and Convert to timestamp in seconds (00:00:00 UTC)
        LocalDate lastWorkday = StockDateUtil.getLastWorkday();
        long lastWorkDayTS = lastWorkday.atStartOfDay(ZoneOffset.UTC).toEpochSecond();

        StockPrice stockPrice = stockPriceRepository.getLastPrice(ticker, lastWorkDayTS);

        // if found data from DB, return it
        if (stockPrice != null) {
            responseData.put("price", stockPrice.getPrice());
            responseData.put("date", StockDateUtil.getReadableDateUtc(stockPrice.getPriceDateTS()));
            responseData.put("source", "database");
            return responseData;
        }

        // otherwise fetch it from external API
        String url = "https://query2.finance.yahoo.com/v8/finance/chart/" + ticker;
        HttpEntity<String> request = buildHttpEntity();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        JsonNode root = mapper.readTree(response.getBody());
        JsonNode timeStamps = root.findValue("timestamp");
        long stockDateTS = timeStamps.get(0).asLong();
        String rmPrice = root.findValue("regularMarketPrice").asText();

        // save it to DB for fast retrieval in future requests
        StockPrice newStockPrice = new StockPrice();
        newStockPrice.setTicker(ticker);
        newStockPrice.setPrice(new BigDecimal(rmPrice));
        newStockPrice.setPriceDateTS(stockDateTS);

        // handle SQL insert exception gracefully without breaking http response
        try {
            stockPriceRepository.save(newStockPrice);
        } catch (Exception e) {
            logger.warn("Could not save to DB: {}", e.getMessage());
        }

        responseData.put("price", new BigDecimal(rmPrice));
        responseData.put("date", StockDateUtil.getReadableDateUtc(stockDateTS));
        responseData.put("source", "Yahoo Finance");
        return responseData;
    }

    public Map<String, Object> getHistory(String param) throws Exception {
        Map<String, Object> responseData = new HashMap<>();

        String ticker = InputUtil.sanitize(param);
        if (!InputUtil.isValidTicker(ticker)) {
            throw new ValidationException("Invalid ticker: " + ticker);
        }

        responseData.put("ticker", ticker);

        LocalDate oneMonthAgo = StockDateUtil.getLastWorkday().minusMonths(1);
        long startOfDayTS = oneMonthAgo.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        int numOfWDays = StockDateUtil.getNumberOfWorkdays(30);

        List<StockPrice> history = stockPriceRepository.getHistoryPrice(ticker, startOfDayTS);

        if (history.size() >= numOfWDays) {
            List<Map<String, Object>> historyData = formatHistoryData(history);
            responseData.put("source", "database");
            responseData.put("data", historyData);
            return responseData;
        }

        String url = "https://query2.finance.yahoo.com/v8/finance/chart/" + ticker + "?range=1mo&interval=1d";
        HttpEntity<String> request = buildHttpEntity();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        JsonNode root = mapper.readTree(response.getBody());
        JsonNode priceNode = root.findValue("close");
        JsonNode dateTSNode = root.findValue("timestamp");

        List<Map<String, Object>> results = new ArrayList<>();
        List<StockPrice> toSave = new ArrayList<>();
        Map<Long, StockPrice> existing = history.stream().collect(Collectors.toMap(StockPrice::getPriceDateTS, p -> p));

        for (int i = 0; i < priceNode.size(); i++) {
            BigDecimal price = new BigDecimal(priceNode.get(i).asText());
            long dateTS = dateTSNode.get(i).asLong();

            Map<String, Object> map = new HashMap<>();
            map.put("date", StockDateUtil.getReadableDateUtc(dateTS));
            map.put("price", price);
            results.add(map);

            StockPrice stockPrice = existing.getOrDefault(dateTS, new StockPrice());
            stockPrice.setTicker(ticker);
            stockPrice.setPrice(price);
            stockPrice.setPriceDateTS(dateTS);
            toSave.add(stockPrice);
        }

        // handle SQL insert exception gracefully
        try {
            stockPriceRepository.saveAll(toSave);
        } catch (Exception e) {
            logger.warn("Could not save to DB: {}", e.getMessage());
        }

        responseData.put("source", "Yahoo Finance");
        responseData.put("data", results);
        return responseData;
    }

    private HttpEntity<String> buildHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("User-Agent", "Chrome/122.0.0.0");
        return new HttpEntity<>("", headers);
    }

    private List<Map<String, Object>> formatHistoryData(List<StockPrice> history) {
        List<Map<String, Object>> historyData = new ArrayList<>();
        for (StockPrice stockPrice : history) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", StockDateUtil.getReadableDateUtc(stockPrice.getPriceDateTS()));
            map.put("price", stockPrice.getPrice());
            historyData.add(map);
        }
        return historyData;
    }
}
