package org.zfin.infrastructure.captcha;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;
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
import org.zfin.framework.featureflag.FeatureFlagEnum;
import org.zfin.framework.featureflag.FeatureFlags;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.zfin.profile.service.ProfileService.isLoggedIn;

@Log4j2
public class RecaptchaService {

    private static final String BASE_URL = "https://www.google.com/recaptcha/api/siteverify";
    private static final double CONFIDENCE_THRESHOLD = 0.5;
    private static final String RECAPTCHA_VERSION = "3";

    //These are the json keys we use to communicate with recaptcha API
    private static final String RECAPTCHA_ARG_SECRET = "secret";
    private static final String RECAPTCHA_ARG_RESPONSE = "response";
    private static final String RECAPTCHA_ARG_REMOTEIP = "remoteip";
    private static final String RECAPTCHA_RESPONSE_SUCCESS = "success";

    /**
     * Set the current session as having been successfully verified using captcha
     * @param request
     */
    public static void setSuccessfulCaptchaToken(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("CAPTCHA_VALIDATED", true);
    }

    /**
     * Remove the session variable so the current user is not considered verified by captcha
     * @param request
     */
    public static void unsetSuccessfulCaptchaToken(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("CAPTCHA_VALIDATED", false);
    }

    /**
     * Check if the current session is verified by captcha
     * @param request
     * @return true if verified human
     */
    private static boolean isSuccessfulCaptchaToken(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Boolean sessionValue = (Boolean) session.getAttribute("CAPTCHA_VALIDATED");
        if (sessionValue == null) {
            return false;
        }
        return sessionValue;
    }

    /**
     * If captcha rules require a redirection to run through the captcha validation process,
     * this method will return the URL to redirect to. If it returns empty, then no redirect is needed.
     *
     * @param request
     * @return
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
        return Optional.of("/action/captcha/" + RECAPTCHA_VERSION + "/challenge?redirect=" + URLEncoder.encode(currentUrl, StandardCharsets.UTF_8));
    }

    /**
     * Using the response from the client, call the google api and ask if the response
     * is from a verified human.
     *
     * @param version The specific version of google recaptcha api
     * @param challengeResponse The response from the client
     * @return true if a verified human
     * @throws IOException if cannot read captcha keys
     */
    public static boolean verifyRecaptcha(RecaptchaKeys.Version version, String challengeResponse) throws IOException {
        return verifyRecaptchaWithGoogle(challengeResponse, null, RecaptchaKeys.getSecretKey(version));
    }

    /**
     * Verifies the reCAPTCHA response token.
     *
     * @param userResponse The user response token from the frontend.
     * @param remoteIp     Optional: The IP address of the user (can be null).
     * @param secretKey    API key from google.
     * @return true if the verification is successful, false otherwise.
     */
    private static boolean verifyRecaptchaWithGoogle(String userResponse, String remoteIp, String secretKey) {
        // Prepare the POST parameters
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair(RECAPTCHA_ARG_SECRET, secretKey));
        nvps.add(new BasicNameValuePair(RECAPTCHA_ARG_RESPONSE, userResponse));
        if (remoteIp != null && !remoteIp.isEmpty()) {
            nvps.add(new BasicNameValuePair(RECAPTCHA_ARG_REMOTEIP, remoteIp));
        }

        try (CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build())
                .build()) {

            HttpPost post = new HttpPost(BASE_URL);
            post.setEntity(new UrlEncodedFormEntity(nvps));

            HttpResponse response = client.execute(post);

            String responseString = EntityUtils.toString(response.getEntity());
            JsonNode jsonResponse = new ObjectMapper().readTree(responseString);
            if (jsonResponse.has("score")) {
                return jsonResponse.get("score").asDouble() >= CONFIDENCE_THRESHOLD;
            }
            return jsonResponse.get(RECAPTCHA_RESPONSE_SUCCESS).asBoolean();
        } catch (Exception e) {
            return false;
        }
    }

}
