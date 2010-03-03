package org.zfin.publication.presentation;

import org.apache.commons.lang.StringUtils;
import org.zfin.people.Person;
import org.zfin.publication.Publication;

import javax.servlet.ServletContext;
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
        Person aPerson = Person.getCurrentSecurityUser();
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
}
