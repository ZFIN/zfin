package org.zfin.publication;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.zfin.datatransfer.ServiceConnectionException;
import org.zfin.datatransfer.webservice.NCBIRequest;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.infrastructure.ant.ReportConfiguration;

import java.util.*;

import static org.apache.commons.cli.OptionBuilder.withArgName;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

@SuppressWarnings("AccessStaticViaInstance")
public class PubMedValidationReport extends AbstractValidateDataReportTask {

    public static final String PUB_MED = "pubmed";

    private static Integer numberOfPublicationsToScan;
    private static Map<Integer, Publication> publicationMap;
    private static final Logger LOG = Logger.getLogger(PubMedValidationReport.class);
    private static List<String> errorList = new ArrayList<>();
    private static List<List<String>> pubMedIdNotFoundList = new ArrayList<>();
    private static List<List<String>> pubInfoMismatchList = new ArrayList<>();
    private static List<Integer> processedIds = new ArrayList<>();

    static {
        options.addOption(withArgName("jobName").hasArg().withDescription("Job Name").create("jobName"));
        options.addOption(withArgName("baseDir").hasArg().withDescription("Base Directory").create("baseDir"));
        options.addOption(withArgName("propertyFilePath").hasArg().withDescription("Path to zfin.properties").create("propertyFilePath"));
        options.addOption(withArgName("numOfPublication").hasArg().withDescription("Maximum number of Publications to scan").create("numOfPublication"));
    }

    public PubMedValidationReport(String jobName, String propertyFilePath, String baseDir) {
        super(jobName, propertyFilePath, baseDir);
    }

    public static void main(String[] arguments) throws Exception {
        initializeLog4J();
        LOG.info("Start Comparing Publications: ZFIN - PubMed");
        CommandLine commandLine = parseArguments(arguments, "comparing publications on ZFIN and PubMed ");
        String jobName1 = commandLine.getOptionValue("jobName");
        String propertyFilePath1 = commandLine.getOptionValue("propertyFilePath");
        String baseDir = commandLine.getOptionValue("baseDir");
        PubMedValidationReport task = new PubMedValidationReport(jobName1, propertyFilePath1, baseDir);
        if (StringUtils.isNotEmpty(commandLine.getOptionValue("numOfPublication"))) {
            numberOfPublicationsToScan = Integer.parseInt(commandLine.getOptionValue("numOfPublication"));
        }
        task.initDatabase();
        System.exit(task.execute());
    }

    protected static void initializeLog4J() {
        initLog4J();
        LOG.setLevel(Level.INFO);
    }

    @Override
    public int execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();
        buildPublicationMap();
        int batchSize = 5000;
        LOG.info(publicationMap.size() + " publications are scanned");
        List<Integer> pubIds = new ArrayList<>(publicationMap.keySet());
        for (int start = 0; start < pubIds.size(); start += batchSize) {
            int end = Math.min(start + batchSize, pubIds.size());
            String idList = StringUtils.join(pubIds.subList(start, end), ",");
            LOG.info("Fetching pubs " + (start + 1) + " - " + end);
            try {
                Document results = new NCBIRequest(NCBIRequest.Eutil.FETCH)
                        .with("db", PUB_MED)
                        .with("id", idList)
                        .go();
                NodeList articles = results.getElementsByTagName("PubmedArticle");
                for (int i = 0; i < articles.getLength(); i++) {
                    Element article = (Element) articles.item(i);
                    checkValidPubMedRecord(article);
                }
            } catch (ServiceConnectionException e) {
                LOG.error(e);
                return 1;
            }
        }
        pubIds.removeAll(processedIds);
        for (Integer accessionNumber : pubIds) {
            Publication publication = publicationMap.get(accessionNumber);
            String accession = null;
            if(accessionNumber != null)
                accession = accessionNumber.toString();
            pubMedIdNotFoundList.add(getElementsList(publication.getShortAuthorList(), publication.getZdbID(), accession));
        }
        createReports();
        LOG.info("Finished");
        return (pubInfoMismatchList.size() > 0 || pubMedIdNotFoundList.size() > 0) ? 1 : 0;
    }

    public void createReports() {
        String templateName = jobName + ".faulty-PubMed-IDs";
        ReportConfiguration reportConfiguration = new ReportConfiguration(jobName, dataDirectory, templateName, true);
        createErrorReport(null, pubMedIdNotFoundList, reportConfiguration);

        templateName = jobName + ".faulty-Pub-Info";
        reportConfiguration = new ReportConfiguration(jobName, dataDirectory, templateName, true);
        createErrorReport(null, pubInfoMismatchList, reportConfiguration);

        if (pubMedIdNotFoundList.size() > 0) {
            for (String error : errorList) {
                System.out.println(error);
            }
        }
    }

    private static void buildPublicationMap() {
        List<Publication> pubs = getPublicationRepository().getPublicationWithPubMedId(numberOfPublicationsToScan);
        publicationMap = new HashMap<>();
        for (Publication pub : pubs) {
            publicationMap.put(pub.getAccessionNumber(), pub);
        }
    }

    private static void checkValidPubMedRecord(Element article) {
        String pmid = article.getElementsByTagName("PMID").item(0).getTextContent();
        Integer accession= null;
        if(StringUtils.isNumeric(pmid))
            accession = Integer.parseInt(pmid);
        processedIds.add(accession);
        Publication publication = publicationMap.get(pmid);
        PublicationMismatch mismatch = new PublicationMismatch();

        String volumeOne = null;
        String volumeTwo = null;
        NodeList volume = article.getElementsByTagName("Volume");
        if (volume.getLength() > 0) {
            volumeOne = volume.item(0).getTextContent();
        }
        NodeList issue = article.getElementsByTagName("Issue");
        if (issue.getLength() > 0) {
            volumeTwo = issue.item(0).getTextContent();
        }

        NodeList pages = article.getElementsByTagName("Pagination");
        if (pages.getLength() > 0) {
            String value = pages.item(0).getTextContent();
            if (!isSamePageNumbers(publication, value)) {
                mismatch.setPages(publication.getPages());
                mismatch.setPagesExternal(value);
            }
        }

        NodeList idList = article.getElementsByTagName("ArticleId");
        for (int i = 0; i < idList.getLength(); i++) {
            Node articleId = idList.item(i);
            Node idType = articleId.getAttributes().getNamedItem("IdType");
            if (idType != null && idType.getTextContent().equals("doi")) {
                String value = articleId.getTextContent();
                if (publication.getDoi() == null || !publication.getDoi().equals(value)) {
                    mismatch.setDoi(publication.getDoi());
                    mismatch.setDoiExternal(value);
                }
            }
        }

        NodeList title = article.getElementsByTagName("ArticleTitle");
        if (title.getLength() > 0) {
            String value = title.item(0).getTextContent();
            if (value.endsWith(".")) {
                value = value.substring(0, value.length() - 1);
            }
            if (value.startsWith("[") && value.endsWith("]")) {
                value = value.substring(1, value.length() - 1);
            }
            if (publication.getTitle() != null &&
                    publication.getTitle().startsWith("Chapter ") &&
                    publication.getTitle().endsWith(value)) {
                return;
            }
            if (publication.getTitle() == null || !publication.getTitle().equalsIgnoreCase(value)) {
                mismatch.setTitle(publication.getTitle());
                mismatch.setTitleExternal(value);
            }
        }

        String fullVolumeString = volumeOne;
        if (volumeTwo != null) {
            fullVolumeString += "(" + volumeTwo + ")";
        }
        if (publication.getVolume() == null || !publication.getVolume().equals(fullVolumeString)) {
            mismatch.setVolume(publication.getVolume());
            mismatch.setVolumeExternal(fullVolumeString);
        }
        if (mismatch.hasValues()) {
            pubInfoMismatchList.add(mismatch.createPublicationMismatchElements(publication.getZdbID(), publication.getShortAuthorList(), publication.getAccessionNumber()));
        }
    }

    public static String getCompletePageNumbers(String value) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        String[] pageValues = value.split("-");
        if (pageValues.length == 1) {
            return value;
        }
        if (pageValues.length > 2) {
            String errorMessage = "Could not parse page numbers with more than three components: " + value;
            LOG.error(errorMessage);
        }
        String start = pageValues[0];
        String end = pageValues[1];
        builder.append(start);
        builder.append("-");
        // complete end number
        int diff = start.length() - end.length();
        if (diff < 0) {
            return value;
        }
        if (end.length() == start.length()) {
            builder.append(end);
        } else {
            char[] characters = start.toCharArray();
            for (int index = 0; index < diff; index++) {
                builder.append(characters[index]);
            }
            builder.append(end);
        }

        return builder.toString();
    }

    public static boolean isSamePageNumbers(Publication publication, String value) {
        String pages = publication.getPages();
        if (StringUtils.equals(pages, value)) {
            return true;
        }
        // if not the same check if auto-completion of the second page number will make it equal
        return StringUtils.equals(pages, getCompletePageNumbers(value));
    }

    private static List<String> getElementsList(String... elements) {
        List<String> listOfElements = new ArrayList<>(elements.length);
        Collections.addAll(listOfElements, elements);
        return listOfElements;
    }


    static class PublicationMismatch {
        private String doi;
        private String volume;
        private String pages;
        private String title;
        private String doiExternal;
        private String volumeExternal;
        private String pagesExternal;
        private String titleExternal;

        public boolean hasValues() {
            if (doiExternal != null) return true;
            if (volumeExternal != null) return true;
            if (pagesExternal != null) return true;
            if (titleExternal != null) return true;
            return false;
        }

        public String getDoi() {
            return doi;
        }

        public void setDoi(String doi) {
            this.doi = doi;
        }

        public String getVolume() {
            return volume;
        }

        public void setVolume(String volume) {
            this.volume = volume;
        }

        public String getPages() {
            return pages;
        }

        public void setPages(String pages) {
            this.pages = pages;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDoiExternal() {
            return doiExternal;
        }

        public void setDoiExternal(String doiExternal) {
            this.doiExternal = doiExternal;
        }

        public String getVolumeExternal() {
            return volumeExternal;
        }

        public void setVolumeExternal(String volumeExternal) {
            this.volumeExternal = volumeExternal;
        }

        public String getPagesExternal() {
            return pagesExternal;
        }

        public void setPagesExternal(String pagesExternal) {
            this.pagesExternal = pagesExternal;
        }

        public String getTitleExternal() {
            return titleExternal;
        }

        public void setTitleExternal(String titleExternal) {
            this.titleExternal = titleExternal;
        }

        public List<String> createPublicationMismatchElements(String pubID, String authors, Integer pubMedID) {
            String accession = null;
            if(pubMedID != null)
                accession = pubMedID.toString();
            return getElementsList(authors, pubID, accession,
                    doi == null ? "" : doi, doiExternal,
                    pages == null ? "" : pages, pagesExternal,
                    volume == null ? "" : volume, volumeExternal,
                    title == null ? "" : title, titleExternal);
        }
    }
}
