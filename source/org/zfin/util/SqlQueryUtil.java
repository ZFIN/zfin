package org.zfin.util;

import org.apache.commons.lang.StringUtils;

/**
 * Utility class for convenience methods to deal with SQL 92 query strings.
 */
public class SqlQueryUtil implements SqlQueryKeywords {

    private static String newline = System.getProperty("line.separator");
    protected static String true_newline = newline;


    public static String getHumanReadableQueryString(String query) {
        if (StringUtils.isEmpty(query))
            return null;
        StringBuilder builder = new StringBuilder();
        String[] tokens = query.toString().split(" ");
        for (String token : tokens) {
            if (StringUtils.isEmpty(token))
                continue;
            if (isKeyWord(token)) {
                builder.append(newline);
            }

            // strip off newlines and add them again.
            // to enable testing!
            if (token.endsWith(true_newline)) {
                builder.append(token.replace(true_newline, ""));
            } else
                builder.append(token);

            if (token.endsWith(","))
                builder.append(newline);
            else
                builder.append(" ");
        }

        return builder.toString();
    }

    private static boolean isKeyWord(String token) {
        if (StringUtils.isEmpty(token))
            return false;
        String modifiedToken = token.trim().toUpperCase();
        if (modifiedToken.equals(INSERT))
            return true;
        if (modifiedToken.equals(UPDATE))
            return true;
        if (modifiedToken.equals(AND))
            return true;
        if (modifiedToken.equals(WHERE))
            return true;
        if (modifiedToken.equals(ORDER_BY))
            return true;
        if (modifiedToken.equals(GROUP_BY))
            return true;
        if (modifiedToken.equals(DELETE))
            return true;
        if (modifiedToken.equals(EXISTS))
            return true;
        if (modifiedToken.equals(TABLE))
            return true;
        if (modifiedToken.equals(CREATE))
            return true;
        if (modifiedToken.equals("("))
            return true;
        if (modifiedToken.equals(","))
            return true;

        return false;
    }


    // Only meant for testing

    public static String getHumanReadableQueryString(String query, String newlineChar) {
        newline = newlineChar;
        return getHumanReadableQueryString(query);
    }
}
