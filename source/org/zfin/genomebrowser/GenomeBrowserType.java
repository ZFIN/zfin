package org.zfin.genomebrowser;

public enum GenomeBrowserType {
    GBROWSE("GBrowse"),
    JBROWSE("JBrowse");

    GenomeBrowserType(String type) {
        this.type = type;
    }

    private String type;

    public String toString() {
        return type;
    }

    public String getReactComponentId() {
        return switch (this) {
            case JBROWSE -> "JbrowseImage";
            case GBROWSE -> "GbrowseImage";
            default -> "GbrowseImage";
        };
    }

    public static GenomeBrowserType fromString(String type) {
        for (GenomeBrowserType genomeBrowserType : values()) {
            if (genomeBrowserType.type.equals(type)) {
                return genomeBrowserType;
            }
        }
        return null;
    }
}
