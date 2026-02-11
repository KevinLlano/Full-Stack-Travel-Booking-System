package com.assessment.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "excursions")
public class Excursion {

    private static final Logger logger = LoggerFactory.getLogger(Excursion.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "excursion_id")
    private Long id;

    @Column(name = "excursion_title")
    private String excursionTitle;

    @Column(name = "excursion_price")
    private BigDecimal excursionPrice;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "create_date")
    @CreationTimestamp
    private LocalDateTime createDate;

    @Column(name = "last_update")
    @UpdateTimestamp
    private LocalDateTime lastUpdate;

    @ManyToOne
    @JoinColumn(name = "vacation_id")
    private Vacation vacation;

    @ManyToMany(mappedBy = "excursions")
    @JsonIgnore
    private Set<CartItem> cartItems = Collections.emptySet(); // Always non-null, immutable by default

    // Constructors
    public Excursion() {
    }

    // Getters and Setters with validation and immutability

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExcursionTitle() {
        return excursionTitle;
    }

    public void setExcursionTitle(String excursionTitle) {
        if (excursionTitle != null && excursionTitle.trim().isEmpty()) {
            logger.warn("Validation failed: Excursion title cannot be empty");
            throw new IllegalArgumentException("Excursion title cannot be empty");
        }
        this.excursionTitle = excursionTitle;
    }

    public BigDecimal getExcursionPrice() {
        return excursionPrice;
    }

    public void setExcursionPrice(BigDecimal excursionPrice) {
        if (excursionPrice != null && excursionPrice.compareTo(BigDecimal.ZERO) < 0) {
            logger.warn("Validation failed: Excursion price cannot be negative");
            throw new IllegalArgumentException("Excursion price cannot be negative");
        }
        this.excursionPrice = excursionPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Vacation getVacation() {
        return vacation;
    }

    public void setVacation(Vacation vacation) {
        this.vacation = vacation;
    }


    public Set<CartItem> getCartItems() {
        return cartItems;
    }


    public void setCartItems(Set<CartItem> cartItems) {
        this.cartItems = (cartItems == null) ? Collections.emptySet() : Set.copyOf(cartItems);
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Excursion excursion = (Excursion) o;
        return Objects.equals(id, excursion.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
