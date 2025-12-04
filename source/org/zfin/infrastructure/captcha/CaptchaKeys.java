package org.zfin.infrastructure.captcha;

import lombok.extern.log4j.Log4j2;
import org.zfin.infrastructure.TokenStorage;

import java.io.IOException;
import java.util.Optional;

import static org.zfin.util.ZfinStringUtils.randomString;

@Log4j2
public class CaptchaKeys {

    public static Optional<String> getSecretKey() throws IOException {
        TokenStorage tokenStorage = new TokenStorage();
        TokenStorage.ServiceKey serviceKey = TokenStorage.ServiceKey.ALTCHA_SECRET_KEY;
        Optional<String> altchaKey = tokenStorage.getValue(serviceKey);
        if (altchaKey.isPresent()) {
            return altchaKey;
        } else {
            log.error("Altcha key is not set in token storage: " + serviceKey.getFilename());
            //we can try setting it to a random value once:
            String randomKey = randomString(50);
            try {
                tokenStorage.setValue(serviceKey, randomKey);
                return Optional.of(randomKey);
            } catch (IOException e) {
                log.error("Failed to set random altcha site key in token storage.", e);
            }
        }

        return Optional.empty();
    }

}
