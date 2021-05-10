package org.zfin.publication;

public enum PublicationType {
    ABSTRACT("Abstract", true, 3, true),
    ACTIVE_CURATION("Active Curation", false, 9, false),
    BOOK("Book", true, 5, true),
    CHAPTER("Chapter", true, 6, true),
    CURATION("Curation", false, 8, false),
    JOURNAL("Journal", true, 1, true),
    MOVIE("Movie", true, 4, true),
    OTHER("Other", true, 10, true),
    REVIEW("Review", true, 2, true),
    UNKNOWN("Unknown", true, 12, true),
    UNPUBLISHED("Unpublished", false, 11, false),
    THESIS("Thesis", true, 7, true);

    private final String display;
    private final boolean curationAllowed;
    private final int displayOrder;
    private final boolean published;

    PublicationType(String type, Boolean curationAllowed, int displayOrder, boolean published) {
        this.display = type;
        this.curationAllowed = curationAllowed;
        this.displayOrder = displayOrder;
        this.published = published;
    }

    public String getDisplay() {
        return display;
    }

    public boolean isCurationAllowed() {
        return curationAllowed;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isPublished() {
        return published;
    }

    @Override
    public String toString() {
        return display;
    }

    public static PublicationType fromString(String display) {
        for (PublicationType type : values()) {
            if (type.display.equals(display)) {
                return type;
            }
        }
        return null;
    }

}
