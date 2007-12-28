package org.zfin.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

/**
 * This class is used to create a URL by adding request parameters
 * (name-value pairs). I tuses URLEncoder to ensure proper
 * encoding.
 */
public class URLCreator {

    private StringBuilder url = new StringBuilder();
    private HashMap<String, String> nameValuePairs = new HashMap<String, String>();

    private static final Log LOG = LogFactory.getLog(URLCreator.class);

    public URLCreator(String url) {
        this.url.append(url);
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

    public String getFullURL() {
        Iterator<String> keys = nameValuePairs.keySet().iterator();
        boolean start = true;
        while (keys.hasNext()) {
            String name = keys.next();
            String value = nameValuePairs.get(name);
            if (start) {
                url.append("?");
                start = false;
            } else {
                url.append("&");
            }
            url.append(name);
            url.append("=");
            url.append(value);
        }

        String encodedUrl;
        try {
            encodedUrl = URLEncoder.encode(url.toString(), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            LOG.error(e);
            encodedUrl = URLEncoder.encode(url.toString());
        }
        return encodedUrl;
    }
}
