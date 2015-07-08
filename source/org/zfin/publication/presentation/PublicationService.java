package org.zfin.publication.presentation;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.expression.Figure;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;

import javax.servlet.ServletContext;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 */
public class PublicationService {

    public static final String SESSION_PUBLICATIONS = "SessionPublications";

    private static final Logger LOG = Logger.getLogger(PublicationService.class);

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

    public static PublicationForm getPublicationFormFromPublication(Publication publication) {
        PublicationForm publicationForm = new PublicationForm();
        publicationForm.setZdbID(publication.getZdbID());
        publicationForm.setTitle(publication.getTitle());
        publicationForm.setStatus(publication.getStatus());
        publicationForm.setPubMedID(publication.getAccessionNumber());
        publicationForm.setDoi(publication.getDoi());
        publicationForm.setAuthors(publication.getAuthors());
        publicationForm.setDate(publication.getPublicationDate());
        publicationForm.setJournal(publication.getJournal());
        publicationForm.setVolume(publication.getVolume());
        publicationForm.setPages(publication.getPages());
        publicationForm.setType(publication.getType());
        publicationForm.setKeywords(publication.getKeywords());
        publicationForm.setAbstractText(publication.getAbstractText());
        publicationForm.setNotes(publication.getErrataAndNotes());
        return publicationForm;
    }

    public static void applyFormToPublication(Publication publication, PublicationForm form) {
        publication.setTitle(form.getTitle());
        publication.setStatus(form.getStatus());
        publication.setAccessionNumber(form.getPubMedID());
        publication.setDoi(form.getDoi());
        publication.setAuthors(form.getAuthors());
        publication.setPublicationDate(form.getDate());
        publication.setJournal(form.getJournal());
        publication.setVolume(form.getVolume());
        publication.setPages(form.getPages());
        publication.setType(form.getType());
        publication.setKeywords(form.getKeywords());
        publication.setAbstractText(form.getAbstractText());
        publication.setErrataAndNotes(form.getNotes());
    }

    public static Map<String, List<String>> getUpdates(Publication publication, PublicationForm form) {
        Map<String, List<String>> updates = new HashMap<>();
        PublicationFormUpdateHelper updateHelper = new PublicationFormUpdateHelper(publication, form);
        updateHelper.compareField(updates, "Title");
        updateHelper.compareField(updates, "Status");
        updateHelper.compareField(updates, "PubMed ID", "accessionNumber", "pubMedID");
        updateHelper.compareField(updates, "DOI");
        updateHelper.compareField(updates, "Authors");

        // handle date separately because 1) we don't want to compare hours, minutes, seconds and 2) need to get the
        // display right
        Calendar pubDate = publication.getPublicationDate();
        truncateDateToDay(pubDate);
        Calendar formDate = form.getDate();
        truncateDateToDay(formDate);
        if (!Objects.equals(pubDate, formDate)) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            updates.put("Publication Date", Arrays.asList(
                    pubDate == null ? "" : df.format(pubDate.getTime()),
                    formDate == null ? "" : df.format(formDate.getTime())
            ));
        }

        updateHelper.compareField(updates, "Journal", "journal.abbreviation");
        updateHelper.compareField(updates, "Volume");
        updateHelper.compareField(updates, "Pages");
        updateHelper.compareField(updates, "Type");
        updateHelper.compareField(updates, "Keywords");
        updateHelper.compareField(updates, "Abstract", "abstractText");
        updateHelper.compareField(updates, "Errata & Notes", "errataAndNotes", "notes");
        return updates;
    }

    private static void truncateDateToDay(Calendar cal) {
        if (cal != null) {
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
    }

    private static class PublicationFormUpdateHelper {

        private Publication pub;
        private PublicationForm form;

        public PublicationFormUpdateHelper(Publication pub, PublicationForm form) {
            this.pub = pub;
            this.form = form;
        }

        public void compareField(Map<String, List<String>> updates, String fieldName) {
            compareField(updates, fieldName, fieldName.toLowerCase());
        }

        public void compareField(Map<String, List<String>> updates, String fieldName, String propertyName) {
            compareField(updates, fieldName, propertyName, propertyName);
        }

        public void compareField(Map<String, List<String>> updates, String fieldName, String pubProperty, String formProperty) {
            Object pubValue = getProperty(pub, pubProperty);
            Object formValue = getProperty(form, formProperty);
            if (!Objects.equals(formValue, pubValue)) {
                updates.put(fieldName, Arrays.asList(
                        Objects.toString(pubValue, ""),
                        Objects.toString(formValue, "")));
            }
        }

        private static Object getProperty(Object bean, String field) {
            try {
                return PropertyUtils.getProperty(bean, field);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NestedNullException e) {
                return null;
            }
        }

    }
}
