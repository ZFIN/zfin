package org.zfin.publication.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.expression.Figure;
import org.zfin.profile.Person;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.profile.service.BeanCompareService;
import org.zfin.profile.service.BeanFieldUpdate;
import org.zfin.profile.service.ProfileService;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.publication.*;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 */
@Service
public class PublicationService {

    @Autowired
    private BeanCompareService beanCompareService;

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

        if (publication == null) {
            return false;
        }
        if (publication.isUnpublished()) {
            return false;
        }

        //if there any non-GELI figures, return true
        for (Figure figure : publication.getFigures()) {
            if (!figure.isGeli()) {
                return true;
            }
        }
        return false;
    }

    public static String getCurationStatusDisplay(Publication publication) {
        StringBuilder sb = new StringBuilder();
        PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
        PublicationTrackingHistory status = publicationRepository.currentTrackingStatus(publication);
        if (status != null) {
            sb.append(status.getStatus().getName())
                    .append(", ")
                    .append(DateFormat.getDateInstance(DateFormat.SHORT).format(status.getDate().getTime()));
        }
        return sb.toString();
    }

    public static String getLastAuthorCorrespondenceDisplay(Publication publication) {
        Set<CorrespondenceSentMessage> sentMessages = publication.getSentMessages();
        String messageType = "";
        Date latestMessageDate = null;
        if (CollectionUtils.isNotEmpty(sentMessages)) {
            latestMessageDate = sentMessages.iterator().next().getSentDate();
            messageType = "Sent";
        }
        Set<CorrespondenceReceivedMessage> receivedMessages = publication.getReceivedMessages();
        if (CollectionUtils.isNotEmpty(receivedMessages)) {
            Date receivedDate = receivedMessages.iterator().next().getDate();
            if (latestMessageDate == null || receivedDate.after(latestMessageDate)) {
                latestMessageDate = receivedDate;
                messageType = "Received";
            }
        }
        if (latestMessageDate == null) {
            return null;
        }
        return messageType + ", " + DateFormat.getDateInstance(DateFormat.SHORT).format(latestMessageDate);
    }

    public static boolean hasCorrespondence(Publication publication) {
        return CollectionUtils.isNotEmpty(publication.getSentMessages()) ||
                CollectionUtils.isNotEmpty(publication.getReceivedMessages());
    }

    public static List<String> getMeshTermDisplayList(Publication publication) {
        List<String> meshTermDisplays = new ArrayList<>();
        for (MeshHeading heading : publication.getMeshHeadings()) {
            meshTermDisplays.addAll(heading.getDisplayList());
        }
        return meshTermDisplays;
    }

    public static Boolean allowCuration(Publication publication) {
        if (publication.isUnpublished()) {
            return false;
        }
        if (publication.getType() == Publication.Type.ACTIVE_CURATION) {
            return false;
        }
        if (publication.getType() == Publication.Type.CURATION) {
            return false;
        }

        return true;
    }

    /* Rather than take a pub, this takes all of the separately generated counts
     * and returns true if any are non-zero */
    public static Boolean hasAdditionalData(Long... counts) {
        for (Long count : counts) {
            if (count > 0) return true;
        }
        return false;
    }


    public static String getExpressionAndPhenotypeLabel(Long expressionCount, Long phenotypeCount) {
        String label = "";

        if (expressionCount > 0 && phenotypeCount > 0) {
            label = "Expression and Phenotype Data";
        } else if (expressionCount == 0 && phenotypeCount > 0) {
            label = "Phenotype Data";
        } else if (expressionCount > 0 && phenotypeCount == 0) {
            label = "Expression Data";
        }

        return label;
    }

    public Collection<BeanFieldUpdate> mergePublicationFromForm(Publication formPub, Publication existingPub) throws Exception {
        Collection<BeanFieldUpdate> updates = new ArrayList<>();

        CollectionUtils.addIgnoreNull(updates, beanCompareService.compareBeanField("title", existingPub, formPub, true));
        CollectionUtils.addIgnoreNull(updates, beanCompareService.compareBeanField("status", existingPub, formPub, true));
        CollectionUtils.addIgnoreNull(updates, beanCompareService.compareBeanField("accessionNumber", existingPub, formPub, true));
        CollectionUtils.addIgnoreNull(updates, beanCompareService.compareBeanField("doi", existingPub, formPub, true));
        CollectionUtils.addIgnoreNull(updates, beanCompareService.compareBeanField("authors", existingPub, formPub, true));

        // handle date separately because 1) we don't want to compare hours, minutes, seconds and 2) need to get the
        // display right
        GregorianCalendar pubDate = existingPub.getPublicationDate();
        truncateDateToDay(pubDate);
        GregorianCalendar formDate = formPub.getPublicationDate();
        truncateDateToDay(formDate);
        if (!Objects.equals(pubDate, formDate)) {
            existingPub.setPublicationDate(formDate);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            BeanFieldUpdate update = new BeanFieldUpdate();
            update.setField("publicationDate");
            update.setFrom(pubDate == null ? "" : df.format(pubDate.getTime()));
            update.setTo(formDate == null ? "" : df.format(formDate.getTime()));
            updates.add(update);
        }

        // handle journal separately to get the display right
        Journal pubJournal = existingPub.getJournal();
        Journal formJournal = formPub.getJournal();
        if (!Objects.equals(pubJournal, formJournal)) {
            existingPub.setJournal(formJournal);
            BeanFieldUpdate update = new BeanFieldUpdate();
            update.setField("journal");
            update.setFrom(pubJournal == null ? "" : pubJournal.getAbbreviation());
            update.setTo(formJournal == null ? "" : formJournal.getAbbreviation());
            updates.add(update);
        }

        CollectionUtils.addIgnoreNull(updates, beanCompareService.compareBeanField("volume", existingPub, formPub, true));
        CollectionUtils.addIgnoreNull(updates, beanCompareService.compareBeanField("pages", existingPub, formPub, true));
        CollectionUtils.addIgnoreNull(updates, beanCompareService.compareBeanField("type", existingPub, formPub, true));
        CollectionUtils.addIgnoreNull(updates, beanCompareService.compareBeanField("keywords", existingPub, formPub, true));
        CollectionUtils.addIgnoreNull(updates, beanCompareService.compareBeanField("abstractText", existingPub, formPub, true));
        CollectionUtils.addIgnoreNull(updates, beanCompareService.compareBeanField("errataAndNotes", existingPub, formPub, true));
        CollectionUtils.addIgnoreNull(updates, beanCompareService.compareBeanField("canShowImages", existingPub, formPub, true));

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

    public List<String> splitAuthorListString(String authorListString) {
        List<String> strings = new ArrayList<>();

        //regex would be nice, but this is more straightforward
        String[] authorStringArray = authorListString.split(",");
        for (int i = 0; i < authorStringArray.length; i = i + 2) {
            if (i + 1 < authorStringArray.length) {
                String lastName = authorStringArray[i];
                String initials = authorStringArray[i + 1];

                if (lastName.contains(" and ")) {
                    lastName = lastName.replace(" and ", "");
                }

                strings.add(lastName.trim() + ", " + initials.trim());
            }
        }

        return strings;
    }

    public List<Person> getAuthorSuggestions(String authorString) {
        List<Person> suggestions = new ArrayList<>();

        String lastName = null;
        String firstInitial = null;

        try {
            lastName = authorString.split(",")[0];
            firstInitial = authorString.split(",")[1].substring(1, 2);
        } catch (ArrayIndexOutOfBoundsException e) {
            //if it couldn't split on a comma, don't even try to suggest anything
            return suggestions;
        }


        ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();

        suggestions.addAll(profileRepository.getPersonByLastNameEqualsAndFirstNameStartsWith(lastName, firstInitial));

        for (Person person : profileRepository.getPersonByLastNameStartsWithAndFirstNameStartsWith(lastName.trim(), firstInitial)) {
            if (!suggestions.contains(person)) {
                suggestions.add(person);
            }
        }

        for (Person person : profileRepository.getPersonByLastNameStartsWith(lastName.trim())) {
            if (!suggestions.contains(person)) {
                suggestions.add(person);
            }
        }


        return suggestions;
    }

    public PublicationFilePresentationBean convertToPublicationFilePresentationBean(PublicationFile file) {
        PublicationFilePresentationBean bean = new PublicationFilePresentationBean();
        bean.setId(file.getId());
        bean.setPubZdbId(file.getPublication().getZdbID());
        bean.setType(file.getType().getName().toString());
        bean.setFileName(ZfinPropertiesEnum.PDF_LOAD + "/" + file.getFileName());
        bean.setOriginalFileName(file.getOriginalFileName());
        return bean;
    }

    public PublicationFile processPublicationFile(Publication publication, String fileName, PublicationFileType fileType, InputStream fileData) throws IOException {
        File fileRoot = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString(), ZfinPropertiesEnum.PDF_LOAD.toString());

        String pubYear = Integer.toString(publication.getEntryDate().get(Calendar.YEAR));
        File yearDirectory = new File(fileRoot, pubYear);
        if (!yearDirectory.exists()) {
            boolean success = yearDirectory.mkdirs();
            if (!success) {
                throw new IOException("Unable to create directory: " + yearDirectory.getAbsolutePath());
            }
        }

        String storedFileName = publication.getZdbID();
        if (fileType.getName() == PublicationFileType.Name.ORIGINAL_ARTICLE) {
            storedFileName += ".pdf";
        } else {
            storedFileName += "-" + fileName;
        }

        File destination = new File(yearDirectory, storedFileName);
        Files.copy(fileData, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

        PublicationFile pubFile = new PublicationFile();
        pubFile.setType(fileType);
        pubFile.setFileName(pubYear + File.separator + storedFileName);
        pubFile.setOriginalFileName(fileName);
        pubFile.setPublication(publication);
        return pubFile;
    }

}
