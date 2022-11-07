package org.zfin.framework.featureflag;


public enum FeatureFlagEnum {
    JBROWSE("jBrowse"),
    CURATOR_JOB_POSTING("Curator Job Posting"),
    FUTURE_FLAG_PLACEHOLDER("Placeholder For Future Feature");

    private String name;

    FeatureFlagEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static FeatureFlagEnum getFlag(String flag) {
        for (FeatureFlagEnum t : values()) {
            if (t.toString().equals(flag))
                return t;
        }
        throw new RuntimeException("No flag named " + flag + " found.");
    }
}
