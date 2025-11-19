package org.zfin.util;

/**
 * Utilities that are not in PropertyUtils.
 */
public class ZfinSystemUtils {

    public static String env(String key) {
        String result = System.getenv(key);
        if (result == null) {
            result = System.getProperty(key);
        }
        return result;
    }

    /**
     * Check if an environment variable or system property is set to true.
     * @param key
     * @return
     */
    public static Boolean envTrue(String key) {
        String value = env(key);
        Boolean result = value != null && ("true".equalsIgnoreCase(value) || "1".equals(value));
        if (!result) {
            String propertyValue = System.getProperty(key);
            result = propertyValue != null && ("true".equalsIgnoreCase(propertyValue) || "1".equals(propertyValue));
        }
        return result;
    }

}
