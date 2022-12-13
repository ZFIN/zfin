package org.zfin.framework.presentation;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Similar to a record, or a DTO class for storing the data relevant to a "navigation item"
 * as displayed on the left nav of a data view page.
 */
@Getter
@Setter
@Builder
public class NavigationItem {

    private NavigationMenuOptions title;

    //only show if user is root
    @Builder.Default
    private boolean requireRoot = false;

    //do not show this navigation item if there is no data associated with it
    @Builder.Default
    private boolean hidden = false;

    //specify if we should show the count in the navigation
    @Builder.Default
    private boolean showCount = true;

    //specify if we should show a border below this item
    @Builder.Default
    private boolean showBorder = false;

    @Override
    public String toString() {
        return this.getTitle().getValue();
    }

}
