package org.zfin.util;

/**
 * Created by IntelliJ IDEA.
 * User: Christian Pich
 * Date: Mar 29, 2008
 * Time: 1:56:03 PM
 * To change this template use File | Settings | File Templates.
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
    public static String hightlightMatchHTML(String text, String highlightString, boolean caseSensitive) {
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
}
