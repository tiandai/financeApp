package com.example.financeapp.model;

import jakarta.persistence.*;
        import jakarta.validation.constraints.*;
        import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "stock_ratings",
    uniqueConstraints = @UniqueConstraint(columnNames = {"ticker", "agency", "ratingDate"}),
    indexes = {
        @Index(name = "idx_ticker", columnList = "ticker"),
        @Index(name = "idx_agency", columnList = "agency")
    }
)

public class stockRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 10)
    private String ticker;

    @NotBlank
    @Size(max = 50)
    private String agency;

    @NotBlank
    @Size(max = 20)
    private String rating;

    @Size(max = 50)
    private String outlook;

    @NotNull
    private LocalDate ratingDate;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
