package br.com.jdbcpp.sample.domain;

import br.com.jdbcpp.api.InputParam;

import java.math.BigDecimal;

public class ProductInputParam {

    @InputParam(enumMethodValue = "ordinal", value = "readMeasurementUnit")
    private MeasurementUnit measurementUnit;
    @InputParam(ignore = true)
    private BigDecimal unitAmount;
    @InputParam(value = "readProductNature")
    private ProductNature productNature;
    @InputParam(enumMethodValue = "getSymbol", value = "readCurrency")
    private Currency currency;

    public MeasurementUnit readMeasurementUnit() {
        return measurementUnit;
    }

    public void writeMeasurementUnit(final MeasurementUnit measurementUnit) {
        this.measurementUnit = measurementUnit;
    }

    public BigDecimal readUnitAmount() {
        return  unitAmount;
    }

    public void writeUnitAmount(final BigDecimal unitAmount) {
        this.unitAmount = unitAmount;
    }

    public ProductNature readProductNature() {
        return productNature;
    }

    public void writeProductNature(final ProductNature productNature) {
        this.productNature = productNature;
    }

    public Currency readCurrency() {
        return currency;
    }

    public void writeCurrency(final Currency currency) {
        this.currency = currency;
    }

}
