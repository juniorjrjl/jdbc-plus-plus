package br.com.jdbcpp.sample.domain;

public enum Currency {

    BRL("R$"),
    USD("$"),
    EUR("€"),
    JPY("¥");

    private final String symbol;

    Currency(final String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
