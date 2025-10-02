package org.zfin.infrastructure;

import org.apache.commons.lang3.StringUtils;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.getenv;

public class TokenStorage implements TokenStorageInterface {
    private static final Map<String, String> cache = new ConcurrentHashMap<>();

    public enum ServiceKey {
        RECAPTCHA_V2_SITE_KEY("captchaSiteKey-V2.txt"),
        RECAPTCHA_V2_SECRET_KEY("captchaSecretKey-V2.txt"),
        RECAPTCHA_V3_SITE_KEY("captchaSiteKey-V3.txt"),
        RECAPTCHA_V3_SECRET_KEY("captchaSecretKey-V3.txt"),
        HCAPTCHA_SITE_KEY("captchaSiteKey-hCaptcha.txt"),
        HCAPTCHA_SECRET_KEY("captchaSecretKey-hCaptcha.txt"),
        ALTCHA_SITE_KEY("captchaSiteKey-altcha.txt"),
        ALTCHA_SECRET_KEY("captchaSecretKey-altcha.txt"),
        ALLIANCE_API_TOKEN("alliance-api-token.txt"),
        NCBI_API_TOKEN("ncbi-token.txt"),
        OMIM_API_TOKEN("omim-token.txt");

        private final String filename;

        ServiceKey(String filename) {
            this.filename = filename;
        }

        public String getFilename() {
            return filename;
        }
    }

    @Override
    public Optional<String> getValue(TokenStorage.ServiceKey serviceKey) {
        String filename = serviceKey.getFilename();
        String value = cache.computeIfAbsent(filename, k -> {
            Optional<String> val = getTokenFileValue(k);
            return val.isPresent() ? val.get() : null;
        });
        if (StringUtils.isEmpty(value)) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    @Override
    public void setValue(TokenStorage.ServiceKey serviceKey, String value) throws IOException {
        Path file = Path.of(targetRoot() + "/server_apps/tokens/" + serviceKey.getFilename());
        Files.writeString(file, value);
        cache.put(serviceKey.getFilename(), value);
    }

    @Override
    public void deleteValue(TokenStorage.ServiceKey serviceKey) {
            cache.remove(serviceKey.getFilename());
    }

    private boolean tokenFileExists(TokenStorage.ServiceKey serviceKey) {
        if (targetRoot() == null) {
            return false;
        }
        Path file = Path.of(getTokenFilePath(serviceKey));
        return Files.exists(file);
    }

    private String getTokenFilePath(TokenStorage.ServiceKey serviceKey) {
        if (targetRoot() == null) {
            return null;
        }
        return targetRoot() + "/server_apps/tokens/" + serviceKey.getFilename();
    }

    private Optional<String> getTokenFileValue(String tokenFilename) {
        if (targetRoot() == null) {
            return Optional.empty();
        }
        Path file = Path.of(targetRoot() + "/server_apps/tokens/" + tokenFilename);
        String value = null;
        try {
            value = StringUtils.chomp(Files.readString(file));
        } catch (IOException e) {}
        if (StringUtils.isEmpty(value)) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    private void printUsage() {
        System.out.println("Usage: java TokenStorage read|write <serviceKey> [<value>]");
        System.out.println("  or : gradle tokenStorage --args='read|write <serviceKey> [<value>]'");
        System.out.println("Available service keys:");
        for (ServiceKey key : ServiceKey.values()) {
            System.out.println(" - " + key.name() + " (" + key.getFilename() + ")");
        }
    }

    private String targetRoot() {
        String targetRoot = ZfinPropertiesEnum.TARGETROOT.value();
        if (StringUtils.isEmpty(targetRoot)) {
//            log.warn("TARGETROOT environment variable is not set, trying to get it from environment variables.");
            targetRoot = getenv("TARGETROOT");
        } else {
            return targetRoot;
        }
        if (StringUtils.isEmpty(targetRoot)) {
            return null;
        } else {
//            log.debug("Using TARGETROOT: " + targetRoot);
            return targetRoot;
        }
    }

    private void run(String[] args) {
        try {
            // Example usage: java TokenStorage read|write <serviceKey> [<value>]
            if (args.length < 2) {
                printUsage();
                return;
            }
            String action = args[0];
            ServiceKey serviceKey = ServiceKey.valueOf(args[1].toUpperCase());
            if ("read".equalsIgnoreCase(action)) {
                if (!tokenFileExists(serviceKey)) {
                    if (targetRoot() == null) {
                        System.out.println("TARGETROOT environment variable is not set.");
                        return;
                    }
                    System.out.println("Token file for " + serviceKey + "(" + getTokenFilePath(serviceKey) + ") does not exist.");
                    System.exit(1);
                    return;
                }
                Optional<String> value = getValue(serviceKey);
                if (value.isEmpty()) {
                    System.out.println("No value found for " + serviceKey);
                    System.exit(2);
                    return;
                }
                System.out.println(value.get());
            } else if ("write".equalsIgnoreCase(action) && args.length == 3) {
                String value = args[2];
                setValue(serviceKey, value);
                System.out.println("Written value for " + serviceKey + ": " + value);
            } else {
                System.out.println("Invalid action or missing value for write.");
                printUsage();
                System.exit(3);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(4);
        }
    }

    public static void main(String[] args) {
        new TokenStorage().run(args);
    }
}
