package org.zfin.util;

import org.zfin.framework.ZfinSimpleTokenizer;

/**
 */
public class HighlightUtil {

    /**
     * Example:
     * anti-DLX3b -> anti-<b>DLX3</b>b
     *
     * @param text            String
     * @param highlightString String
     * @param caseSensitive   boolean
     * @return String
     */
    public static String highlightMatchHTML(String text, String highlightString, boolean caseSensitive) {
        if (text == null)
            return null;

        if (highlightString == null)
            return text;

        if (caseSensitive) {
            return null;
        }
        String textLowerCase = text.toLowerCase();
        String highlighterStringLowerCase = highlightString.toLowerCase();
        int firstMatch = textLowerCase.indexOf(highlighterStringLowerCase);
        if (firstMatch == -1)
            return text;
        StringBuilder builder = new StringBuilder();
        builder.append(text.substring(0, firstMatch));
        builder.append("<b>");
        builder.append(text.substring(firstMatch, firstMatch + highlightString.length()));
        builder.append("</b>");
        builder.append(text.substring(firstMatch + highlightString.length()));
        return builder.toString();
    }

    /**
     * Ignore colons and dashes
     *
     * @param text
     * @param highlightString
     * @return
     */
    public static String highlightSmartMatchHTML(String text, String highlightString, boolean caseSensitive, boolean smart) {
        if (text == null)
            return null;

        if (highlightString == null)
            return text;

        if (caseSensitive) {
            return null;
        }
        String textLowerCase = text.toLowerCase();
        String highlighterStringLowerCase = highlightString.toLowerCase();
        // remove special characters
        highlighterStringLowerCase = ZfinSimpleTokenizer.getSpecialCharacterReplacement(highlighterStringLowerCase);
        if (smart) {
            StringBuilder newStringBuilder = new StringBuilder(text.length() + 7);
            int startOfMatching = getMatchingStartIndex(text, highlighterStringLowerCase);
            int endOfMatching = getMatchingEndIndex(text, highlighterStringLowerCase, startOfMatching);
            newStringBuilder.append(text.substring(0, startOfMatching));
            newStringBuilder.append("<b>");
            newStringBuilder.append(text.substring(startOfMatching, endOfMatching + 1));
            newStringBuilder.append("</b>");
            newStringBuilder.append(text.substring(endOfMatching + 1));
            return newStringBuilder.toString();
        }

        int firstMatch = textLowerCase.indexOf(highlighterStringLowerCase);
        if (firstMatch == -1)
            return text;
        StringBuilder builder = new StringBuilder();
        builder.append(text.substring(0, firstMatch));
        builder.append("<b>");
        builder.append(text.substring(firstMatch, firstMatch + highlightString.length()));
        builder.append("</b>");
        builder.append(text.substring(firstMatch + highlightString.length()));
        return builder.toString();
    }

    private static int getMatchingEndIndex(String text, String highlightString, int startOfMatching) {
        int lengthOfMatchingString = highlightString.length();
        int index = startOfMatching;
        char[] chars = text.toCharArray();
        int matchingCharacterIndex = 0;
        for (; index < text.length(); index++) {
            char character = chars[index];
            String charString = String.valueOf(character);
            if (!(charString.equals("-")) && !(charString.equals(":")))
                matchingCharacterIndex++;
            if (matchingCharacterIndex == lengthOfMatchingString)
                return index;
        }
        return -1;
    }

    private static int getMatchingStartIndex(String text, String highlightString) {
        String textLowerCase = text.toLowerCase();
        textLowerCase = ZfinSimpleTokenizer.getSpecialCharacterReplacement(textLowerCase);
        String highlighterStringLowerCase = highlightString.toLowerCase();
        int startOfMatch = textLowerCase.indexOf(highlighterStringLowerCase);
        int index = 0;
        int fullIndexCounter = 0;
        for (char character : text.toCharArray()) {
            String charString = String.valueOf(character);
            if (index == startOfMatch && !isIgnoredCharacter(charString)) {
                return fullIndexCounter;
            } else {
                if (!isIgnoredCharacter(charString))
                    index++;
            }
            fullIndexCounter++;
        }
        return -1;
    }

    private static boolean isIgnoredCharacter(String charString) {
        return charString.equals("-") || charString.equals(":");
    }

}
