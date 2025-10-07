package org.zfin.infrastructure.captcha;

import lombok.extern.log4j.Log4j2;
import org.zfin.infrastructure.TokenStorage;

import java.io.IOException;
import java.util.Optional;

import static org.zfin.infrastructure.captcha.CaptchaService.getCurrentVersion;

/**
 * Requires the existence of captchaSiteKey-V2.txt, captchaSiteKey-V3.txt, captchaSiteKey-hCaptcha.txt
 * to store keys for recaptcha v2, recaptcha v3, and hCaptcha respectively. Similar files for secret keys are needed too.
 */
@Log4j2
public class CaptchaKeys {

    public enum Version {
        V2,
        V3,
        hCaptcha,
        altcha;
    }

    public static Optional<String> getSiteKey() {
        TokenStorage.ServiceKey serviceKey = switch (getCurrentVersion()) {
            case V2 -> TokenStorage.ServiceKey.RECAPTCHA_V2_SITE_KEY;
            case V3 -> TokenStorage.ServiceKey.RECAPTCHA_V3_SITE_KEY;
            case hCaptcha -> TokenStorage.ServiceKey.HCAPTCHA_SITE_KEY;
            case altcha -> TokenStorage.ServiceKey.ALTCHA_SITE_KEY;
        };
        return (new TokenStorage()).getValue(serviceKey);
    }

    public static Optional<String> getSecretKey() throws IOException {
        TokenStorage.ServiceKey serviceKey = switch (getCurrentVersion()) {
            case V2 -> TokenStorage.ServiceKey.RECAPTCHA_V2_SECRET_KEY;
            case V3 -> TokenStorage.ServiceKey.RECAPTCHA_V3_SECRET_KEY;
            case hCaptcha -> TokenStorage.ServiceKey.HCAPTCHA_SECRET_KEY;
            case altcha -> TokenStorage.ServiceKey.ALTCHA_SECRET_KEY;
        };
        return (new TokenStorage()).getValue(serviceKey);
    }

}
