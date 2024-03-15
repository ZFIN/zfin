package org.zfin.framework.presentation;


import lombok.Getter;
import lombok.Setter;
import org.springframework.ui.Model;

@Getter
@Setter
public class TranscriptNavigationMenu extends NavigationMenu {

    /**
     * Set up the navigation menu.
     * This uses the builder pattern available on the NavigationItem class through lombok
     */
    public TranscriptNavigationMenu() {
        super(
            title(NavigationMenuOptions.SUMMARY),
            title(NavigationMenuOptions.SEQUENCE),
            title(NavigationMenuOptions.RELATED_TRANSCRIPTS),
            title(NavigationMenuOptions.SEGEMENT_RELATIONSHIPS),
            title(NavigationMenuOptions.PROTEIN_PRODUCTS),
            title(NavigationMenuOptions.SUPPORTING_SEQUENCE),
            title(NavigationMenuOptions.DB_LINKS).requireRoot(true),
            title(NavigationMenuOptions.CITATION)
        );

        //If we aren't using the counter on the left hand navigation, turn it off for all menu items
        //We can remove this once this functionality is no longer behind a flag
        getNavigationItems().forEach(item -> item.setShowCount(false));

    }

    /**
     * Pass the page model for publication-view into this method.
     * It will set up the logic of which menu items to show based on the
     * contents of the model.
     *
     * @param model The page model for publication-view
     */
    public void setModel(Model model) {
    }

}
