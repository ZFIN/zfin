package org.zfin.anatomy.presentation;

import org.apache.log4j.Logger;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.Term;

/**
 * Presentation Class to create output from a Run object.
 */
public class AnatomyItemPresentation extends EntityPresentation {

    private final static Logger logger = Logger.getLogger(AnatomyItemPresentation.class);

    private static final String uri = "anatomy/term-detail?anatomyItem.zdbID=";
    public static final String GO_URI = "http://www.ebi.ac.uk/ego/GTerm?id=";
    private static final String popupUri = "ontology/term-detail-popup?termID=";

    /**
     * Generates a link using the anatomy items name.
     *
     * @param anatomyItem Run
     * @return html for marker link
     */
    public static String getLink(AnatomyItem anatomyItem) {
        return getLink(anatomyItem, anatomyItem.getTermName(),false);
    }

    /**
     * Generates a Marker link using the Abbreviation
     *
     *
     * @param anatomyItem Run
     * @param name        name attribute in hyperlink
     * @param suppressPopupLink
     * @return html for marker link
     */
    public static String getLink(AnatomyItem anatomyItem, String name, boolean suppressPopupLink) {
        StringBuilder sb = new StringBuilder();
        sb.append(getTomcatLink(uri, anatomyItem.getZdbID(), anatomyItem.getTermName(), name));
        if(!suppressPopupLink){
            sb.append(getPopupLink(anatomyItem));
        }
        return sb.toString();
    }

    private static Object getPopupLink(AnatomyItem anatomyItem) {
        StringBuilder sb = new StringBuilder(100);
        sb.append(getTomcatPopupLink(popupUri, String.valueOf(anatomyItem.getOboID()),
                "Term definition, synonyms and links"));
        return sb.toString();
    }

    public static String getName(AnatomyItem anatomyItem) {
        return getSpanTag("none", anatomyItem.getTermName(), anatomyItem.getTermName());
    }

    public static String getWikiLink(Term term) {
        if (term.getOntology().equals(Ontology.ANATOMY))
            return getWikiLink("/action/" + uri, term.getOboID(), term.getTermName());
        else if (Ontology.isGoOntology(term.getOntology()))
            return getExternalWikiLink(GO_URI + term.getOboID(), term.getTermName());
        else {
            logger.error("unable to process term: " + term + " while generating wiki link");
            return null;
        }
    }

}
