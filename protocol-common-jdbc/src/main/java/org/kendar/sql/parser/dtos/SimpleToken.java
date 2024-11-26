package org.kendar.sql.parser.dtos;

public class SimpleToken {
    public void setType(TokenType type) {
        this.type = type;
    }

    private TokenType type;
    private String value;

    public SimpleToken(TokenType type,String value) {
        this.type = type;
        setValue(value);
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}