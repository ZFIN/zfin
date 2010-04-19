package org.zfin.ontology.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.ontology.Term;

/**
 * Display a link to the term detail.
 */
public class TermPresentation extends EntityPresentation {

    private static final String uri = "dev-tools/ontology-term-detail?action=" + OntologyBean.ActionType.SHOW_TERM + "&termID=";

    /**
     * Create hyperlink to term detail page.
     *
     * @param term term
     * @return hyperlink
     */
    public static String getLink(Term term) {
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
        return getTomcatLink(uri, term.getID(), term.getTermName(), name);
    }

}
