package org.zfin.framework.presentation;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultTermNavigationMenu extends NavigationMenu {

    /**
     * Set up the navigation menu.
     * This uses the builder pattern available on the NavigationItem class through lombok
     */
    public DefaultTermNavigationMenu() {
        super(
            title(NavigationMenuOptions.SUMMARY).showCount(false),
            title(NavigationMenuOptions.RELATIONSHIPS).showCount(false)
        );
    }

}
