package br.com.jdbcpp.sample.domain;

import br.com.jdbcpp.api.PropStrategy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class ProductIndexedClass {

    @PropStrategy(resultSetIndex = 0)
    private long id;
    @PropStrategy(resultSetIndex = 1)
    private String name;
    @PropStrategy(resultSetIndex = 2)
    private BigDecimal price;
    @PropStrategy(resultSetIndex = 3)
    private long amount;
    @PropStrategy(resultSetIndex = 4)
    private OffsetDateTime createdAt;

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
