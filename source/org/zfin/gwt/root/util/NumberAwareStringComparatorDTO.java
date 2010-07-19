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

        // not in the jme emulation library
        // a patch can be found here: http://code.google.com/p/google-web-toolkit/source/browse/releases/1.5/user/super/com/google/gwt/emul/java/lang/StringBuilder.java?r=2940
        // from here: http://sinnema313.wordpress.com/2008/11/16/performance-tuning-a-gwt-application/
        // and here: http://development.lombardi.com/?p=1073 (uses gwt2 stringbuilder)
        // string += may be just as fast
        String builder = new String();
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
                builder += character;
                lastCharacterIsNumeral = false;
            }
        }
        if(lastCharacterIsNumeral){
            addPaddedNumeral(builder, numeralBuffer);
        }
        return builder;

    }

    private static void addPaddedNumeral(String builder, StringBuilder numeralBuffer) {
        int numberOfZerosToPad = numberOfDigits - numeralBuffer.length();
        for (int index = 0; index < numberOfZerosToPad; index++)
            builder += 0;
        builder += numeralBuffer;
    }
}