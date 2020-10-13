package org.zfin.framework.presentation;

public enum SectionVisibilityAction {
    SHOW_SECTION("showSection"),
    HIDE_SECTION("hideSection"),
    SHOW_ALL("showAll"),
    HIDE_ALL("hideAll");
    private String value;

    private SectionVisibilityAction(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

    public static SectionVisibilityAction[] getActionItems() {
        return values();
    }
}
