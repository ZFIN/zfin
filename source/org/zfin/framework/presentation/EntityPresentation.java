package org.zfin.framework.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.wiki.service.AntibodyWikiWebService;

/**
 * ToDo: Please add documentation for this class.
 */
public abstract class EntityPresentation {

    private static final Logger logger = LogManager.getLogger(EntityPresentation.class);
    protected static final String NONGENEDOMMARKER = "nongenedommarker";
    public static final String WITHDRAWN = " <i class=\"warning-icon\" title=\"Withdrawn\"></i>";
    public static final String ZFIN_JUMP_URL = "/";
    public static String domain;

    /**
     * Uses ZfinProperties to get webdriver link information
     *
     * @return first bit of webdriver links
     */
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

    public static String getViewLink(String zdbID, String linkText) {
        return getViewLink(zdbID, linkText, null, null);
    }

    public static String getViewLink(String zdbID, String linkText, String name) {
        return getViewLink(zdbID, linkText, name, null);
    }

    public static String getViewLink(String zdbID, String linkText, String name, String cssClassName) {
        return getViewLink(zdbID, linkText, name, cssClassName, null);
    }

    public static String getViewLink(String zdbID, String linkText, String name, String cssClassName, String domID) {
        StringBuilder sb = getViewHyperlinkStart();
        sb.append(zdbID);
        sb.append("\"");
        if (domID != null) {
            sb.append(" id=\"");
            sb.append(domID);
            sb.append("\"");
        }
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
        sb.append(linkText);
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
        if (zdbID.contains("CONSTRCT")) {
            sb.append(name);
        } else {
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

    public static String getPopupLink(String url) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"");
        sb.append(url);
        sb.append("\"");
        sb.append(" class=\"popup-link data-popup-link\"");
        sb.append(" title=\"");
        sb.append("\">");
        sb.append("</a>");
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

    protected static String getSpanTagWithID(String cssClassName, String title, String name, String id) {
        return "<span class=\"" + cssClassName + "\" title=\"" + replaceSupTags(title) + "\" id=\"" + id + "\">" + name + "</span>";
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
