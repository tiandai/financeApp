package com.example.financeapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Entity
@Table (name = "stock_prices",
        uniqueConstraints = { @UniqueConstraint(columnNames = {"ticker", "priceDateTS"})},
        indexes = { @Index(name = "idx_ticker", columnList = "ticker") }
)
public class StockPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    @NotBlank(message = "ticker is required")
    private String ticker;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable=false)
    @NotNull(message = "Price date is required")
    private Long priceDateTS;

    public Long getId() { return id; }

    public String getTicker() {
        return ticker;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Long getPriceDateTS() { return priceDateTS; }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setPriceDateTS(Long priceDateTS) {
        this.priceDateTS = priceDateTS;
    }
}
