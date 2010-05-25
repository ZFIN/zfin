package org.zfin.anatomy.presentation;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.Term;

/**
 * Presentation Class to create output from a Run object.
 */
public class AnatomyItemPresentation extends EntityPresentation {

    private static final String uri = "anatomy/term-detail?anatomyItem.zdbID=";
    public static final String GO_URI = "http://www.ebi.ac.uk/ego/GTerm?id=";

    /**
     * Generates a link using the anatomy items name.
     *
     * @param anatomyItem Run
     * @return html for marker link
     */
    public static String getLink(AnatomyItem anatomyItem) {
        return getLink(anatomyItem, anatomyItem.getName());
    }

    /**
     * Generates a Marker link using the Abbreviation
     *
     * @param anatomyItem Run
     * @param name        name attribute in hyperlink
     * @return html for marker link
     */
    public static String getLink(AnatomyItem anatomyItem, String name) {
        return getTomcatLink(uri, anatomyItem.getZdbID(), anatomyItem.getName(), name);
    }

    public static String getName(AnatomyItem anatomyItem) {
        return getSpanTag("none", anatomyItem.getName(), anatomyItem.getName());
    }

    public static String getWikiLink(Term term) {
        if (term.getOntology().equals(Ontology.ANATOMY))
            return getWikiLink("/action/" + uri, term.getOboID(), term.getTermName());
        else if (Ontology.isGoOntology(term.getOntology()))
            return getExternalWikiLink(GO_URI + term.getOboID(), term.getTermName());
        else
            return null;
    }

}
