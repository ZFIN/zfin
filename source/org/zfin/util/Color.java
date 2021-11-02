package org.zfin.util;

public enum Color {

    VIOLET("violet"),
    BLUE("blue"),
    CYAN("cyan"),
    GREEN("green"),
    YELLOW("yellow"),
    ORANGE("orange"),
    RED("red"),
    FAR_RED("far red");

    String name;

    Color(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Color getColor(String colorName) {
        for(Color color : values()){
            if(color.getName().equals(colorName))
                return color;
        }
        return null;
    }
}
