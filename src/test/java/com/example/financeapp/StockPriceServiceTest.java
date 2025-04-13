package com.example.financeapp;

import com.example.financeapp.model.StockPrice;
import com.example.financeapp.repository.StockPriceRepository;
import com.example.financeapp.service.StockPriceService;
import com.example.financeapp.utils.StockDateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StockPriceServiceTest {

    @InjectMocks
    private StockPriceService stockPriceService;

    @Mock
    private StockPriceRepository stockPriceRepository;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getLastPrice_shouldThrowValidationException_forInvalidTicker() {
        assertThrows(ValidationException.class, () -> stockPriceService.getLastPrice("!!!"));
    }

    @Test
    void getLastPrice_shouldReturnFromDatabase_whenFound() throws Exception {
        String ticker = "AAPL";
        long ts = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toEpochSecond();

        StockPrice mockPrice = new StockPrice();
        mockPrice.setTicker(ticker);
        mockPrice.setPrice(new BigDecimal("123.45"));
        mockPrice.setPriceDateTS(ts);

        when(stockPriceRepository.getLastPrice(eq(ticker), anyLong())).thenReturn(mockPrice);

        Map<String, Object> result = stockPriceService.getLastPrice(ticker);

        assertEquals(ticker, result.get("ticker"));
        assertEquals(new BigDecimal("123.45"), result.get("price"));
        assertEquals("database", result.get("source"));
    }

    @Test
    void getHistory_shouldThrowValidationException_forInvalidTicker() {
        assertThrows(ValidationException.class, () -> stockPriceService.getHistory("123!!"));
    }

    @Test
    void getHistory_shouldReturnDataFromDatabase_whenSufficient() throws Exception {
        String ticker = "AAPL";
        long ts = LocalDate.now().minusDays(5).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        StockPrice sp = new StockPrice();
        sp.setTicker(ticker);
        sp.setPrice(new BigDecimal("150.00"));
        sp.setPriceDateTS(ts);

        List<StockPrice> mockData = new ArrayList<>(Collections.nCopies(22, sp));

        when(stockPriceRepository.getHistoryPrice(eq(ticker), anyLong())).thenReturn(mockData);

        Map<String, Object> result = stockPriceService.getHistory(ticker);

        assertEquals("database", result.get("source"));
        assertTrue(((List<?>) result.get("data")).size() >= 22);
    }
}
