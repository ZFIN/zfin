package org.zfin.antibody.presentation;

import org.zfin.antibody.Antibody;
import org.zfin.framework.presentation.EntityPresentation;

/**
 * Create a hyperlink to the antibody detail page.
 */
public class AntibodyPresentation extends EntityPresentation {

    /**
     * Generates a link to the antibody detail page.
     *
     * @param antibody antibody
     * @return html for marker link
     */
    public static String getLink(Antibody antibody) {
        return getViewLink(antibody.getZdbID(), antibody.getName());
    }

    public static String getName(Antibody antibody) {
        return getSpanTag("none", antibody.getName(), antibody.getName());
    }

    /**
     * Should be of the form.
     * [atp6va0a1|http://zfin.org/actio/marker/view/ZDB-GENE-030131-302|ATPase, H+ transporting, lysosomal V0 subunit a isoform 1]
     *
     * @param antibody Antibody to render.
     * @return A rendered wiki link.
     */
    public static String getWikiLink(Antibody antibody) {
        return getWikiLink("", antibody.getZdbID(), antibody.getName(), antibody.getName());
    }

}
