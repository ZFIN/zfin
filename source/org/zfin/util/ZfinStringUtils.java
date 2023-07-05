package org.zfin.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zfin.marker.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities that are not in StringUtils.
 */
public class ZfinStringUtils {
    public static final String ELLIPSIS = "...";

    /**
     * Truncate a given text to the maximum of maxLength characters and add an ellipsis (...) at the end
     * to indicate the omission of text.
     *
     * @param originalText string
     * @param maxLength    text
     * @return truncated text
     */
    public static String getTruncatedString(String originalText, int maxLength) {
        if (originalText == null) {
            return null;
        }

        if (maxLength < 1) {
            throw new IllegalStateException("The maximum length of the text to be truncated must be greater than 0!");
        }

        int textLength = originalText.length();
        if (textLength < maxLength) {
            return originalText;
        }

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
        if (text == null) {
            return null;
        }
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
            if (keyValuesPair.length == 2) {
                buffer.append(keyValuesPair[1]);
            }
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
        if (line == null) {
            return null;
        }
        List<Integer> whiteSpaceSet = new ArrayList<Integer>();
        String[] tokens = line.split(" ");
        int accumulatedPosition = startPosition;
        int index = 0;
        for (String token : tokens) {
            if (index++ == tokens.length - 1) {
                break;
            }
            accumulatedPosition += token.length() + 1;
            whiteSpaceSet.add(accumulatedPosition - 1);
        }
        if (line.substring(line.length() - 1).equals(" ")) {
            whiteSpaceSet.add(line.length() - 1);
        }
        return whiteSpaceSet;
    }

    // useful for cases such as FB case 8817,Fish Search: Fish view page geno/genox id list parser error
    public static String cleanUpConcatenatedZDBIdsDelimitedByComma(String concatenatedZDBIdsDelimitedByComma) {
        if (concatenatedZDBIdsDelimitedByComma == null) {
            return null;
        }
        String delimiter = ",";
        if (concatenatedZDBIdsDelimitedByComma.indexOf(delimiter) > 0) {
            String[] concatenatedIDsPieces = concatenatedZDBIdsDelimitedByComma.split(delimiter);
            for (int i = 0; i < concatenatedIDsPieces.length; i++) {
                String newStr = concatenatedIDsPieces[i].trim();
                if (newStr.isEmpty() || newStr.indexOf("ZDB") < 0) {
                    continue;
                }

                if (!newStr.startsWith("ZDB")) {
                    String[] piecesZDBId = newStr.split("ZDB");
                    if (i == 0) {
                        concatenatedZDBIdsDelimitedByComma = "ZDB" + piecesZDBId[1];
                    } else {
                        concatenatedZDBIdsDelimitedByComma = concatenatedZDBIdsDelimitedByComma + delimiter + "ZDB" + piecesZDBId[1];
                    }
                } else {
                    if (i == 0) {
                        concatenatedZDBIdsDelimitedByComma = newStr;
                    } else {
                        concatenatedZDBIdsDelimitedByComma = concatenatedZDBIdsDelimitedByComma + delimiter + newStr;
                    }
                }

            }
        }
        return concatenatedZDBIdsDelimitedByComma;
    }

    public static boolean isValidNucleotideSequence(final String sequenceString) {
        return isValidNucleotideSequence(sequenceString, null);
    }

    public static boolean isValidNucleotideSequence(final String sequenceString, Marker.Type type) {
        if (sequenceString == null) {
            return false;
        }

        String bases = "ATGC";
        if (type != null && type == Marker.Type.TALEN) {
            bases += "R";
        }
        String pattern = String.format("^[%s]+$", bases);
        return sequenceString.matches(pattern);
    }

    public static String removeHtmlTags(final String string) {
        return string.replaceAll("<.*?>", "");
    }

    public static String objectToJson(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
        }
        return null;
    }

    public static String getCamelCase(String text) {
        String[] words = text.split("[\\W_]+");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (i == 0) {
                word = word.isEmpty() ? word : word.toLowerCase();
            } else {
                word = word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
            }
            builder.append(word);
        }
        return builder.toString();
    }
}



