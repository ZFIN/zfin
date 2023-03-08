package org.zfin.framework.presentation;


import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.Model;
import org.zfin.framework.featureflag.FeatureFlagEnum;
import org.zfin.framework.featureflag.FeatureFlags;
import org.zfin.publication.Publication;

/**
 * This class encapsulates the logic for which navigation items to show on the publication view page,
 * and also some of the properties like showing counts, or borders.
 * This is easier to maintain than having the logic in the JSP file.
 */
@Getter
@Setter
public class PublicationNavigationMenu extends NavigationMenu {

    /**
     * Set up the navigation menu.
     * This uses the builder pattern available on the NavigationItem class through lombok
     */
    public PublicationNavigationMenu() {
        super(
            title(NavigationMenuOptions.SUMMARY).showCount(false),
            title(NavigationMenuOptions.ABSTRACT).showCount(false),
            title(NavigationMenuOptions.ERRATA).showCount(false).showBorder(true),
            title(NavigationMenuOptions.GENES),
            title(NavigationMenuOptions.FIGURES),
            title(NavigationMenuOptions.PROBES),
            title(NavigationMenuOptions.EXPRESSION),
            title(NavigationMenuOptions.PHENOTYPE),
            title(NavigationMenuOptions.MUTATION),
            title(NavigationMenuOptions.DISEASE),
            title(NavigationMenuOptions.STRS),
            title(NavigationMenuOptions.FISH),
            title(NavigationMenuOptions.ANTIBODIES),
            title(NavigationMenuOptions.ORTHOLOGY),
            title(NavigationMenuOptions.EFGs),
            title(NavigationMenuOptions.MAPPING),
            title(NavigationMenuOptions.DIRECTLY_ATTRIBUTED_DATA).requireRoot(true),
            title(NavigationMenuOptions.ZEBRASHARE).requireRoot(true)
        );

        //If we aren't using the counter on the left hand navigation, turn it off for all menu items
        //We can remove this once this functionality is no longer behind a flag
        if (!FeatureFlags.isFlagEnabled(FeatureFlagEnum.USE_NAVIGATION_COUNTER)) {
            getNavigationItems().forEach(item -> item.setShowCount(false));
        }

    }

    /**
     * Pass the page model for publication-view into this method.
     * It will set up the logic of which menu items to show based on the
     * contents of the model.
     *
     * @param model The page model for publication-view
     */
    public void setModel(Model model) {
        Publication publication = (Publication)(model.asMap().get("publication"));

        //show zebrashare nav item if data is present
        this.setHidden(NavigationMenuOptions.ZEBRASHARE, !model.containsAttribute("zebraShareMetadata"));

        //show errata if exists
        this.setHidden(NavigationMenuOptions.ERRATA, StringUtils.isEmpty(publication.getErrataAndNotes()));

        //if there's no errata, put the border on the abstract
        if (StringUtils.isEmpty(publication.getErrataAndNotes())) {
            NavigationItem abstractNavItem = this.getNavigationItem(NavigationMenuOptions.ABSTRACT);
            if (abstractNavItem != null) {
                abstractNavItem.setShowBorder(true);
            }
        }
    }

}
