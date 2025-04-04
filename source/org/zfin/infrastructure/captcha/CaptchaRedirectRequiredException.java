package org.zfin.infrastructure.captcha;

public class CaptchaRedirectRequiredException extends Exception {
    public CaptchaRedirectRequiredException(String message) {
        super(message);
    }
}
