package org.zfin.infrastructure.captcha;

import org.apache.commons.lang3.StringUtils;
import org.zfin.framework.featureflag.FeatureFlagEnum;
import org.zfin.framework.featureflag.FeatureFlags;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.zfin.infrastructure.captcha.RecaptchaService.getCurrentVersion;

public class RecaptchaKeys {
    private static final Map<String, String> cache = new ConcurrentHashMap<>();

    public enum Version {
        V2,
        V3;
    }

    public static String getSiteKey() throws IOException {
        return getValue("recaptchaSiteKey", getCurrentVersion());
    }

    public static String getSecretKey() throws IOException {
        return getValue("recaptchaSecretKey", getCurrentVersion());
    }

    private static String getValue(String filenamePrefix, Version recaptchaVersion) throws IOException {
        String filename = filenamePrefix + recaptchaVersion.name() + ".txt";
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
        return StringUtils.chomp(Files.readString(file));
    }
}
