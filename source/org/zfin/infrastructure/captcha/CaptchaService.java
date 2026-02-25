package org.zfin.infrastructure.captcha;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.altcha.altcha.Altcha;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.WebUtils;
import org.zfin.framework.featureflag.FeatureFlagEnum;
import org.zfin.framework.featureflag.FeatureFlags;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

import static org.zfin.infrastructure.service.RequestService.getCurrentRequest;
import static org.zfin.infrastructure.service.RequestService.getCurrentResponse;
import static org.zfin.profile.service.ProfileService.isLoggedIn;

@Log4j2
public class CaptchaService {

    private static final String CAPTCHA_COOKIE_NAME = "grcptv";
    private static final int CAPTCHA_COOKIE_MAX_AGE = 7 * 24 * 60 * 60; //one week in seconds
    private static final long TOKEN_MAX_AGE_MS = CAPTCHA_COOKIE_MAX_AGE * 1000L; //convert to milliseconds
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * Set the current session as having been successfully verified using captcha
     *
     */
    public static void setSuccessfulCaptchaToken() {
        HttpServletRequest request = getCurrentRequest();
        HttpServletResponse response = getCurrentResponse();
        String sessionId = request.getSession().getId();
        String token = generateToken(sessionId);
        if (token == null) {
            log.error("Failed to generate captcha token; allowing access without cookie");
            return;
        }
        Cookie captchaCookie = new Cookie(CAPTCHA_COOKIE_NAME, token);
        captchaCookie.setMaxAge(CAPTCHA_COOKIE_MAX_AGE);
        captchaCookie.setPath("/");

        response.addCookie(captchaCookie);
    }

    /**
     * Remove the session variable so the current user is not considered verified by captcha
     * @param response Server response object to remove cookie from
     */
    public static void unsetSuccessfulCaptchaToken(HttpServletResponse response) {
        Cookie captchaCookie = new Cookie(CAPTCHA_COOKIE_NAME, "");
        captchaCookie.setMaxAge(0);
        captchaCookie.setPath("/");

        response.addCookie(captchaCookie);
    }

    /**
     * Check if the current session is verified by captcha
     * @param request Check if this request object has the needed cookie
     * @return true if verified human
     */
    public static boolean isSuccessfulCaptchaToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, CAPTCHA_COOKIE_NAME);
        if (cookie == null || StringUtils.isEmpty(cookie.getValue())) {
            return false;
        }
        String sessionId = request.getSession().getId();
        return validateToken(cookie.getValue(), sessionId);
    }

    /**
     * If captcha rules require a redirection to run through the captcha validation process,
     * this method will return the URL to redirect to. If it returns empty, then no redirect is needed.
     *
     * @param request The current request object so we can determine the redirect page to come back to
     * @return If empty, no redirect is needed. Otherwise, use the value as the destination for a 302 redirect
     */
    public static Optional<String> getRedirectUrlIfNeeded(HttpServletRequest request) {
        if (isLoggedIn()) {
            return Optional.empty();
        }
        if (!FeatureFlags.isFlagEnabled(FeatureFlagEnum.ENABLE_CAPTCHA)) {
            return Optional.empty();
        }
        if (isSuccessfulCaptchaToken(request)) {
            return Optional.empty();
        }
        String currentUrl = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if (queryString != null) {
            currentUrl += "?" + queryString;
        }

        // Add the current URL as a query parameter to the CAPTCHA challenge redirect
        return Optional.of("/action/captcha/challenge?redirect=" + URLEncoder.encode(currentUrl, StandardCharsets.UTF_8));
    }

    /**
     * Using the response from the client, call the google api and ask if the response
     * is from a verified human.
     *
     * @param challengeResponse The response from the client
     * @return true if a verified human
     * @throws IOException if cannot read captcha keys
     */
    public static boolean verifyCaptcha(String challengeResponse) throws IOException {
        if (StringUtils.isEmpty(challengeResponse)) {
            return false;
        }
        if (verifyAltcha(challengeResponse)) {
            setSuccessfulCaptchaToken();
            return true;
        }
        return false;
    }

    private static String generateToken(String sessionId) {
        try {
            Optional<String> secretKey = CaptchaKeys.getSecretKey();
            if (secretKey.isEmpty()) {
                return null;
            }
            String timestamp = String.valueOf(System.currentTimeMillis());
            String data = timestamp + "|" + sessionId;
            String hmac = computeHmac(data, secretKey.get());
            return timestamp + "|" + sessionId + "|" + hmac;
        } catch (Exception e) {
            log.error("Failed to generate captcha token", e);
            return null;
        }
    }

    private static boolean validateToken(String token, String sessionId) {
        try {
            String[] parts = token.split("\\|", 3);
            if (parts.length != 3) {
                return false;
            }

            String timestamp = parts[0];
            String tokenSessionId = parts[1];
            String hmac = parts[2];

            Optional<String> secretKey = CaptchaKeys.getSecretKey();
            if (secretKey.isEmpty()) {
                log.error("Secret key unavailable during captcha validation; allowing access");
                return true;
            }

            // Verify HMAC
            String expectedHmac = computeHmac(timestamp + "|" + tokenSessionId, secretKey.get());
            if (!MessageDigest.isEqual(hmac.getBytes(StandardCharsets.UTF_8), expectedHmac.getBytes(StandardCharsets.UTF_8))) {
                return false;
            }

            // Verify timestamp is within allowed window
            long tokenTime = Long.parseLong(timestamp);
            if (System.currentTimeMillis() - tokenTime > TOKEN_MAX_AGE_MS) {
                return false;
            }

            // Verify session ID matches
            return tokenSessionId.equals(sessionId);
        } catch (Exception e) {
            log.error("Failed to validate captcha token", e);
            return false;
        }
    }

    private static String computeHmac(String data, String key) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
    }

    private static boolean verifyAltcha(String challengeResponse) throws IOException {
        try {
            Optional<String> secretKey = CaptchaKeys.getSecretKey();
            if (secretKey.isEmpty()) {
                log.error("Altcha secret key is not set. Please check your configuration.");
                // just allow for now (better than breaking the site)
                return true;
            }
            return Altcha.verifySolution(challengeResponse, secretKey.get(), true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
