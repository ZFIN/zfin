package org.zfin.framework.presentation;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Class that is called from JSP through a function call.
 */
public class Functions {

    /**
     * Escape characters to valid HTML code
     *
     * @param string character string
     * @return String
     */
    public static String escapeJavaScript(String string) {
        if (string.indexOf("\r\n") > -1)
            string = string.replaceAll("\r\n", "<br/>");
        if (string.indexOf("\n") > -1)
            string = string.replaceAll("\n", "<br/>");

        return StringEscapeUtils.escapeJavaScript(string);
    }

    /**
     * Escape characters to valid HTML code
     *
     * @param string character string
     * @return String
     */
    public static String escapeHtml(String string) {
        string = StringEscapeUtils.escapeHtml(string);
        if (string.indexOf("\r\n") > -1)
            string = string.replaceAll("\r\n", "<br/>");
        if (string.indexOf("\n") > -1)
            string = string.replaceAll("\n", "<br/>");

        return string;
    }
}
