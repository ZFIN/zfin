package org.zfin.gwt.root.util;

public class BooleanCollector {

    private boolean booleanCollector;
    private boolean downValue;

    public BooleanCollector(boolean value) {
        this.downValue = value;
        booleanCollector = !downValue;
    }

    public boolean addBoolean(Boolean value) {
        if (value == downValue) {
            booleanCollector = value;
        }
        return booleanCollector == downValue;
    }

    public boolean arrivedValue() {
        return booleanCollector == downValue;
    }
}
