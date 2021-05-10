package org.zfin.sequence.blast;

/**
 */
public enum WuBlastExitEnum {
    SUCCESS(0),
    BUS_ERROR(4),
    OKAY_ERROR_EXIT16(16),
    OKAY_ERROR_EXIT17(17),
    EMPTY_DATABASE(18),
    OKAY_ERROR_EXIT23(23),;


    private int value;

    WuBlastExitEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }


    public static int[] getNonErrorValues() {
        int[] exitValues = {
                SUCCESS.getValue(),
                BUS_ERROR.getValue(),
                OKAY_ERROR_EXIT16.getValue(),
                OKAY_ERROR_EXIT17.getValue(),
                OKAY_ERROR_EXIT23.getValue(),
        };
        return exitValues;
    }
}
