package org.zfin.uniquery;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class UrlPattern {

    private String pattern;
    private String type;
    // optional: Lucene uses default value.
    private int boostValue;
    private String titlePrefix;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public int getBoostValue() {
        return boostValue;
    }

    public void setBoostValue(int boostValue) {
        this.boostValue = boostValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitlePrefix() {
        return titlePrefix;
    }

    public void setTitlePrefix(String titlePrefix) {
        this.titlePrefix = titlePrefix;
    }
}
