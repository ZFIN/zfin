package org.zfin.framework.presentation;

import org.zfin.properties.ZfinProperties;

/**
 * ToDo: Please add documentation for this class.
 */
public abstract class EntityPresentation {

    protected static final String NONGENEDOMMARKER = "nongenedommarker";

    /**
     * Uses ZfinProperties to get webdriver link information
     *
     * @return first bit of webdriver links
     */
    private static StringBuilder getWebdriverHyperLinkStart() {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"/");
        sb.append(ZfinProperties.getWebDriver());
        return sb;
    }

    private static StringBuilder getTomcatHyperLinkStart() {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"/action/");
        return sb;
    }

    protected static String getTomcatLink(String uri, String zdbID, String abbreviation) {
        StringBuilder sb = getTomcatHyperLinkStart();
        sb.append(uri);
        sb.append(zdbID);
        sb.append("\">");
        sb.append(abbreviation);
        sb.append("</a>");
        return sb.toString();
    }

    protected static String getWebdriverLink(String uri, String zdbID, String abbreviation) {
        StringBuilder sb = getWebdriverHyperLinkStart();
        sb.append(uri);
        sb.append(zdbID);
        sb.append("\">");
        sb.append(abbreviation);
        sb.append("</a>");
        return sb.toString();
    }

    protected static String getSpanTag(String cssClassName, String title, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("<span class=\"");
        sb.append(cssClassName);
        sb.append("\" title=\"");
        sb.append(title);
        sb.append("\">");
        sb.append(name);
        sb.append("</span>");
        return sb.toString();
    }

    public static String getGeneralHyperLink(String url, String linkName) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"");
        sb.append(url);
        sb.append("\">");
        sb.append(linkName);
        sb.append("</a>");
        return sb.toString();
    }


}
