package org.zfin.ontology.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.TermDTO;

/**
 * Display a link to the term detail.
 */
public class TermDTOPresentation extends EntityPresentation {

    private static final String uri = "ontology/term-detail?termID=";

    /**
     * Create hyperlink to term detail page.
     *
     * @param term term
     * @return hyperlink
     */
    public static String getLink(TermDTO term) {
        if (term == null)
            return null;
        if (term.getOntology() != null && term.getOntology().equals(OntologyDTO.SPATIAL))
            return term.getName();
        else
            return getLink(term, term.getName());
    }

    /**
     * Generates a Marker link using the Abbreviation
     *
     * @param term term
     * @param name name attribute in hyperlink
     * @return html for term link
     */
    public static String getLink(TermDTO term, String name) {
        if (term == null || name == null)
            return null;
        return getTomcatLinkWithTitle(uri, term.getZdbID(), term.getName(), name, term.getOntology().getOntologyName());
    }

}
