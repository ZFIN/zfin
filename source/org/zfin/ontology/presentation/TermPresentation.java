package org.zfin.ontology.presentation;

import org.apache.log4j.Logger;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.mutant.presentation.PostComposedPresentationBean;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;

/**
 * Display a link to the term detail.
 */
public class TermPresentation extends EntityPresentation {

    private static final String POSTCOMPOSED_TERM_SEPARATOR = "&nbsp;";
    public static final String uri = "ontology/term-detail/";
    private static final String popupUri = "ontology/term-detail-popup?termID=";
    private static final String postComposedUri = "ontology/post-composed-term-detail?";
    private static final String postComposedPopupUri = "ontology/post-composed-term-detail-popup?";
    public static final String GO_URI = "http://www.ebi.ac.uk/QuickGO/GTerm?id=";

    private final static Logger logger = Logger.getLogger(TermPresentation.class);


    /**
     * Create hyperlink to term detail page.
     *
     * @param term              term
     * @param suppressPopupLink suppress popup link (true/false)
     * @return hyperlink hyperlink string
     */
    public static String getLink(Term term, boolean suppressPopupLink, String hyperlinkAttributeName) {
        if (term == null)
            return null;
        StringBuilder sb = new StringBuilder(50);
        if (StringUtils.isEmpty(hyperlinkAttributeName))
            sb.append(getLink(term, term.getTermName()));
        else
            sb.append(getLink(term, hyperlinkAttributeName));

        if (!suppressPopupLink)
            sb.append(getPopupLink(term));

        return sb.toString();
    }

    /**
     * Not doing anything special with term names yet, but if we use this method, we can later
     *
     * @param term Term object
     * @return name of term
     */
    public static String getName(Term term) {
        if (term == null)
            return null;
        return term.getTermName();
    }


    /**
     * Generates a term link using the Abbreviation
     * @param term term
     * @param name name attribute in hyperlink
     * @return html for term link
     */
    protected static String getLink(Term term, String name) {
        if (term == null || name == null)
            return null;
        if (term.getOntology() == null)
            return name;
        String title = term.getOntology().getCommonName();
        return getTomcatLinkWithTitle(uri, term.getOboID(), term.getTermName(), name, title);
    }


    public static String getPopupLink(Term term) {
        StringBuilder sb = new StringBuilder(100);
        sb.append(getTomcatPopupLink(popupUri, String.valueOf(term.getOboID()),
                "Term definition, synonyms and links"));
        return sb.toString();

    }

    public static String getLink(PostComposedPresentationBean entity, boolean suppressPopupLink) {

        if (entity == null) return null;

        StringBuilder sb = new StringBuilder(50);

        if (entity.getSubTermZdbId() == null) {

            sb.append(getTomcatLinkWithTitle(uri, entity.getSuperOntologyId(), entity.getSuperTermName(), entity.getSuperTermName(), null));

            if (!suppressPopupLink) {
                sb.append(getTomcatPopupLink(popupUri, String.valueOf(entity.getSuperOntologyId()),
                        "Term definition, synonyms and links"));
            }
            return sb.toString();
        }

        sb.append("<span class=\"post-composed-term-link\">");

        StringBuilder uriSuffix = new StringBuilder(50);
        uriSuffix.append("superTermID=");
        uriSuffix.append(entity.getSuperOntologyId());
        uriSuffix.append("&subTermID=");
        uriSuffix.append(entity.getSubOntologyId());
        sb.append(getTomcatLink(postComposedUri, uriSuffix.toString(), getName(entity)));
        sb.append("</span>");

        if (!suppressPopupLink) {
            StringBuilder popupSb = new StringBuilder(50);
            popupSb.append("superTermID=");
            popupSb.append(entity.getSuperOntologyId());
            popupSb.append("&subTermID=");
            popupSb.append(entity.getSubOntologyId());

            sb.append(getTomcatPopupLink(postComposedPopupUri, popupSb.toString(), "Term definition, synonyms and links"));
        }

        return sb.toString();

    }

    public static String getLink(PostComposedEntity entity, boolean suppressPopupLink) {
        if (entity == null)
            return null;
        if (entity.getSuperterm() == null)
            return null;
        if (entity.getSubterm() == null)
            return getLink(entity.getSuperterm(), suppressPopupLink);

        StringBuilder sb = new StringBuilder(50);
        sb.append("<span class=\"post-composed-term-link\">");

        StringBuilder uriSuffix = new StringBuilder(50);
        uriSuffix.append("superTermID=");
        uriSuffix.append(entity.getSuperterm().getOboID());
        uriSuffix.append("&subTermID=");
        uriSuffix.append(entity.getSubterm().getOboID());
        sb.append(getTomcatLink(postComposedUri, uriSuffix.toString(), getName(entity)));
        sb.append("</span>");

        if (!suppressPopupLink)
            sb.append(getPopupLink(entity));

        return sb.toString();
    }

    public static String getName(PostComposedPresentationBean entity) {
        if (entity == null)
            return null;
        if (entity.getSuperTermName() == null)
            return null;
        StringBuilder postComposedTermName = new StringBuilder(50);
        postComposedTermName.append("<span class=\"post-composed-term-name\">");
        postComposedTermName.append(entity.getSuperTermName());
        if (entity.getSubTermZdbId() != null) {
            postComposedTermName.append(POSTCOMPOSED_TERM_SEPARATOR);
            postComposedTermName.append(entity.getSubTermName());
        }
        postComposedTermName.append("</span>");

        return postComposedTermName.toString();
    }

    public static String getName(PostComposedEntity entity) {
        if (entity == null)
            return null;
        if (entity.getSuperterm() == null)
            return null;
        StringBuilder postComposedTermName = new StringBuilder(50);
        postComposedTermName.append("<span class=\"post-composed-term-name\">");
        postComposedTermName.append(getName(entity.getSuperterm()));
        if (entity.getSubterm() != null) {
            postComposedTermName.append(POSTCOMPOSED_TERM_SEPARATOR);
            postComposedTermName.append(getName(entity.getSubterm()));
        }
        postComposedTermName.append("</span>");

        return postComposedTermName.toString();
    }

    public static String getPopupLink(PostComposedEntity entity) {
        if (entity == null)
            return null;
        if (entity.getSubterm() == null)
            return getPopupLink(entity.getSuperterm());

        StringBuilder uriSuffix = new StringBuilder(50);
        uriSuffix.append("superTermID=");
        uriSuffix.append(entity.getSuperterm().getOboID());
        uriSuffix.append("&subTermID=");
        uriSuffix.append(entity.getSubterm().getOboID());

        return getTomcatPopupLink(postComposedPopupUri, uriSuffix.toString(), "Term definition, synonyms and links");
    }

    public static String getLinkByZfinEntity(ZfinEntity entity) {
        Term term = new GenericTerm();
        term.setZdbID(entity.getID());
        term.setTermName(entity.getName());
        return getLink(term, entity.getName());
    }

    public static String getLink(Term term, boolean suppressPopupLink) {
        return getLink(term, suppressPopupLink, null);
    }

    public static String getWikiLink(Term term) {
        if (term.getOntology().equals(Ontology.ANATOMY))
            return getWikiLink("", term.getOboID(), term.getTermName());
        else if (Ontology.isGoOntology(term.getOntology()))
            return getExternalWikiLink(GO_URI + term.getOboID(), term.getTermName());
        else {
            logger.error("unable to process term: " + term + " while generating wiki link");
            return null;
        }
    }

    public static String getLinkStartTag(Term term) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"/");
        sb.append(term.getOboID());
        sb.append("\">");
        return sb.toString();
    }

    public static String getLinkEndTag(Term term) {
        return "</a>";
    }

}
