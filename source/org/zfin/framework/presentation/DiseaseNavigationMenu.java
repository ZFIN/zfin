package org.zfin.framework.presentation;


import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.featureflag.FeatureFlagEnum;

import static org.zfin.framework.featureflag.FeatureFlags.isFlagEnabled;

@Getter
@Setter
public class DiseaseNavigationMenu extends NavigationMenu {

    /**
     * Set up the navigation menu.
     * This uses the builder pattern available on the NavigationItem class through lombok
     */
    public DiseaseNavigationMenu() {
        super(
            title(NavigationMenuOptions.SUMMARY).showCount(false),
            title(NavigationMenuOptions.RELATIONSHIPS).showCount(false),
            title(NavigationMenuOptions.OTHER_PAGE).showCount(false),
            title(NavigationMenuOptions.GENES_INVOLVED).showCount(false),
            title(NavigationMenuOptions.ZEBRAFISH_MODELS).showCount(false),
            title(NavigationMenuOptions.CITATION).showCount(false),
            isFlagEnabled(FeatureFlagEnum.SHOW_ALLIANCE_DATA) ?
                title(NavigationMenuOptions.ALLELE).showCount(false) : null
        );
    }

}
