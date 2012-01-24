package org.zfin.fish.presentation;

import org.zfin.framework.presentation.EntityPresentation;

/**
 * Create a links and name html div's .
 */
public class FishPresentation extends EntityPresentation {

    private static final String uri = "/action/fish/fish-detail/";

    /**
     * Generates a Go stage link using the go id
     *
     * @param fish Go fish page
     * @return html for marker link
     */
    public static String getLink(Fish fish) {
        return getGeneralHyperLink(uri + fish.getGenotypeExperimentIDsString(), getName(fish));
    }

    /**
     * Generate the name of the link
     *
     * @param fish Fish
     * @return name of the hyperlink.
     */
    public static String getName(Fish fish) {
        String stageName;
        stageName = fish.getName();

        return getSpanTag("fish", stageName, stageName);
    }

}
