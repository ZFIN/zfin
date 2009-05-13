package org.zfin.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

/**
 * This class is used to create a URL by adding request parameters
 * (name-value pairs). I tuses URLEncoder to ensure proper
 * encoding.
 */
public class URLCreator {

    private StringBuilder url = new StringBuilder();
    // linked map to retain the order the parameters were originally in.
    private LinkedHashMap<String, String> nameValuePairs = new LinkedHashMap<String, String>();
    private String urlWithoutParameters;

    private static final Log LOG = LogFactory.getLog(URLCreator.class);

    public URLCreator(String url) {
        if (url == null)
            throw new NullPointerException("No URL provided in constructor");
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
    public boolean addNamevaluePair(String name, String value) {
        if (nameValuePairs.containsKey(name))
            return false;
        nameValuePairs.put(name, value);
        return true;
    }

    /**
     * Retrieve the full URL if fullURL=true
     * otherwise return query string only.
     * The string is URL-encoded.
     *
     * @param completeURL boolean
     * @return full url or query string
     */
    public String getURL(boolean completeURL) {
        StringBuilder fullUrl = new StringBuilder();
        if (completeURL)
            fullUrl = new StringBuilder(urlWithoutParameters);

        Iterator<String> keys = nameValuePairs.keySet().iterator();
        boolean start = true;
        while (keys.hasNext()) {
            String name = keys.next();
            String value = nameValuePairs.get(name);
            if (start) {
                if (completeURL)
                    fullUrl.append("?");
                start = false;
            } else {
                fullUrl.append("&");
            }
            fullUrl.append(name);
            fullUrl.append("=");
            String urlEncodedValue;
            try {
                urlEncodedValue = URLEncoder.encode(value, "ISO-8859-1");
            } catch (UnsupportedEncodingException e) {
                LOG.error(e);
                urlEncodedValue = URLEncoder.encode(value);
            }
            fullUrl.append(urlEncodedValue);
        }

        return fullUrl.toString();
    }

    /**
     * Removes a parameter and its values from the url query string.
     *
     * @param name parameter name
     * @return return true if parameter was found
     */
    public boolean removeNamevaluePair(String name) {
        if (StringUtils.isEmpty(name))
            return true;
        if (nameValuePairs.containsKey(name)) {
            nameValuePairs.remove(name);
            return true;
        }
        return false;
    }

    private void createMapFromUrl() {

        // Split off the given URL from its query string
        StringTokenizer tokenizerQueryString = new StringTokenizer(url.toString(), "?");
        urlWithoutParameters = tokenizerQueryString.nextToken();

        // Parse the query string (if any) into name/value pairs
        if (tokenizerQueryString.hasMoreTokens()) {
            String strQueryString = tokenizerQueryString.nextToken();
            if (strQueryString != null) {
                StringTokenizer tokenizerNameValuePair = new StringTokenizer(strQueryString, "&");
                while (tokenizerNameValuePair.hasMoreTokens()) {
                    try {
                        String strNameValuePair = tokenizerNameValuePair.nextToken();
                        StringTokenizer tokenizerValue = new StringTokenizer(strNameValuePair, "=");

                        String strName = tokenizerValue.nextToken();
                        String strValue = tokenizerValue.nextToken();

                        nameValuePairs.put(strName, strValue);
                    }
                    catch (Throwable t) {
                        // If we cannot parse a parameter, ignore it
                    }
                }
            }
        }
    }

}
