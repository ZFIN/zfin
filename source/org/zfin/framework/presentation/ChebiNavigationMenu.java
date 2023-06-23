package org.zfin.framework.presentation;


import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.Model;
import org.zfin.publication.Publication;

@Getter
@Setter
public class ChebiNavigationMenu extends NavigationMenu {

    /**
     * Set up the navigation menu.
     * This uses the builder pattern available on the NavigationItem class through lombok
     */
    public ChebiNavigationMenu() {
        super(
            title(NavigationMenuOptions.SUMMARY).showCount(false),
            title(NavigationMenuOptions.RELATIONSHIPS).showCount(false),
            title(NavigationMenuOptions.PHENOTYPE_CHEBI).showCount(false),
            title(NavigationMenuOptions.CHEBI_HUMAN_DISEASE).showCount(false),
            title(NavigationMenuOptions.CITATION).showCount(false)
            );
    }

}
