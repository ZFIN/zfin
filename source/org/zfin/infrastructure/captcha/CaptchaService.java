package org.zfin.infrastructure.captcha;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.altcha.altcha.Altcha;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.web.util.WebUtils;
import org.zfin.framework.featureflag.FeatureFlagEnum;
import org.zfin.framework.featureflag.FeatureFlags;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.zfin.infrastructure.service.RequestService.getCurrentRequest;
import static org.zfin.infrastructure.service.RequestService.getCurrentResponse;
import static org.zfin.profile.service.ProfileService.isLoggedIn;

@Log4j2
public class CaptchaService {

    //TODO: should we compute this algorithmically? If we see bots setting this without going through captcha,
    //      we should use some cryptography to set it in a way that prevents tampering.
    private static final String CAPTCHA_COOKIE_NAME = "grcptv";
    private static final String CAPTCHA_COOKIE_VALUE = "rcv_true";
    private static final int CAPTCHA_COOKIE_MAX_AGE = 7 * 24 * 60 * 60; //one week

    /**
     * Set the current session as having been successfully verified using captcha
     *
     */
    public static void setSuccessfulCaptchaToken() {
        HttpServletResponse response = getCurrentResponse();
        Cookie captchaCookie = new Cookie(CAPTCHA_COOKIE_NAME, CAPTCHA_COOKIE_VALUE);
        captchaCookie.setMaxAge(CAPTCHA_COOKIE_MAX_AGE);
        captchaCookie.setPath("/");

        response.addCookie(captchaCookie);
    }

    /**
     * Remove the session variable so the current user is not considered verified by captcha
     * @param response Server response object to remove cookie from
     */
    public static void unsetSuccessfulCaptchaToken(HttpServletResponse response) {
        Cookie captchaCookie = new Cookie(CAPTCHA_COOKIE_NAME, CAPTCHA_COOKIE_VALUE);
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
        if (cookie != null && CAPTCHA_COOKIE_VALUE.equals(cookie.getValue())) {
            return true;
        }
        return false;
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
