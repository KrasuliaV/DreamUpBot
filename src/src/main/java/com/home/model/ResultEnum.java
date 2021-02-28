package com.home.model;

import java.util.stream.Stream;

public enum ResultEnum {
    KINESTHETIC("0"),
    VISUAL("1"),
    AUDIAL("2"),
    DISCRETE("3");

    String resultNumber;

    ResultEnum(String resultNumber) {
        this.resultNumber = resultNumber;
    }

    public String getResultNumber() {
        return resultNumber;
    }

    public static ResultEnum getResultName(String resultNumber) {
        return Stream.of(ResultEnum.values())
                .filter(res -> res.getResultNumber().equals(resultNumber))
                .findFirst()
                .orElse(ResultEnum.DISCRETE);
    }
}
