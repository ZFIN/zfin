package org.zfin.infrastructure.captcha;

import org.apache.commons.lang3.StringUtils;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.zfin.infrastructure.captcha.CaptchaService.getCurrentVersion;

/**
 * Requires the existence of captchaSiteKey-V2.txt, captchaSiteKey-V3.txt, captchaSiteKey-hCaptcha.txt
 * to store keys for recaptcha v2, recaptcha v3, and hCaptcha respectively. Similar files for secret keys are needed too.
 */
public class CaptchaKeys {
    private static final Map<String, String> cache = new ConcurrentHashMap<>();

    public enum Version {
        V2,
        V3,
        hCaptcha,
        altcha;
    }

    public static String getSiteKey() throws IOException {
        return getValue("captchaSiteKey-", getCurrentVersion());
    }

    public static String getSecretKey() throws IOException {
        return getValue("captchaSecretKey-", getCurrentVersion());
    }

    private static String getValue(String filenamePrefix, Version captchaVersion) throws IOException {
        String filename = filenamePrefix + captchaVersion.name() + ".txt";
        return cache.computeIfAbsent(filename, k -> {
            try {
                return getTokenFileValue(k);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static String getTokenFileValue(String tokenFilename) throws IOException {
        Path file = Path.of(ZfinPropertiesEnum.TARGETROOT.value() + "/server_apps/tokens/" + tokenFilename);
        String value = StringUtils.chomp(Files.readString(file));
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return value;
    }
}
