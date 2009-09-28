package org.zfin.framework.presentation;

import org.apache.log4j.Logger;
import org.zfin.properties.ZfinProperties;
import org.zfin.wiki.AntibodyWikiWebService;
import org.zfin.wiki.WikiLoginException;

/**
 * ToDo: Please add documentation for this class.
 */
public abstract class EntityPresentation {

    protected static final Logger logger = Logger.getLogger(EntityPresentation.class);
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

    protected static String getWikiLink(String uri, String zdbID, String abbreviation) {
        return getWikiLink(uri, zdbID, abbreviation, null);
    }

    protected static String getWikiLink(String uri, String zdbID, String abbreviation, String name) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append(abbreviation);
            sb.append("|");
            sb.append("http://");
            sb.append(AntibodyWikiWebService.getInstance().getDomainName());
            sb.append("/");
            sb.append(uri);
            sb.append(zdbID);
            if (name != null) {
                sb.append("|");
                sb.append(name);
            }
            sb.append("]");
            return sb.toString();
        } catch (WikiLoginException e) {
            logger.error(e);
            return null;
        }
    }

    protected static String getTomcatLink(String uri, String zdbID, String abbreviation, String name) {
        StringBuilder sb = getTomcatHyperLinkStart();
        sb.append(uri);
        sb.append(zdbID);
        sb.append("\"");
        sb.append(" name=\"");
        if (name != null)
            sb.append(name);
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

    protected static String getWebdriverUrl(String uri, String zdbID) {
        StringBuilder sb = new StringBuilder("/");
        sb.append(ZfinProperties.getWebDriver());
        sb.append(uri);
        sb.append(zdbID);
        return sb.toString();
    }

    protected static String getWebdriverStartTag(String uri, String zdbID) {
        StringBuilder sb = new StringBuilder("<a href=\"");
        sb.append(getWebdriverUrl(uri, zdbID));
        sb.append("\">");
        return sb.toString();
    }

    /* I made this method public because it won't generally be necessary for
     * underlying entities to override this method */
    public static String getLinkEndTag() {
        return "</a>";
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
