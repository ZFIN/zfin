package org.zfin.fish.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.mutant.Fish;

/**
 * Create a links and name html div's .
 */
public class FishPresentation extends EntityPresentation {

    private static final String uri = "/action/fish/fish-detail/";
    private static final String popupUri = "fish/fish-detail-popup/";

    public static String getLink(Fish fish, Boolean suppressPopupLink) {

        String name = fish.getDisplayName();
        String link = getViewLink(fish.getZdbID(), name, name, null);
        if (!suppressPopupLink) {
            link += getTomcatPopupLink(popupUri, fish.getZdbID(), "More details about this fish");
        }
        return link;
    }


    public static String getName(Fish fish) {
        return getSpanTag("fish", fish.getName(), fish.getDisplayName());
    }

}
