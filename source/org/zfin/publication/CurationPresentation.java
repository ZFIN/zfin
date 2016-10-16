package org.zfin.publication;

import org.zfin.framework.presentation.EntityPresentation;

/**
 * Presentation Class to create output from a Publication object.
 */
public class CurationPresentation extends EntityPresentation {

    public static enum CurationTab{
        GO,PHENO,FX,Experiment,Figure,Genotype,Feature,Construct ;
    }

    public static final String uri = "/action/curation/";
    private static final String noteuri="?MIval=aa-curatornote.apg&OID=";
    private static final String cookie = "&cookie=tab";
    private static String constructID;

    /**
     * Generates a Publication link using the name.
     *
     * @param publication Publication
     * @return html for Publication link
     */
    public static String getLink(Publication publication) {
        return getWebdriverLink(uri, publication.getZdbID(), publication.getShortAuthorList());
    }

    public static String getLink(Publication publication, CurationTab tab) {
        StringBuilder sb = getTomcatHyperLinkStart();
        sb.append(uri);
        sb.append(publication.getZdbID());
        sb.append(tab.name());
        sb.append("\">");
        sb.append(publication.getShortAuthorList());
        sb.append("</a>");
        return sb.toString();
    }

}


