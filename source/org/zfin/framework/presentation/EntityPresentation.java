package org.zfin.framework.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.wiki.service.AntibodyWikiWebService;

/**
 * ToDo: Please add documentation for this class.
 */
public abstract class EntityPresentation {

    private static final Logger logger = Logger.getLogger(EntityPresentation.class);
    protected static final String NONGENEDOMMARKER = "nongenedommarker";
    protected static final String WITHDRAWN_PREFIX = "&nbsp;<img src='/images/warning-noborder.gif' title='Withdrawn' alt='Withdrawn' width='20' height='20'";
    public static final String WITHDRAWN = WITHDRAWN_PREFIX + " align='top' />";
    public static final String ZFIN_JUMP_URL = "/";
    public static String domain;
    public static final String CURATION_URI = "?MIval=aa-curation.apg&";

    /**
     * Uses ZfinProperties to get webdriver link information
     *
     * @return first bit of webdriver links
     */
    protected static StringBuilder getWebdriverHyperLinkStart() {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"/");
        sb.append(ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value());
        return sb;
    }

    protected static StringBuilder getTomcatHyperLinkStart() {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"" + getTomcatUrlStart());
        return sb;
    }

    protected static StringBuilder getViewHyperlinkStart() {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"/");
        return sb;
    }

    protected static String getTomcatUrlStart() {
        return "/action/";
    }

    protected static String getWikiLink(String uri, String zdbID, String abbreviation) {
        return getWikiLink(uri, zdbID, abbreviation, null);
    }

    protected static String getWikiLink(String uri, String zdbID, String abbreviation, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"");
        sb.append("http://zfin.org");
        sb.append("/");
        if (StringUtils.isNotEmpty(uri)) {
            sb.append(uri);
            sb.append("/");
        }
        sb.append(zdbID);
        sb.append("\">");
        // needed so the <em> is not escaped. Hack that needs to be fixed by generic encoding consideration
        // see antibody wiki page generation...
        if (zdbID.contains("ZDB-GENE-"))
            sb.append(abbreviation);
        else
            sb.append(AntibodyWikiWebService.getEncodedString(abbreviation));
        sb.append("</a>");
        return sb.toString();
    }

    protected static String getExternalWikiLink(String link, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(name);
        sb.append("|");
        sb.append(link);
        sb.append("]");
        return sb.toString();
    }

    public static String getViewLink(String zdbID, String abbreviation, String name, String cssClassName) {
        StringBuilder sb = getViewHyperlinkStart();
        sb.append(zdbID);
        sb.append("\"");
        if (name != null) {
            sb.append(" title=\"");
            sb.append(name);
            sb.append("\"");
        }
        if (cssClassName != null) {
            sb.append(" class=\"");
            sb.append(cssClassName);
            sb.append("\" ");
        }
        sb.append(">");
        sb.append(abbreviation);
        sb.append("</a>");
        return sb.toString();
    }

    public static String getViewLinkWithID(String zdbID, String linkContent, String domID) {
        StringBuilder sb = getViewHyperlinkStart();
        sb.append(zdbID);
        sb.append("\"");

        sb.append(" id=\"");
        sb.append(domID);
        sb.append("\" ");

        sb.append(">");
        sb.append(linkContent);
        sb.append("</a>");
        return sb.toString();
    }

    protected static String getTomcatLink(String uri, String zdbID, String abbreviation, String name) {
        StringBuilder sb = getTomcatHyperLinkStart();
        sb.append(uri);
        sb.append(zdbID);
        sb.append("\"");
        if (name != null) {
            sb.append(" name=\"");
            sb.append(name);
            sb.append("\"");
        }
        sb.append(" id='" + zdbID + "'>");
        if (zdbID.contains("CONSTRCT")){
            sb.append(name);
        }
        else {
            sb.append(abbreviation);
        }
        sb.append("</a>");
        return sb.toString();
    }

    protected static String getTomcatLink(String uri, String zdbID, String abbreviation, String name, String idName) {
        StringBuilder sb = getTomcatHyperLinkStart();
        sb.append(uri);
        sb.append(zdbID);
        sb.append("\"");
        if (name != null) {
            sb.append(" name=\"");
            sb.append(name);
            sb.append("\"");
        }
        sb.append(" id='" + idName + "'>");
        sb.append(abbreviation);
        sb.append("</a>");
        return sb.toString();
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

    protected static String getTomcatLinkWithTitle(String uri, String zdbID, String abbreviation, String name, String title) {
        StringBuilder sb = getTomcatHyperLinkStart();
        sb.append(uri);
        sb.append(zdbID);
        sb.append("\"");
        sb.append(" name=\"");
        if (name != null)
            sb.append(name);
        sb.append("\"");
        sb.append(" title=\"");
        if (title != null)
            sb.append(title);
        sb.append("\">");
        sb.append(abbreviation);
        sb.append("</a>");
        return sb.toString();
    }

    public static String getTomcatPopupLink(String uri, String zdbID, String title) {
        StringBuilder sb = getTomcatHyperLinkStart();
        sb.append(uri);
        sb.append(zdbID);
        sb.append("\"");
        sb.append(" class=\"popup-link data-popup-link\"");
        sb.append(" title=\"");
        if (title != null)
            sb.append(title);
        sb.append("\">");
        sb.append("</a>");
        return sb.toString();
    }

    public static String getWebdriverLink(String uri, String zdbID, String abbreviation) {
        StringBuilder sb = getWebdriverHyperLinkStart();
        sb.append(uri);
        sb.append(zdbID);
        sb.append("\">");
        sb.append(abbreviation);
        sb.append("</a>");
        return sb.toString();
    }

    protected static String getWebdriverLink(String uri, String zdbID, String abbreviation, String idName) {
        StringBuilder sb = getWebdriverHyperLinkStart();
        sb.append(uri);
        sb.append(zdbID);
        sb.append("\" id='" + idName + "'>");
        sb.append(abbreviation);
        sb.append("</a>");
        return sb.toString();
    }

    protected static String getWebdriverUrl(String uri, String zdbID) {
        StringBuilder sb = new StringBuilder("/");
        sb.append(ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value());
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

    protected static String getViewStartTag(String zdbID) {
        StringBuilder sb = getViewHyperlinkStart();
        sb.append(zdbID);
        sb.append("\">");
        return sb.toString();
    }

    public static String getJumpToLink(String zdbID) {
        StringBuilder sb = new StringBuilder();
        sb.append(ZfinPropertiesEnum.NON_SECURE_HTTP.value());
        sb.append(ZfinPropertiesEnum.DOMAIN_NAME.value());
        sb.append("/");
        sb.append(zdbID);
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
        sb.append(replaceSupTags(title));
        sb.append("\">");
        sb.append(name);
        sb.append("</span>");
        return sb.toString();
    }

    public static String replaceSupTags(String title) {
        title = title.replaceAll("<sup>", " [");
        title = title.replaceAll("</sup>", "]");
        return title;
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
