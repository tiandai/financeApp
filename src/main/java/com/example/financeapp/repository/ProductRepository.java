package com.example.financeapp.repository;

import com.example.financeapp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT q FROM Product q WHERE q.name LIKE %?1%")
    List<Product> getContainingProduct(String word);
}