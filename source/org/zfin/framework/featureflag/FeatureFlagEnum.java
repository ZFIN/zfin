package org.zfin.framework.featureflag;


import java.util.NoSuchElementException;

public enum FeatureFlagEnum {
    JBROWSE2("jBrowse2", false),
    CURATOR_JOB_POSTING("Curator Job Posting", true),
    USE_NAVIGATION_COUNTER("Show Navigation Counter", false),
    SHOW_ALLIANCE_DATA("Show Alliance Data", false),
    USE_REACT_CONSTRUCT_TAB("Use React-Based Construct Tab on Curation UI", false),
    ENABLE_CAPTCHA("Enable Captcha", false),
    RECAPTCHA_V2("Use Recaptcha V2 - Otherwise V3", false),
    H_CAPTCHA("Use hCaptcha - Otherwise Recaptcha", true);


    private final String name;
    private final boolean enabledByDefault;

    FeatureFlagEnum(String name, boolean enabledByDefault) {
        this.name = name;
        this.enabledByDefault = enabledByDefault;
    }

    public String getName() {
        return name;
    }
    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    public static FeatureFlagEnum getFlag(String flag) throws NoSuchElementException {
        for (FeatureFlagEnum t : values()) {
            if (t.toString().equals(flag))
                return t;
        }
        throw new NoSuchElementException("No such flag: " + flag );
    }

    public static FeatureFlagEnum getFlagByName(String flag) throws NoSuchElementException {
        for (FeatureFlagEnum t : values()) {
            if (t.getName().equals(flag))
                return t;
        }
        throw new NoSuchElementException("No flag named " + flag + " found.");
    }
}
