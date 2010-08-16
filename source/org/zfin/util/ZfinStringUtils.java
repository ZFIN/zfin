package org.zfin.util;

/**
 * Utilities that are not in StringUtils.
 */
public class ZfinStringUtils {
    public static final String ELLIPSIS = "...";

    /**
     * Replace a given string replace in a given string text with a string with.
     * <p/>
     * Example:
     * StringUtils.replace("AbCDeF", "abCD", gWE)       = "gweef"
     *
     * @param text    string
     * @param replace string
     * @param with    string
     * @return string
     */
    public static String replaceCaseInsensitive(String text, String replace, String with) {
        if (text == null)
            return null;

        if (replace == null)
            return text;

        String replaceLowerCase = replace.toLowerCase();

        // get all the case insensitive matches
        String textLowerCase = text.toLowerCase();
        int position = textLowerCase.indexOf(replaceLowerCase);


        char[] characters = replace.toCharArray();
        for (char character : characters) {
            //        if (Character.isLowerCase(character))
//                char lowerCase = Character.toLowerCase(character);
            //          text = StringUtils.replaceChars(text, lowerCase, )
        }
        return null;
    }

    /**
     * Truncate a given text to the maximum of maxLength characters and add an ellipsis (...) at the end
     * to indicate the omission of text.
     *
     * @param originalText string
     * @param maxLength    text
     * @return truncated text
     */
    public static String getTruncatedString(String originalText, int maxLength) {
        if (originalText == null)
            return null;

        if (maxLength < 1)
            throw new IllegalStateException("The maximum length of the text to be truncated must be greater than 0!");

        int textLength = originalText.length();
        if (textLength < maxLength)
            return originalText;

        String truncatedText = originalText.substring(0, maxLength - 1);
        int indexOfLastWhiteSpace = truncatedText.lastIndexOf(" ");
        // truncated text has white space
        if (indexOfLastWhiteSpace > -1) {
            truncatedText = truncatedText.substring(0, indexOfLastWhiteSpace);
            truncatedText += " ";
        }
        truncatedText += ELLIPSIS;
        return truncatedText;
    }

    public static String getHtmlTableFromQueryString(String text) {
        if (text == null)
            return null;
        String[] querParameters = text.split("&");
        StringBuffer buffer = new StringBuffer(querParameters.length * 10);
        buffer.append("<table>");
        for (String keyValue : querParameters) {
            buffer.append("<tr>");
            buffer.append("<td>");
            String[] keyValuesPair = keyValue.split("=");
            buffer.append(keyValuesPair[0]);
            buffer.append("</td>");
            buffer.append("<td>");
            if (keyValuesPair.length == 2)
                buffer.append(keyValuesPair[1]);
            buffer.append("</td>");
            buffer.append("</tr>");
        }
        buffer.append("</table>");
        return buffer.toString();
    }
}
