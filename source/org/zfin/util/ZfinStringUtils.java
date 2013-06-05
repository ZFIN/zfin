package org.zfin.util;

import org.apache.commons.lang3.text.translate.NumericEntityEscaper;
import java.util.ArrayList;
import java.util.List;

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

    public static List<Integer> detectWhiteSpaces(String line) {
        return detectWhiteSpaces(line, 0);
    }

    public static List<Integer> detectWhiteSpaces(String line, int startPosition) {
        if (line == null)
            return null;
        List<Integer> whiteSpaceSet = new ArrayList<Integer>();
        String[] tokens = line.split(" ");
        int accumulatedPosition = startPosition;
        int index = 0;
        for (String token : tokens) {
            if (index++ == tokens.length - 1)
                break;
            accumulatedPosition += token.length() + 1;
            whiteSpaceSet.add(accumulatedPosition - 1);
        }
        if (line.substring(line.length() - 1).equals(" "))
            whiteSpaceSet.add(line.length() - 1);
        return whiteSpaceSet;
    }

    // useful for cases such as FB case 8817,Fish Search: Fish view page geno/genox id list parser error
    public static String cleanUpConcatenatedZDBIdsDelimitedByComma (String concatenatedZDBIdsDelimitedByComma) {
        if (concatenatedZDBIdsDelimitedByComma == null) {
            return null;
        }
        String delimiter = ",";
        if (concatenatedZDBIdsDelimitedByComma.indexOf(delimiter) > 0) {
            String[] concatenatedIDsPieces = concatenatedZDBIdsDelimitedByComma.split(delimiter);
            for(int i =0; i < concatenatedIDsPieces.length ; i++) {
                  String newStr = concatenatedIDsPieces[i].trim();
                  if (newStr.isEmpty() || newStr.indexOf("ZDB") < 0)  {
                      continue;
                  }

                  if (!newStr.startsWith("ZDB")) {
                        String[] piecesZDBId = newStr.split("ZDB");
                        if (i == 0) {
                              concatenatedZDBIdsDelimitedByComma = "ZDB" + piecesZDBId[1];
                        }   else {
                              concatenatedZDBIdsDelimitedByComma = concatenatedZDBIdsDelimitedByComma + delimiter + "ZDB" + piecesZDBId[1];
                        }
                  }   else {
                        if (i == 0) {
                              concatenatedZDBIdsDelimitedByComma = newStr;
                        }   else {
                              concatenatedZDBIdsDelimitedByComma = concatenatedZDBIdsDelimitedByComma + delimiter + newStr;
                        }
                  }

            }
        }
        return concatenatedZDBIdsDelimitedByComma;
    }

    /**
     * Escape characters into character entities if they are outside of the range supported by informix
     * @param name String to escape
     * @return escaped String
     */
    public static String escapeHighUnicode(String name) {

        if (name ==null)
            return null;

        return NumericEntityEscaper.above(0xFF).translate(name);

    }

}



