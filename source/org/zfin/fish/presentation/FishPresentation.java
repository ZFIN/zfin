package org.zfin.fish.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.mutant.Fish;

/**
 * Create a links and name html div's .
 */
public class FishPresentation extends EntityPresentation {

    private static final String uri = "/action/fish/fish-detail/";
    private static final String popupUri = "fish/fish-detail-popup/";

    /**
     * Generates a Go stage link using the go id
     *
     * @param fish Go fish page
     * @return html for marker link
     */
    public static String getLink(MartFish fish) {
        if (fish.getGenotypeID() != null) {
            return getGeneralHyperLink(uri + fish.getFishID(), getName(fish)) +
                    getTomcatPopupLink(popupUri, fish.getFishID(), "More details about this fish");
        } else {
            return null;
        }
    }

    public static String getLink(Fish fish, Boolean suppressPopupLink) {
        String link = getViewLink(fish.getZdbID(), fish.getName(), fish.getName(), null);
        if (!suppressPopupLink) {
            link += getTomcatPopupLink(popupUri, fish.getZdbID(), "More details about this fish");
        }
        return link;
    }

    /**
     * Generate the name of the link
     *
     * @param fish MartFish
     * @return name of the hyperlink.
     */
    public static String getName(MartFish fish) {
        String stageName;
        stageName = fish.getName();

        return getSpanTag("fish", stageName, stageName);
    }


    public static String getName(Fish fish) { return getSpanTag("fish", fish.getName(), fish.getName());  }

}
