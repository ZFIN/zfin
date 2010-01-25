package org.zfin.anatomy.presentation;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.ontology.GoTerm;
import org.zfin.ontology.presentation.GoTermPresentation;

/**
 * Presentation Class to create output from a Run object.
 */
public class AnatomyItemPresentation extends EntityPresentation {

    private static final String uri = "anatomy/term-detail?anatomyItem.zdbID=";

    /**
     * Generates a link using the anatomy items name.
     *
     * @return html for marker link
     * @param anatomyItem Run
     */
    public static String getLink(AnatomyItem anatomyItem) {
        return getLink(anatomyItem,anatomyItem.getName()) ;
    }
    /**
     * Generates a Marker link using the Abbreviation
     *
     * @return html for marker link
     * @param anatomyItem Run
     * @param name name attribute in hyperlink
     */
    public static String getLink(AnatomyItem anatomyItem, String name) {
        return getTomcatLink(uri, anatomyItem.getZdbID(), anatomyItem.getName(), name);
    }

    public static String getName(AnatomyItem anatomyItem) {
        return getSpanTag("none", anatomyItem.getName(), anatomyItem.getName());
    }

    public static String getWikiLink(AnatomyItem anatomyItem) {
        return getWikiLink("/action/"+uri,anatomyItem.getZdbID(),anatomyItem.getName()) ;
    }

    public static String getWikiLink(GoTerm goTerm) {
        return getExternalWikiLink(GoTermPresentation.uri+goTerm.getGoID(),goTerm.getName())  ;
    }

}
