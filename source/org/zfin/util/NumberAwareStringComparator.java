package org.zfin.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator that is aware of numeric values.
 */
public class NumberAwareStringComparator implements Comparator<String>, Serializable {

    private int numberOfDigits = 5;

    public NumberAwareStringComparator() {
    }

    public NumberAwareStringComparator(int numberOfDigits) {
        this.numberOfDigits = numberOfDigits;
    }

    @Override
    public int compare(String nameOne, String nameTwo) {
        if (nameOne == null && nameTwo == null)
            return -1;
        if (nameOne == null && nameTwo != null)
            return -1;
        if (nameOne != null && nameTwo == null)
            return +1;

        String termNameOne = addZerosToString(nameOne);
        String termNameTwo = addZerosToString(nameTwo);

        return termNameOne.compareToIgnoreCase(termNameTwo);
    }

    public String addZerosToString(String termName) {
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

    private void addPaddedNumeral(StringBuilder builder, StringBuilder numeralBuffer) {
        int numberOfZerosToPad = numberOfDigits - numeralBuffer.length();
        for (int index = 0; index < numberOfZerosToPad; index++)
            builder.append(0);
        builder.append(numeralBuffer);
    }
}