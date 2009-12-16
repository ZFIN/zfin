package org.zfin.antibody.presentation;

import org.zfin.antibody.Antibody;
import org.zfin.framework.presentation.EntityPresentation;

/**
 * Create a hyperlink to the antibody detail page.
 */
public class AntibodyPresentation extends EntityPresentation {

    private static final String uri = "antibody/detail?antibody.zdbID=";

    /**
     * Generates a link to the antibody detail page.
     *
     * @param antibody antibody
     * @return html for marker link
     */
    public static String getLink(Antibody antibody) {
        return getTomcatLink(uri, antibody.getZdbID(), antibody.getName(), null);
    }

    public static String getName(Antibody antibody) {
        return getSpanTag("none", antibody.getName(), antibody.getName());
    }

    /**
     * Should be of the form.
     * [atp6va0a1|http://zfin.org/cgi-bin/webdriver?MIval=aa-markerview.apg&OID=ZDB-GENE-030131-302|ATPase, H+ transporting, lysosomal V0 subunit a isoform 1]
     *
     * @param antibody Antibody to render.
     * @return A rendered wiki link.
     */
    public static String getWikiLink(Antibody antibody) {
        return getWikiLink("/action/" + uri, antibody.getZdbID(), antibody.getAbbreviation(), antibody.getName());
    }

}
