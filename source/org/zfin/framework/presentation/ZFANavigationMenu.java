package org.zfin.framework.presentation;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZFANavigationMenu extends NavigationMenu {

    /**
     * Set up the navigation menu.
     * This uses the builder pattern available on the NavigationItem class through lombok
     */
    public ZFANavigationMenu() {
        super(
            title(NavigationMenuOptions.SUMMARY).showCount(false),
            title(NavigationMenuOptions.RELATIONSHIPS).showCount(false),
            title(NavigationMenuOptions.EXPRESSION).showCount(false),
            title(NavigationMenuOptions.PHENOTYPE).showCount(false)
        );
    }

}
