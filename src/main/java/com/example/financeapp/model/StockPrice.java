package com.example.financeapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table (name = "stock_prices", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ticker", "priceDateTS"})
})
public class StockPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ticker", nullable=false)
    @NotBlank(message = "ticker is required")
    private String ticker;

    @Column(name = "price", nullable=false)
    @NotBlank(message = "Price is required")
    private String price;

    @Column(name = "priceDateTS", nullable=false)
    @NotNull(message = "Price date is required")
    private Long priceDateTS;

    public Long getId() { return id; }

    public String getTicker() {
        return ticker;
    }

    public String getPrice() {
        return price;
    }

    public Long getPriceDateTS() { return priceDateTS; }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setPriceDateTS(Long priceDateTS) {
        this.priceDateTS = priceDateTS;
    }
}
