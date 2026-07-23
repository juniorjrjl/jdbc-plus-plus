package br.com.jdbcpp.sample.domain;

import br.com.jdbcpp.api.PropStrategy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class ProductClass {

    @PropStrategy
    private long id;
    @PropStrategy
    private String name;
    @PropStrategy
    private BigDecimal price;
    @PropStrategy
    private long amount;
    @PropStrategy
    private OffsetDateTime createdAt;

    public ProductClass() {}

    public ProductClass(final long id,
                        final String name,
                        final BigDecimal price,
                        final long amount,
                        final OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(final BigDecimal price) {
        this.price = price;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(final long amount) {
        this.amount = amount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
