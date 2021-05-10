package org.zfin.uniquery;

import cvu.html.HTMLTokenizer;
import cvu.html.TagToken;
import cvu.html.TextToken;
import org.apache.commons.lang.StringUtils;
import org.zfin.framework.presentation.EntityPresentation;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: cmpich
 * Date: 2/2/12
 * Time: 8:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class IndexerUtil {

    /**
     * This procedure does two things:
     * (1) returns a list of all URLs in a web page summary
     * (2) updates the summary text (by removing markup tags) and title
     *
     * @param summary URL Summary
     * @throws java.io.IOException exception form HTMLTokenizer
     */
    protected static void updateSummary(WebPageSummary summary) throws IOException {
        boolean inScriptTag = false;
        StringBuilder strippedText = new StringBuilder();
        List<String> tmp_urls = new ArrayList<String>();
        summary.setBody(EntityPresentation.replaceSupTags(summary.getBody()));
        HTMLTokenizer ht = new HTMLTokenizer(new StringReader(summary.getBody()));
        for (Enumeration e = ht.getTokens(); e.hasMoreElements(); ) {
            Object obj = e.nextElement();
            if (obj instanceof TagToken) {
                TagToken tag = (TagToken) obj;
                String tagName = tag.getName();
                if (tagName != null) {
                    tagName = tagName.toLowerCase();
                }

                String new_url = null;
                if (("a").equals(tagName)) {
                    new_url = tag.getAttributes().get("href");
                } else if ("frame".equals(tagName)) {
                    new_url = tag.getAttributes().get("src");
                } else if ("title".equals(tagName) && e.hasMoreElements() && !tag.isEndTag()) {
                    // need to check all tokens until we hit a </title> tag
                    StringBuilder titleString = new StringBuilder();
                    while (e.hasMoreElements()) {
                        obj = e.nextElement();
                        if (obj instanceof TextToken) {
                            titleString.append(obj);
                        }
                        if (obj instanceof TagToken) {
                            TagToken internalTag = (TagToken) obj;
                            if (internalTag.getName().equals("title") && internalTag.isEndTag()) {
                                summary.setTitle(titleString.toString());
                                break;
                            }
                            if (internalTag.getName().equals("sup") && !internalTag.isEndTag()) {
                                titleString.append(" [");
                                obj = e.nextElement();
                                if (obj instanceof TextToken)
                                    titleString.append(((TextToken) obj).getText());
                            }
                            if (internalTag.getName().equals("sup") && internalTag.isEndTag()) {
                                titleString.append("]");
                            }
                        }
                    }

                } else if ("script".equals(tagName) && !tag.isEndTag()) {
                    inScriptTag = true;
                } else if ("script".equals(tagName) && tag.isEndTag()) {
                    inScriptTag = false;
                }


                if (new_url != null) {
                    // clean up special characters
                    new_url = StringUtils.replace(new_url, "\t", "");
                    new_url = StringUtils.replace(new_url, "\n", "");
                    new_url = StringUtils.replace(new_url, "\r", "");
                    new_url = StringUtils.replace(new_url, "&amp;", "&");

                    // remove the hostname (e.g., _quark) from cgi-bin_quark
                    if (new_url.contains("cgi-bin_")) {
                        String hostName = StringUtils.substringBetween(new_url, "cgi-bin_", "/");
                        int index = new_url.indexOf("cgi-bin_" + hostName);
                        // should assert that index != -1
                        String firstPart = new_url.substring(0, index + 7);
                        String secondPart = new_url.substring(index + 8 + hostName.length());
                        new_url = firstPart + secondPart;
                    }

                    if (new_url.startsWith("http://")) {
                        // verify we're on the same host and port
                        URL u = new URL(new_url);
                        if (u.getHost().equals(summary.getUrl().getHost()) && u.getPort() == summary.getUrl().getPort()) {
                            new_url = chopOffNamedAnchor(new_url);
                            tmp_urls.add(new_url);
                        }
                    } else if (!new_url.contains("://") && !new_url.startsWith("mailto:") && !new_url.startsWith("#") && !new_url.startsWith("javascript:")) {
                        // parse relative new_url
                        if (StringUtils.isNotEmpty(new_url)) {
                            new_url = formURL(summary.getUrl(), new_url);
                            new_url = chopOffNamedAnchor(new_url);
                            if (!tmp_urls.contains(new_url))
                                tmp_urls.add(new_url);
                        }
                    }
                }
            } else if ((obj instanceof TextToken) && !inScriptTag) {
                TextToken t = (TextToken) obj;
                String tokenText = t.getText();
                if (tokenText != null && tokenText.trim().length() > 0) {
                    strippedText.append(tokenText.trim()).append(" ");
                }
            }
        }

        summary.setText(stripSpecialCharacters(strippedText.toString()));
        summary.setUrls(new String[tmp_urls.size()]);
        tmp_urls.toArray(summary.getUrls());
    }


    public static String stripSpecialCharacters(String text) {
        text = text.replaceAll("&nbsp;", " ");
        return text;
    }


    public static String chopOffNamedAnchor(String url) {
        int pos = url.indexOf("#");
        if (pos == -1)
            return url;
        else
            return url.substring(0, pos);
    }


    // converts relative URL to absolute URL
    public static String formURL(URL origURL, String newURL) {
        StringBuilder base = new StringBuilder(origURL.getProtocol());
        base.append("://").append(origURL.getHost());
        if (origURL.getPort() != -1) {
            base.append(":").append(origURL.getPort());
        }


        // strip off single quotes because parser seems to leave them on
        if (newURL.startsWith("'")) {
            newURL = newURL.substring(1);
        }
        if (newURL.endsWith("'")) {
            newURL = newURL.substring(0, newURL.length() - 1);
        }

        if (newURL.startsWith("/")) {
            base.append(newURL);
        } else if (newURL.startsWith("..")) {
            origURL.getFile();
        } else {
            String file = origURL.getFile();
            int pos = file.lastIndexOf("/");
            if (pos != -1)
                file = file.substring(0, pos);

            while (newURL.startsWith("../")) {
                pos = file.lastIndexOf("/");
                file = file.substring(0, pos);
                newURL = newURL.substring(3);
            }

            base.append(file).append("/").append(newURL);
        }

        return base.toString();
    }


}
