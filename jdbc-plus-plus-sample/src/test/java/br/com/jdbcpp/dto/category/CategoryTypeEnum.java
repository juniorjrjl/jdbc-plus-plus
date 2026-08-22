package br.com.jdbcpp.dto.category;

public enum CategoryTypeEnum {

    TECHNOLOGY("technology"),
    HEALTH("health"),
    ELECTRONIC("electronic"),
    FURNITURE("furniture");

    private final String value;

    CategoryTypeEnum(final String value) {
        this.value = value;
    }

    public String getEnumNameLowerCase() {
        return value;
    }

}
