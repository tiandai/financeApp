package com.example.financeapp;

import com.example.financeapp.controller.StockPriceController;
import com.example.financeapp.service.StockPriceService;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StockPriceControllerTest {

	@InjectMocks
	private StockPriceController stockPriceController;

	@Mock
	private StockPriceService stockPriceService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void lastPrice_shouldReturnOkResponse_whenValidTicker() throws Exception {
		String ticker = "AAPL";
		Map<String, Object> mockResponse = Map.of(
				"ticker", ticker,
				"price", new BigDecimal("175.32"),
				"date", "2024-04-01",
				"source", "database"
		);

		when(stockPriceService.getLastPrice(ticker)).thenReturn(mockResponse);

		ResponseEntity<Map<String, Object>> response = stockPriceController.getLastPrice(ticker);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(mockResponse, response.getBody());
	}

	@Test
	void lastPrice_shouldReturnNotFound_whenValidationFails() throws Exception {
		String ticker = "!!";
		when(stockPriceService.getLastPrice(ticker)).thenThrow(new ValidationException("invalid ticker"));

		ResponseEntity<Map<String, Object>> response = stockPriceController.getLastPrice(ticker);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("invalid ticker", response.getBody().get("error"));
	}

	@Test
	void history_shouldReturnData_whenValidTicker() throws Exception {
		String ticker = "AAPL";
		Map<String, Object> mockResponse = Map.of(
				"symbol", ticker,
				"data", List.of(Map.of("price", "4200", "date", "2024-04-01")),
				"source", "Yahoo Finance"
		);

		when(stockPriceService.getHistory(ticker)).thenReturn(mockResponse);

		ResponseEntity<Map<String, Object>> response = stockPriceController.getHistory(ticker);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(mockResponse, response.getBody());
	}

	@Test
	void history_shouldReturnNotFound_whenInvalidTicker() throws Exception {
		String ticker = "123!";
		when(stockPriceService.getHistory(ticker)).thenThrow(new ValidationException("invalid ticker"));

		ResponseEntity<Map<String, Object>> response = stockPriceController.getHistory(ticker);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("invalid ticker", response.getBody().get("error"));
	}
}
