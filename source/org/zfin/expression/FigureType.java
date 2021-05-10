package org.zfin.expression;

public enum FigureType {
    FIGURE("figure"),
    TOD("text only");

    private String name;

    FigureType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
