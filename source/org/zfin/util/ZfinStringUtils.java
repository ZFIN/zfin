package org.zfin.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if (sequenceString == null) {
            return false;
        }

        Pattern pattern = Pattern.compile("\\s|[^ATGC]");
        Matcher matcher = pattern.matcher(sequenceString);
        return !matcher.find();
    }

    public static boolean isZdbId(final String txt) {
        // generated with
        // http://txt2re.com/index-java.php3?s=ZDB-GENE-160119-12&-7&-28&4&-29&2&-30&9

        String re1 = "(ZDB)";    // Word 1
        String re2 = "(-)";    // Any Single Character 1
        String re3 = "((?:[a-z][a-z]+))";    // Word 2
        String re4 = "(-)";    // Any Single Character 2
        String re5 = "((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3}))(?:[0]?[1-9]|[1][012])(?:(?:[0-2]?\\d{1})|(?:[3][01]{1})))(?![\\d])";    // YYYYMMDD 1
        String re6 = "(-)";    // Any Single Character 3
        String re7 = "(\\d+)";    // Integer Number 1

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(txt);
        return m.find();
    }

    public static String removeHtmlTags(final String string) {
        return string.replaceAll("<.*?>", "");
    }

}



