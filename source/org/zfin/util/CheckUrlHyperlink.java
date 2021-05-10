package org.zfin.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Checks a URL link.
 */
public class CheckUrlHyperlink {

    public static boolean isValidHyperlink(String url) {
        if (url == null)
            return false;

        URL u;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            return false;
        }

        HttpURLConnection uc;
        try {
            uc = (HttpURLConnection) u.openConnection();
        } catch (IOException e) {
            return false;
        }
        try {
            int responseCode = uc.getResponseCode();
            return responseCode == 200 || responseCode != 404;
        } catch (IOException e) {
            return false;
        }
    }

}
