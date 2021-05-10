package org.zfin.framework.presentation;

/**
 * Convenience class that transforms text entry fields.
 */
public class UIFieldTransformer {

    public static String transformTextEntryFieldValue(String value) {
        if (value == null)
            return null;

        // strip off prefixed and trailing spaces
        value = value.trim();
        return value;
    }

}
