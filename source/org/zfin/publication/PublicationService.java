package org.zfin.publication;

import org.apache.commons.lang.StringUtils;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class PublicationService {

    public static final String MOST_RECENT_PUBLICATIONS = "MostRecentPublications";

    public static List<Publication> addRecentPublications(ServletContext servletContext, Publication publication) {
        List<Publication> mostRecentsPubs = (List<Publication>) servletContext.getAttribute(MOST_RECENT_PUBLICATIONS);
        if (mostRecentsPubs == null){
            mostRecentsPubs = new ArrayList<Publication>();
        }

        if (publication!=null) {
            mostRecentsPubs.add(publication);
            servletContext.setAttribute(MOST_RECENT_PUBLICATIONS, mostRecentsPubs);
        }
        return mostRecentsPubs;
    }

    public static List<Publication> getRecentPublications(ServletContext servletContext) {
        return (List<Publication>) servletContext.getAttribute(MOST_RECENT_PUBLICATIONS);
    }
}
