package org.zfin.framework.presentation;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class is basically a collection of NavigationItem entries.
 * It has some helper methods for setting properties of its collection members
 * and getting the collection of items that should be displayed in a specific context.
 * Also used for the same logic for the sections that correspond to the left nav.
 */
@Getter
@Setter
public class NavigationMenu {

    protected List<NavigationItem> navigationItems;
    protected boolean root = false;

    public NavigationMenu(NavigationItem.NavigationItemBuilder ...navigationItemBuilders) {
        this.navigationItems = Stream.of(navigationItemBuilders)
                .map(NavigationItem.NavigationItemBuilder::build)
                .toList();
    }

    /**
     * Set the hidden attribute for the NavigationItem in this menu matching the given title
     * @param title
     * @param value
     */
    public void setHidden(NavigationMenuOptions title, boolean value) {
        for(NavigationItem item : navigationItems) {
            if (item.getTitle().equals(title)) {
                item.setHidden(value);
            }
        }
    }

    /**
     * Determine which sections to include based on each Menu Item's properties (isRequireRoot, isHidden, etc.)
     * @return collection of NavigationItems for display
     */
    public List<NavigationItem> getDisplayedNavigationItems() {
        List<NavigationItem> filteredSections = new ArrayList<>();
        for(NavigationItem item : navigationItems) {
            if (item.isHidden()) {
                continue;
            }
            if (item.isRequireRoot() && !isRoot()) {
                continue;
            }
            filteredSections.add(item);
        }
        return filteredSections;
    }

    /**
     * Given the title of a section of a view page, return true if it should be shown, otherwise false
     *
     * @param title The title of the section
     * @return true if given section title should be displayed
     */
    public boolean include(String title) {
        return this.getDisplayedNavigationItems()
                .stream()
                .map(NavigationItem::toString)
                .toList()
                .contains(title);
    }

    public boolean include(NavigationItem item) {
        return include(item.toString());
    }

    public boolean include(NavigationMenuOptions option) {
        return include(option.toString());
    }


    /**
     * Create a builder for a NavigationItem starting with a title
     * @return the builder
     */
    protected static NavigationItem.NavigationItemBuilder title(NavigationMenuOptions title) {
        return NavigationItem.builder().title(title);
    }

    /**
     * Get the NavigationItem matching the given title
     * @param title title of the item we are looking for (enum)
     * @return the NavigationItem
     */
    protected NavigationItem getNavigationItem(NavigationMenuOptions title) {
        for(NavigationItem result : getNavigationItems()) {
            if (result.getTitle().equals(title)) {
                return result;
            }
        }
        return null;
    }
}
