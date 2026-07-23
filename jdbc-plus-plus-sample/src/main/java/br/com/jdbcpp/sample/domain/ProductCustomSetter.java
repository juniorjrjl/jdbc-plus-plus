package br.com.jdbcpp.sample.domain;

import br.com.jdbcpp.api.PropStrategy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class ProductCustomSetter {

    @PropStrategy("changeId")
    private long id;
    @PropStrategy(ignore = true)
    private String name;
    @PropStrategy("changePrice")
    private BigDecimal price;
    @PropStrategy(ignore = true)
    private long amount;
    @PropStrategy("changeCreatedAt")
    private OffsetDateTime createdAt;

    public long getId() {
        return id;
    }

    public void changeId(final long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void changeName(final String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void changePrice(final BigDecimal price) {
        this.price = price;
    }

    public long getAmount() {
        return amount;
    }

    public void changeAmount(final long amount) {
        this.amount = amount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void changeCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
