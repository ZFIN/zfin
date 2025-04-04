package org.zfin.infrastructure.captcha;

import org.apache.commons.lang3.StringUtils;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RecaptchaKeys {
    private static final Map<String, String> cache = new ConcurrentHashMap<>();

    public enum Version {
        V2("2"),
        V3("3");

        private final String number;

        Version(String versionNumber) {
            this.number = versionNumber;
        }

        public static Version fromString(String versionString) {
            for (Version v : values()) {
                if (v.number.equals(versionString)) {
                    return v;
                }
            }
            throw new IllegalArgumentException("Unknown version: " + versionString);
        }

    }

    public static String getSiteKey(String version) throws IOException {
        return getSiteKey(Version.fromString(version));
    }

    public static String getSiteKey(Version version) throws IOException {
        return getValue("recaptchaSiteKey", version);
    }

    public static String getSecretKey(String version) throws IOException {
        return getSecretKey(Version.fromString(version));
    }

    public static String getSecretKey(Version version) throws IOException {
        return getValue("recaptchaSecretKey", version);
    }

    private static String getValue(String filenamePrefix, Version keyType) throws IOException {
        String filename = filenamePrefix + "V" + keyType.number + ".txt";
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
