package org.zfin.ontology.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.ontology.GoTerm;

/**
 * Defines the basic link a GoTerm is hyperlinked to.
 */
public class GoTermPresentation extends EntityPresentation {

    public static final String uri = "http://www.ebi.ac.uk/ego/GTerm?id=GO:";

    /**
     * Generates a Go term link using the go id
     *
     * @param term Go term
     * @return html for marker link
     */
    public static String getLink(GoTerm term) {
        return getGeneralHyperLink(uri + term.getGoID(), term.getName());
    }

    /**
     * Generate the name of the link
     *
     * @param term go term
     * @return name of the hyerplink.
     */
    public static String getName(GoTerm term) {
        return getSpanTag("none", term.getName(), term.getName());
    }

}
