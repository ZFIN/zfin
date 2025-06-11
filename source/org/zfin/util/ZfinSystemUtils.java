package org.zfin.util;

/**
 * Utilities that are not in PropertyUtils.
 */
public class ZfinSystemUtils {

    public static String env(String key) {
        return System.getenv(key);
    }

    public static Boolean envTrue(String key) {
        String value = env(key);
        return value != null && ("true".equalsIgnoreCase(value) || "1".equals(value));
    }

}
