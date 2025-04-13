package com.example.financeapp.repository;

import com.example.financeapp.model.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {
    @Query("SELECT q FROM StockPrice q WHERE q.ticker = :ticker AND q.priceDateTS > :startDateTS ORDER BY q.priceDateTS DESC LIMIT 1")
    StockPrice getLastPrice(String ticker, Long startDateTS);

    @Query("SELECT q FROM StockPrice q WHERE q.ticker = :ticker AND q.priceDateTS > :startDateTS ORDER BY q.priceDateTS")
    List<StockPrice> getHistoryPrice(String ticker, Long startDateTS);
}