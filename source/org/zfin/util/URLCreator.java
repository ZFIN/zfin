package org.zfin.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to create a URL by adding request parameters
 * (name-value pairs). It uses URLEncoder to ensure proper
 * encoding.
 */
public class URLCreator {

    public static final String AMPERSAND = "&";
    public static final String QUESTION_MARK = "?";
    private StringBuilder url = new StringBuilder();
    // linked map to retain the order the parameters were originally in.
    private String urlWithoutParameters;
    List<NameValuePair> nameValuePairs = new ArrayList<>();


    private static final Log LOG = LogFactory.getLog(URLCreator.class);

    public URLCreator(String url) {
        if (url == null) {
            throw new NullPointerException("No URL provided in constructor");
        }
        this.url.append(url);
        createMapFromUrl();
    }

    /**
     * Add a new name-value pair to the URL.
     * If the name is already being used the name-value is not added.
     *
     * @param name  request parameter
     * @param value value parameter
     * @return if name already exists in the hashmap do not add it -> false
     */
    public boolean addNameValuePair(String name, String value) {
        NameValuePair nameValuePair = new BasicNameValuePair(name, value);
        if (!nameValuePairs.contains(nameValuePair)) {
            nameValuePairs.add(nameValuePair);
        }
        return true;
    }

    public void replaceNameValuePair(String name, String value) {
        NameValuePair nameValuePair = new BasicNameValuePair(name, value);
        removeNameValuePair(nameValuePair.getName());
        nameValuePairs.add(nameValuePair);
    }

    /**
     * Retrieve the full URL if fullURL=true
     * otherwise return query string only.
     * The string is URL-encoded.
     *
     * @return full url or query string
     */
    public String getFullURLPlusSeparator() {
        String url = getURL();
        if (nameValuePairs != null && nameValuePairs.size() >= 1) {
            return url + AMPERSAND;
        } else {
            if (url.endsWith(QUESTION_MARK))
                return url;
            else
                return url + QUESTION_MARK;
        }
    }

    /**
     * Retrieve the full URL if fullURL=true
     * otherwise return query string only.
     * The string is URL-encoded.
     *
     * @return full url or query string
     */
    public String getURL() {
        String queryString = URLEncodedUtils.format(nameValuePairs, StandardCharsets.UTF_8);

        if (StringUtils.isEmpty(queryString)) {
            return urlWithoutParameters;
        } else {
            return urlWithoutParameters + QUESTION_MARK + URLEncodedUtils.format(nameValuePairs, StandardCharsets.UTF_8);
        }


    }

    public String getPath() {
        return urlWithoutParameters;
    }

    /**
     * Removes a parameter and its values from the url query string.
     *
     * @param name parameter name
     * @return return true if parameter was found
     */
    public boolean removeNameValuePair(String name) {
        if (StringUtils.isEmpty(name)) {
            return true;
        }

        for (NameValuePair nameValuePair : nameValuePairs) {
            if (StringUtils.equals(name, nameValuePair.getName())) {
                nameValuePairs.remove(nameValuePair);
                return true;
            }
        }
        return false;
    }


    public boolean removeNameValuePair(String name, String value) {
        return removeNameValuePair(new BasicNameValuePair(name, value));
    }

    public boolean removeNameValuePair(NameValuePair pairToRemove) {
        if (pairToRemove == null) {
            return false;
        }

        for (NameValuePair nameValuePair : nameValuePairs) {
            if (pairToRemove.equals(nameValuePair)) {
                nameValuePairs.remove(nameValuePair);
                return true;
            }
        }
        return false;
    }

    public List<NameValuePair> getNameValuePairs(String name) {
        List<NameValuePair> returnList = new ArrayList<>();
        for (NameValuePair nameValuePair : nameValuePairs) {
            if (StringUtils.equals(nameValuePair.getName(), name)) {
                returnList.add(nameValuePair);
            }
        }
        return returnList;
    }

    /**
     * This is a convenience method that might cause trouble, it will blindly return
     * the first value for a given name.  Normally that's all that we want, but
     * in some situations that might cause a surprise.
     *
     * @param name
     * @return
     */
    public String getFirstValue(String name) {
        for (NameValuePair nameValuePair : nameValuePairs) {
            if (StringUtils.equals(nameValuePair.getName(), name)) {
                return nameValuePair.getValue();
            }
        }
        return null;
    }


    private void createMapFromUrl() {
        String[] tokens = StringUtils.split(url.toString(), QUESTION_MARK, 2);
        if (tokens.length == 2) {
            urlWithoutParameters = tokens[0];
            nameValuePairs.addAll(URLEncodedUtils.parse(tokens[1], StandardCharsets.UTF_8));
        } else {
            urlWithoutParameters = url.toString();
        }
    }
}
