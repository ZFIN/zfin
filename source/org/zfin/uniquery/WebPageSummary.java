package org.zfin.uniquery;

import org.apache.commons.lang.StringUtils;
import org.zfin.uniquery.categories.SiteSearchCategories;

import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convenience class to hold info for the indexer.
 */
public class WebPageSummary {

    private URL url;
    private String body;
    private String text;
    private String title;
    // set this absolute url when indexing non-zfin sites, such as the wiki.
    private String urlName;
    // array of links on this page
    private String[] urls;
    private Type type;

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getUrls() {
        return urls;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    public String getUrlName() {
        if (urlName != null)
            return urlName;
        if (!StringUtils.isEmpty(url.getFile()))
            return url.getFile();
        else
            return url.toString();
    }

    public void setUrlName(String urlName) {
        this.urlName = urlName;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    private String getPrefixTitle() {
        List<SearchCategory> categories = SiteSearchCategories.getSearchCategories();
        for (SearchCategory category : categories) {
            List<UrlPattern> urlPatterns = category.getUrlPatterns();
            for (UrlPattern urlPattern : urlPatterns) {
                Pattern regExpPattern = Pattern.compile(urlPattern.getPattern());
                Matcher matcher = regExpPattern.matcher(url.toString());
                if (matcher.find()) {
                    return urlPattern.getTitlePrefix();
                }
            }
        }
        return "";
    }

    /**
     * Remove the following words from the title if they exists:
     * 1) ZFIN View:
     * 2) ZFIN:
     * If no title is found 'Untitled' is used.
     * A prefix is added according to the specification in the site search category file.
     *
     * @return modified title
     */
    public String getAdjustedTitle() {
        String modifiedTitle;
        if (title != null && title.length() > 0) {
            modifiedTitle = title.trim();
            if (modifiedTitle.startsWith("ZFIN View")) {
                modifiedTitle = modifiedTitle.substring(10).trim();
            }
            if (modifiedTitle.startsWith("ZFIN")) {
                modifiedTitle = modifiedTitle.substring(4).trim();
            }
            if (modifiedTitle.startsWith(":")) {
                modifiedTitle = modifiedTitle.substring(1);
            }
        } else {
            modifiedTitle = "Untitled";
        }
        String prefixTitle = getPrefixTitle();
        if (prefixTitle == null)
            return modifiedTitle;
        else
            return prefixTitle + modifiedTitle;
    }


    enum Type {
        ZFIN, WIKI
    }
}
