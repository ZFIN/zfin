package org.zfin.gwt.root.util;

import java.util.Comparator;

/**
 * Comparator that is aware of numeric values.
 */
public class NumberAwareStringComparatorDTO implements Comparator<String> {

    private static int numberOfDigits = 5;

    public int compare(String name1, String name2) {
        if (name1 == null && name2 != null)
            return -1;
        if (name1 != null && name2 == null)
            return +1;
        if (name1 == null && name2 == null)
            return -1;

        String termNameOne = addZerosToString(name1);
        String termNameTwo = addZerosToString(name2);

        return termNameOne.compareToIgnoreCase(termNameTwo);
    }

    public static String addZerosToString(String termName) {
        if (termName == null)
            return null;

        StringBuilder builder = new StringBuilder(termName.length() + 10);
        char[] characters = termName.toCharArray();
        boolean lastCharacterIsNumeral = false;
        StringBuilder numeralBuffer = new StringBuilder(4);
        for (char character : characters) {
            if (Character.isDigit(character)) {
                numeralBuffer.append(character);
                lastCharacterIsNumeral = true;
            } else {
                // pad if the last character was a numeral and now we encountered a non-digit.
                if (lastCharacterIsNumeral) {
                    addPaddedNumeral(builder, numeralBuffer);
                    // reset the number string buffer
                    numeralBuffer = new StringBuilder(4);
                }
                builder.append(character);
                lastCharacterIsNumeral = false;
            }
        }
        if(lastCharacterIsNumeral){
            addPaddedNumeral(builder, numeralBuffer);
        }
        return builder.toString();

    }

    private static void addPaddedNumeral(StringBuilder builder, StringBuilder numeralBuffer) {
        int numberOfZerosToPad = numberOfDigits - numeralBuffer.length();
        for (int index = 0; index < numberOfZerosToPad; index++)
            builder.append(0);
        builder.append(numeralBuffer);
    }
}