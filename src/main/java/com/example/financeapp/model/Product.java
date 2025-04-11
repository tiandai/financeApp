package com.example.financeapp.model;

import jakarta.persistence.*;

@Entity
@Table (name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price")
    private String price;

    @Column(name = "priceDateTS")
    private Long priceDateTS;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public Long getDateTS() { return priceDateTS; }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setPriceDateTS(Long priceDateTS) {
        this.priceDateTS = priceDateTS;
    }
}
