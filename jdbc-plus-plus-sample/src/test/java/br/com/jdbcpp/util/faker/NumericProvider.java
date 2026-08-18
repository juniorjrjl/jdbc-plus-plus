package br.com.jdbcpp.util.faker;

import net.datafaker.providers.base.AbstractProvider;

import java.math.BigDecimal;

public class NumericProvider extends AbstractProvider<CustomFaker> {

    protected NumericProvider(final CustomFaker faker) {
        super(faker);
    }

    public BigDecimal nonZeroPositive(){
        return valueBetween(1, Integer.MAX_VALUE);
    }

    public BigDecimal valueBetween(final int min, final int max) {
        return new BigDecimal(Double.toString(faker.number().randomDouble(2 ,min, max)));
    }

}