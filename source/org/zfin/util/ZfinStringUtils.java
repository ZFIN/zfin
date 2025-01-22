package org.zfin.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zfin.marker.Marker;

import java.util.*;

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

    public static boolean isEqualIgnoringWhiteSpace(String a, String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.replaceAll("\\s+", "").equals(b.replaceAll("\\s+", ""));
    }

    /**
     * Sorts a list of ZDB IDs in ascending order.
     * Alphabetical, but uses special logic for numeric parts.
     * If the date part starts with 9, then it is considered to be in the 20th century.
     * If the sequence part is needed for sorting, it is considered to be a number.
     *
     * @param zdbIDs list of ZDB IDs
     * @return sorted list of ZDB IDs
     */
    public static List<String> sortedZdbIDs(List<String> zdbIDs) {
        zdbIDs.sort((o1, o2) -> {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            List<String> parts1 = zdbIDParts(o1);
            List<String> parts2 = zdbIDParts(o2);

            //compare "ZDB" (should always be the same
            if(!parts1.get(0).equals(parts2.get(0))) {
                return parts1.get(0).compareTo(parts2.get(0));
            }

            //compare type (eg. GENE, FISH, etc.)
            if(!parts1.get(1).equals(parts2.get(1))) {
                return parts1.get(1).compareTo(parts2.get(1));
            }

            //compare date
            String date1 = parts1.get(2);
            String date2 = parts2.get(2);
            if(!date1.equals(date2)) {
                date1 = date1.startsWith("9") ? "19" + date1 : "20" + date1;
                date2 = date2.startsWith("9") ? "19" + date2 : "20" + date2;
                return date1.compareTo(date2);
            }

            //compare sequence
            String seq1 = parts1.get(3);
            String seq2 = parts2.get(3);
            if(!seq1.equals(seq2)) {
                try {
                    return Integer.parseInt(seq1) - Integer.parseInt(seq2);
                } catch (NumberFormatException e) {
                    return seq1.compareTo(seq2);
                }
            }

            //if all else fails, compare the whole string (shouldn't happen)
            return o1.compareTo(o2);
        });
        return zdbIDs;
    }

    public static String randomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    private static List<String> zdbIDParts(String zdbID) {
        return Arrays.asList(zdbID.split("-"));
    }

}



