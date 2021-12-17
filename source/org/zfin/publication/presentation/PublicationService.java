package org.zfin.publication.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.expression.Figure;
import org.zfin.framework.api.FieldFilter;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mutant.repository.PhenotypeRepository;
import org.zfin.profile.Person;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.profile.service.BeanCompareService;
import org.zfin.profile.service.BeanFieldUpdate;
import org.zfin.profile.service.ProfileService;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.publication.*;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.Category;
import org.zfin.search.FieldName;
import org.zfin.search.service.SolrService;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class PublicationService {

    @Autowired
    private BeanCompareService beanCompareService;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @Autowired
    private PhenotypeRepository phenotypeRepository;

    public static final String SESSION_PUBLICATIONS = "SessionPublications";

    private static final Logger LOG = LogManager.getLogger(PublicationService.class);

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

    public Boolean showFiguresLink(Publication publication) {

        if (publication == null) {
            return false;
        }
        if (publication.isUnpublished() && CollectionUtils.size(publication.getFigures()) > 100) {
            return false;
        }
        if (publication.getJournal().getZdbID().equals("ZDB-JRNL-181119-2")) {
            return true;
        }
        //if there any non-GELI figures, return true
        for (Figure figure : publication.getFigures()) {
            if (!figure.isGeli()) {
                return true;
            }
        }
        return false;
    }

    public String getCurationStatusDisplay(Publication publication) {
        StringBuilder sb = new StringBuilder();
        PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
        PublicationTrackingHistory status = publicationRepository.currentTrackingStatus(publication);
        if (status != null) {
            sb.append(status.getStatus().getName().toString())
                    .append(", ")
                    .append(DateFormat.getDateInstance(DateFormat.SHORT).format(status.getDate().getTime()));
        }
        return sb.toString();
    }

    public String getLastAuthorCorrespondenceDisplay(Publication publication) {
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

    public boolean hasCorrespondence(Publication publication) {
        return CollectionUtils.isNotEmpty(publication.getSentMessages()) ||
                CollectionUtils.isNotEmpty(publication.getReceivedMessages());
    }

    public List<String> getMeshTermDisplayList(Publication publication) {
        List<String> meshTermDisplays = new ArrayList<>();
        for (MeshHeading heading : publication.getMeshHeadings()) {
            meshTermDisplays.addAll(heading.getDisplayList());
        }
        return meshTermDisplays;
    }

    public Boolean allowCuration(Publication publication) {
        return publication.getCuratable();
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
        CollectionUtils.addIgnoreNull(updates, beanCompareService.compareBeanField("curatable", existingPub, formPub, true));

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

        ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();

        suggestions.addAll(profileRepository.getPeopleByFullName(authorString));

        String lastName = null;
        String firstInitial = null;

        if (authorString.contains(",")) {
            lastName = authorString.split(",")[0];
            firstInitial = authorString.split(",")[1].substring(1, 2);

            for (Person person : profileRepository.getPersonByLastNameEqualsAndFirstNameStartsWith(lastName.trim(), firstInitial)) {
                if (!suggestions.contains(person)) {
                    suggestions.add(person);
                }
            }

            for (Person person : profileRepository.getPersonByLastNameStartsWithAndFirstNameStartsWith(lastName.trim(), firstInitial)) {
                if (!suggestions.contains(person)) {
                    suggestions.add(person);
                }
            }
        } else {
            lastName = authorString;
        }

        for (Person person : profileRepository.getPersonByLastNameEquals(lastName.trim())) {
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

    public PublicationFile processPublicationFile(Publication publication, String fileName, PublicationFileType fileType, InputStream fileData) throws IOException {
        File fileRoot = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString());

        String pubYear = Integer.toString(publication.getEntryDate().get(Calendar.YEAR));
        File yearDirectory = new File(fileRoot, pubYear);
        if (!yearDirectory.exists()) {
            boolean success = yearDirectory.mkdirs();
            if (!success) {
                throw new IOException("Unable to create directory: " + yearDirectory.getAbsolutePath());
            }
        }

        File publicationDirectory = new File(fileRoot, pubYear + "/" + publication.getZdbID());
        if (!publicationDirectory.exists()) {
            boolean success = publicationDirectory.mkdirs();
            if (!success) {
                throw new IOException("Unable to create directory: " + publicationDirectory.getAbsolutePath());
            }
        }

        String storedFileName = publication.getZdbID() + "/" + publication.getZdbID();
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

    public boolean publicationHasFigureWithLabel(Publication publication, String label) {
        return publication.getFigures().stream().anyMatch(fig -> fig.getLabel().equals(label));
    }

    public List<DataLinkBean> getPublicationDataLinks(Publication publication) {
        List<DataLinkBean> links = new ArrayList<>();

        long markerCount = publicationRepository.getMarkerCount(publication);
        if (markerCount > 0) {
            links.add(new DataLinkBean(
                    "/action/publication/" + publication.getZdbID() + "/genes",
                    "Genes / Markers",
                    markerCount
            ));
        }

        long morpholinoCount = publicationRepository.getMorpholinoCount(publication);
        if (morpholinoCount > 0) {
            links.add(new DataLinkBean(
                    "/action/publication/" + publication.getZdbID() + "/strs?type=MRPHLNO",
                    "Morpholino",
                    morpholinoCount
            ));
        }

        long talenCount = publicationRepository.getTalenCount(publication);
        if (talenCount > 0) {
            links.add(new DataLinkBean(
                    "/action/publication/" + publication.getZdbID() + "/strs?type=TALEN",
                    "TALEN",
                    talenCount
            ));
        }

        long crisprCount = publicationRepository.getCrisprCount(publication);
        if (crisprCount > 0) {
            links.add(new DataLinkBean(
                    "/action/publication/" + publication.getZdbID() + "/strs?type=CRISPR",
                    "CRISPR",
                    crisprCount
            ));
        }

        long antibodyCount = publicationRepository.getAntibodyCount(publication);
        if (antibodyCount > 0) {
            links.add(new DataLinkBean(
                    "/action/antibody/antibodies-per-publication/" + publication.getZdbID(),
                    "Antibodies",
                    antibodyCount
            ));
        }

        long efgCount = publicationRepository.getEfgCount(publication);
        if (efgCount > 0) {
            links.add(new DataLinkBean(
                    "/action/publication/" + publication.getZdbID() + "/efgs",
                    "Engineered Foreign Genes",
                    efgCount
            ));
        }

        long cloneCount = publicationRepository.getCloneProbeCount(publication);
        if (cloneCount > 0) {
            links.add(new DataLinkBean(
                    "/action/publication/" + publication.getZdbID() + "/clones",
                    "Clones and Probes",
                    cloneCount
            ));
        }

        long expressionCount = publicationRepository.getExpressionCount(publication);
        long phenotypeCount = publicationRepository.getPhenotypeCount(publication);
        if (showFiguresLink(publication) && (expressionCount > 0 || phenotypeCount > 0)) {
            String label = getExpressionAndPhenotypeLabel(expressionCount, phenotypeCount);
            links.add(new DataLinkBean(
                    "/action/figure/all-figure-view/" + publication.getZdbID(),
                    label
            ));
        }

        long mappingCount = publicationRepository.getMappingDetailsCount(publication);
        if (mappingCount > 0) {
            links.add(new DataLinkBean(
                    "/action/mapping/publication/" + publication.getZdbID(),
                    "Mapping Details",
                    mappingCount
            ));
        }

        long featureCount = publicationRepository.getFeatureCount(publication);
        if (featureCount > 0) {
            links.add(new DataLinkBean(
                    "/action/publication/" + publication.getZdbID() + "/feature-list",
                    "Mutations and Transgenics",
                    featureCount
            ));
        }

        long fishCount = publicationRepository.getFishCount(publication);
        if (fishCount > 0) {
            links.add(new DataLinkBean(
                    "/action/publication/" + publication.getZdbID() + "/fish-list",
                    "Fish",
                    fishCount
            ));
        }

        long orthologyCount = publicationRepository.getOrthologyCount(publication);
        if (orthologyCount > 0) {
            links.add(new DataLinkBean(
                    "/action/publication/" + publication.getZdbID() + "/orthology-list",
                    "Orthology",
                    orthologyCount
            ));
        }

        if (CollectionUtils.isNotEmpty(phenotypeRepository.getHumanDiseaseModels(publication.getZdbID()))) {
            links.add(new DataLinkBean(
                    "/action/publication/" + publication.getZdbID() + "/disease",
                    "Human Disease / Zebrafish Model Data"
            ));
        }

        return links;
    }

    public List<String> getAuthorStringList(List<PubmedPublicationAuthor> authors) {
        List<String> strings = new ArrayList<>();

        for (PubmedPublicationAuthor author : authors) {
            strings.add(author.getLastName().trim() + ", " + author.getFirstName().trim());
        }

        return strings;
    }

    private SolrQuery getCitationQuery(String xref, List<PublicationType> excludeTypes) {
        SolrQuery query = new SolrQuery();
        query.addFilterQuery(FieldName.CATEGORY.getName() + ":" + Category.PUBLICATION.getName());
        query.addFilterQuery(FieldName.XREF.getName() + ":" + xref);
        query.setFields(FieldName.ID.getName());
        excludeTypes.forEach(type -> query.addFilterQuery("-" + FieldName.PUBLICATION_TYPE + ":\"" + type.toString() + "\""));
        return query;
    }

    public JsonResultResponse<Publication> getCitationsByXref(String xref, List<PublicationType> excludeTypes, Pagination pagination) throws IOException, SolrServerException {
        SolrQuery query = getCitationQuery(xref, excludeTypes);
        String filter = pagination.getFieldFilter(FieldFilter.CITATION);
        if (StringUtils.isNotEmpty(filter)) {
            query.addFilterQuery(FieldName.FULL_NAME_AC.getName() + ":(" + filter + ")");
        }
        query.setStart(pagination.getStart());
        query.setRows(pagination.getLimit());

        switch (StringUtils.defaultString(pagination.getSortBy())) {
            case "Year, Oldest":
                query.addSort(SortClause.asc(FieldName.YEAR.getName()));
                query.addSort(SortClause.asc(FieldName.AUTHOR_SORT.getName()));
                break;
            case "First Author, A to Z":
                query.addSort(SortClause.asc(FieldName.AUTHOR_SORT.getName()));
                query.addSort(SortClause.desc(FieldName.YEAR.getName()));
                break;
            case "First Author, Z to A":
                query.addSort(SortClause.desc(FieldName.AUTHOR_SORT.getName()));
                query.addSort(SortClause.desc(FieldName.YEAR.getName()));
                break;
            default:
                query.addSort(SortClause.desc(FieldName.YEAR.getName()));
                query.addSort(SortClause.asc(FieldName.AUTHOR_SORT.getName()));
        }

        QueryResponse queryResponse = SolrService.getSolrClient().query(query);
        List<Publication> publications = queryResponse.getResults().stream()
                .map(doc -> (String) doc.getFieldValue(FieldName.ID.getName()))
                .map(publicationRepository::getPublication)
                .collect(Collectors.toList());

        JsonResultResponse<Publication> response = new JsonResultResponse<>();
        response.setResults(publications);
        response.setTotal(queryResponse.getResults().getNumFound());

        return response;
    }

    public void getAllCitationsByXref(String xref, List<PublicationType> excludedTypes, Consumer<List<Publication>> consumer) throws IOException, SolrServerException {
        SolrQuery query = getCitationQuery(xref, excludedTypes);
        SolrService.getAllResults(query, response -> {
            List<String> ids = response.getResults().stream()
                    .map(doc -> (String) doc.getFieldValue(FieldName.ID.getName()))
                    .collect(Collectors.toList());
            List<Publication> pubs = publicationRepository.getPublications(ids);
            consumer.accept(pubs);
        });
    }

    public Publication getPublication(String zdbID) {
        Publication publication = publicationRepository.getPublication(zdbID);
        if (publication == null) {
            String replacedZdbID = infrastructureRepository.getReplacedZdbID(zdbID);
            if (replacedZdbID != null) {
                publication = publicationRepository.getPublication(replacedZdbID);
            }
        }
        return publication;
    }
}
