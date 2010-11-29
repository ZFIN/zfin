package org.zfin.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.codec.Hex;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

/**
 * We overload this class so that we can add IP to the token.
 * Only the makeTokenSignature method changes, but we need to overload the calls to that method.
 * Otherwise, most of this comes directly from TokenBasedRememberMeServices
 */
public class IpTokenRememberMeServices extends TokenBasedRememberMeServices{

    @Override
    protected UserDetails processAutoLoginCookie(String[] cookieTokens, HttpServletRequest request, HttpServletResponse response) {

        if (cookieTokens.length != 3) {
            throw new InvalidCookieException("Cookie token did not contain 3" +
                    " tokens, but contained '" + Arrays.asList(cookieTokens) + "'");
        }

        long tokenExpiryTime;

        try {
            tokenExpiryTime = new Long(cookieTokens[1]).longValue();
        }
        catch (NumberFormatException nfe) {
            throw new InvalidCookieException("Cookie token[1] did not contain a valid number (contained '" +
                    cookieTokens[1] + "')");
        }

        if (isTokenExpired(tokenExpiryTime)) {
            throw new InvalidCookieException("Cookie token[1] has expired (expired on '"
                    + new Date(tokenExpiryTime) + "'; current time is '" + new Date() + "')");
        }

        // Check the user exists.
        // Defer lookup until after expiry time checked, to possibly avoid expensive database call.

        UserDetails userDetails = getUserDetailsService().loadUserByUsername(cookieTokens[0]);

        // Check signature of token matches remaining details.
        // Must do this after user lookup, as we need the DAO-derived password.
        // If efficiency was a major issue, just add in a UserCache implementation,
        // but recall that this method is usually only called once per HttpSession - if the token is valid,
        // it will cause SecurityContextHolder population, whilst if invalid, will cause the cookie to be cancelled.
        String expectedTokenSignature = makeTokenSignature(tokenExpiryTime, userDetails.getUsername(),
                userDetails.getPassword(),request.getRemoteAddr());

        if (!expectedTokenSignature.equals(cookieTokens[2])) {
            throw new InvalidCookieException("Cookie token[2] contained signature '" + cookieTokens[2]
                    + "' but expected '" + expectedTokenSignature + "'");
        }

        return userDetails;
    }

    public void onLoginSuccess(HttpServletRequest request, HttpServletResponse response,
                               Authentication successfulAuthentication) {

        String username = retrieveUserName(successfulAuthentication);
        String password = retrievePassword(successfulAuthentication);

        // If unable to find a username and password, just abort as TokenBasedRememberMeServices is
        // unable to construct a valid token in this case.
        if (!StringUtils.hasLength(username) || !StringUtils.hasLength(password)) {
            return;
        }

        int tokenLifetime = calculateLoginLifetime(request, successfulAuthentication);
        long expiryTime = System.currentTimeMillis();
        // SEC-949
        expiryTime += 1000L* (tokenLifetime < 0 ? TWO_WEEKS_S : tokenLifetime);

        String signatureValue = makeTokenSignature(expiryTime, username, password,request.getRemoteAddr());

        setCookie(new String[] {username, Long.toString(expiryTime), signatureValue}, tokenLifetime, request, response);

        if (logger.isDebugEnabled()) {
            logger.debug("Added remember-me cookie for user '" + username + "', expiry: '"
                    + new Date(expiryTime) + "'");
        }
    }

    /**
     *
     * Calculates the digital signature to be put in the cookie. Default value is
     * MD5 ("username:tokenExpiryTime:password:key:IP")
     * @param tokenExpiryTime
     * @param username
     * @param password
     * @param ip
     * @return
     */
    protected String makeTokenSignature(long tokenExpiryTime, String username, String password,String ip) {
        String data = username + ":" + tokenExpiryTime + ":" + password + ":" + getKey() + ":" + ip;
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No MD5 algorithm available!");
        }

        return new String(Hex.encode(digest.digest(data.getBytes())));
    }
}
