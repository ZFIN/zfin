package org.zfin.util;

/**
 * Utilities that are not in PropertyUtils.
 */
public class ZfinPropertyUtils {

    public static Object getPropertyOrNull(Object object, String property) {
        try {
            return org.apache.commons.beanutils.PropertyUtils.getProperty(object, property);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getStringOrNull(Object object, String property) {
        try {
            return (String) org.apache.commons.beanutils.PropertyUtils.getProperty(object, property);
        } catch (Exception e) {
            return null;
        }
    }

}
