package org.zfin.ontology.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.ontology.TermExternalReference;

/**
 * Create link to external reference
 */
public class TermExternalReferencePresentation extends EntityPresentation {

    /**
     * Create hyperlink to term detail page.
     *
     * @param termXref term
     * @return hyperlink
     */
    public static String getLink(TermExternalReference termXref) {
        if (termXref == null)
            return null;
        if (termXref.getForeignDB() == null)
            return termXref.getFullAccession();
        else
            return getLink(termXref, termXref.getFullAccession());
    }

    /**
     * Generates a Term hyperlink
     *
     * @param termXref term
     * @param name     name attribute in hyperlink
     * @return html for term link
     */
    public static String getLink(TermExternalReference termXref, String name) {
        if (name == null)
            return null;
        return "<a href=\"" + termXref.getXrefUrl() + "\">" + name + "</a>";
    }

}
