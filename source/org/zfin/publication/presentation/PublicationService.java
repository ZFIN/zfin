package org.zfin.publication.presentation;

import org.apache.commons.lang.StringUtils;
import org.zfin.expression.Figure;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;

import javax.servlet.ServletContext;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class PublicationService {

    public static final String SESSION_PUBLICATIONS = "SessionPublications";

    private static String generateKey(String key) {
        String user = getUserName();
        return SESSION_PUBLICATIONS + (StringUtils.isEmpty(key) ? "" : key) + (StringUtils.isEmpty(user) ? "" : user);
    }

    private static String getUserName() {
        Person aPerson = ProfileService.getCurrentSecurityUser();
        String userName;
        if (aPerson == null) {
            userName = "anonymous";
        } else {
            userName = aPerson.getUsername();
        }
        return userName;
    }

    public static List<Publication> addRecentPublications(ServletContext servletContext, Publication publication, String key) {
        String generatedKey = generateKey(key);
        List<Publication> mostRecentsPubs = (List<Publication>) servletContext.getAttribute(generatedKey);
        if (mostRecentsPubs == null) {
            mostRecentsPubs = new ArrayList<Publication>();
        }

        if (publication != null) {
            mostRecentsPubs.add(publication);
            servletContext.setAttribute(generatedKey, mostRecentsPubs);
        }
        return mostRecentsPubs;
    }

    public static List<Publication> getRecentPublications(ServletContext servletContext, String key) {
        String generatedKey = generateKey(key);
        return (List<Publication>) servletContext.getAttribute(generatedKey);
    }

    public static Boolean showFiguresLink(Publication publication) {

        if (publication == null) { return false; }
        if (publication.isUnpublished()) { return false; }

        //if there any non-GELI figures, return true
        for (Figure figure : publication.getFigures()) {
            if (!figure.isGeli()) { return true; }
        }
        return false;
    }

    public static String getCurationStatusDisplay(Publication publication) {
        StringBuilder sb = new StringBuilder();

        if (publication.getCloseDate() != null) {
            sb.append("Closed ");
            sb.append( DateFormat.getDateInstance(DateFormat.SHORT).format(publication.getCloseDate().getTime()));
        } else {
            sb.append("Open");
        }

        if (publication.isIndexed()) {
            sb.append(", Indexed ");
            sb.append(DateFormat.getDateInstance(DateFormat.SHORT).format(publication.getIndexedDate().getTime()));
        } else {

        }


        return sb.toString();
    }

    public static Boolean allowCuration(Publication publication) {
        if (publication.isUnpublished()){ return false; }
        if (publication.getType() == Publication.Type.ACTIVE_CURATION) { return false; }
        if (publication.getType() == Publication.Type.CURATION) { return false; }

        return true;
    }

    /* Rather than take a pub, this takes all of the separately generated counts
     * and returns true if any are non-zero */
    public static Boolean hasAdditionalData(Long... counts) {
        for (Long count : counts) { if (count > 0) return true; }
        return false;
    }


    public static String getExpressionAndPhenotypeLabel(Long expressionCount,Long phenotypeCount) {
        String label = "";

        if (expressionCount > 0 && phenotypeCount > 0) { label = "Expression and Phenotype Data"; }
        else if (expressionCount == 0 && phenotypeCount > 0) { label = "Phenotype Data"; }
        else if (expressionCount > 0 && phenotypeCount == 0) { label = "Expression Data"; }

        return label;
    }




}
