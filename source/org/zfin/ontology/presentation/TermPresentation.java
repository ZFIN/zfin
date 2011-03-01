package org.zfin.ontology.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.Term;

/**
 * Display a link to the term detail.
 */
public class TermPresentation extends EntityPresentation {

    private static final String uri = "ontology/term-detail?termID=";

    /**
     * Create hyperlink to term detail page.
     *
     * @param term term
     * @return hyperlink
     */
    public static String getLink(Term term) {
        if (term == null)
            return null;
        if (term.getOntology() != null && term.getOntology().equals(Ontology.SPATIAL))
            return term.getTermName();
        else
            return getLink(term, term.getTermName());
    }

    /**
     * Generates a Marker link using the Abbreviation
     *
     * @param term term
     * @param name name attribute in hyperlink
     * @return html for term link
     */
    public static String getLink(Term term, String name) {
        if (term == null || name == null)
            return null;
        return getTomcatLinkWithTitle(uri, term.getZdbID(), term.getTermName(), name, term.getOntology().getCommonName());
    }

}
