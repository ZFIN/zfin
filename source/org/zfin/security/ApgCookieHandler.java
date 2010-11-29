package org.zfin.security;

/**
 */
public class ApgCookieHandler {

    public static final String APG_ZFIN_LOGIN = "zfin_login";
    public static final int MAX_LENGTH_OF_APG_COOKIE = 19;

    /**
     * Pass in a Tomcat cookie and return an apg cookie.
     * APG cookies are shorter than Tomcat cookies and typically
     * are truncated.
     *
     * @param id Tomcat cookie
     * @return corresponding APG cookie
     */
    public static String convertTomcatCookieToApgCookie(String id) {
        return id.substring(0, ApgCookieHandler.MAX_LENGTH_OF_APG_COOKIE);
    }
}
