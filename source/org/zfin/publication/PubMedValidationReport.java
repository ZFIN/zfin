package org.zfin.publication;

import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.infrastructure.ant.ReportConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.cli.OptionBuilder.withArgName;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

@SuppressWarnings("AccessStaticViaInstance")
public class PubMedValidationReport extends AbstractValidateDataReportTask {

    public static final String PUB_MED = "pubmed";

    private static Integer numberOfPublicationsToScan;
    private static List<Publication> publicationList;
    private static final Logger LOG = Logger.getLogger(PubMedValidationReport.class);

    static {
        options.addOption(withArgName("jobName").hasArg().withDescription("Job Name").create("jobName"));
        options.addOption(withArgName("baseDir").hasArg().withDescription("Base Directory").create("baseDir"));
        options.addOption(withArgName("propertyFilePath").hasArg().withDescription("Path to zfin.properties").create("propertyFilePath"));
        options.addOption(withArgName("numOfPublication").hasArg().withDescription("Maximum number of Publications to scan").create("numOfPublication"));
        options.addOption(withArgName("threads").hasArg().withDescription("Maximum number of threads to use").create("threads"));
    }

    private static int numOfThreads = 1;

    public static void main(String[] arguments) throws Exception {
        initializeLog4J();
        LOG.info("Start Comparing Publications: ZFIN - PubMed");
        CommandLine commandLine = parseArguments(arguments, "comparing publications on ZFIN and PubMed ");
        PubMedValidationReport task = new PubMedValidationReport();
        task.jobName = commandLine.getOptionValue("jobName");
        task.propertyFilePath = commandLine.getOptionValue("propertyFilePath");
        if (StringUtils.isNotEmpty(commandLine.getOptionValue("numOfPublication")))
            numberOfPublicationsToScan = Integer.parseInt(commandLine.getOptionValue("numOfPublication"));
        if (StringUtils.isNotEmpty(commandLine.getOptionValue("threads")))
            numOfThreads = Integer.parseInt(commandLine.getOptionValue("threads"));

        task.init(commandLine.getOptionValue("baseDir"));
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
        publicationList = getPublicationRepository().getPublicationWithPubMedId(numberOfPublicationsToScan);
        LOG.info(numOfThreads + " threads used");
        LOG.info(publicationList.size() + " publications are scanned");
        for (int index = 0; index < numOfThreads; index++) {
            Thread thread = new Thread(new CheckPublications());
            thread.start();
        }
        while (publicationList.size() > 0) {
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        createReports();
        LOG.info("Finished");
        return (pubInfoMismatchList.size() > 0 || pubMedIdNotFoundList.size() > 0) ? 1 : 0;
    }

    private static class CheckPublications implements Runnable {

        @Override
        public void run() {
            Publication publication;
            while ((publication = getPublication()) != null) {
                checkValidPubMedRecord(publication);
            }
        }

    }

    private synchronized static Publication getPublication() {
        if (publicationList.size() > 0) {
            if (publicationList.size() % 1000 == 0) {
                LOG.info(publicationList.size() + " left");
            }
            return publicationList.remove(0);
        }
        return null;
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

    private static void checkValidPubMedRecord(Publication publication) {
        try {
            EUtilsServiceStub service = new EUtilsServiceStub();
            // call NCBI ESummary utility
            EUtilsServiceStub.ESummaryRequest req = new EUtilsServiceStub.ESummaryRequest();
            req.setDb(PUB_MED);
            req.setId(publication.getAccessionNumber());
            EUtilsServiceStub.ESummaryResult res = service.run_eSummary(req);
            // results output
            EUtilsServiceStub.DocSumType[] docSum = res.getDocSum();
            if (docSum == null) {
                pubMedIdNotFoundList.add(getElementsList(publication.getShortAuthorList(), publication.getZdbID(), publication.getAccessionNumber()));
                return;
            }
            if (docSum.length > 1) {
                errorList.add("More than one PubMed record found for " + publication.getZdbID() + "[" + publication.getAccessionNumber() + "]");
                return;
            }
            PublicationMismatch mismatch = new PublicationMismatch();
            EUtilsServiceStub.DocSumType aDocSum = docSum[0];

            String volumeOne = null;
            String volumeTwo = null;
            for (int k = 0; k < aDocSum.getItem().length; k++) {

                String elementName = aDocSum.getItem()[k].getName();
                String value = aDocSum.getItem()[k].getItemContent();
                if (elementName.equals("Volume")) {
                    volumeOne = value;
                }
                if (elementName.equals("Issue")) {
                    volumeTwo = value;
                }
                if (elementName.equals("Pages")) {
                    if (!isSamePageNumbers(publication, value)) {
                        mismatch.setPages(publication.getPages());
                        mismatch.setPagesExternal(value);
                    }
                }
                if (elementName.equals("DOI") && value != null) {
                    if (publication.getDoi() == null || !publication.getDoi().equals(value)) {
                        mismatch.setDoi(publication.getDoi());
                        mismatch.setDoiExternal(value);
                    }
                }
                if (elementName.equals("Title") && value != null) {
                    if (value.endsWith("."))
                        value = value.substring(0, value.length() - 1);
                    if (value.startsWith("[") && value.endsWith("]"))
                        value = value.substring(1, value.length() - 1);
                    if (publication.getTitle() != null && publication.getTitle().startsWith("Chapter ") && publication.getTitle().endsWith(value))
                        continue;
                    if (publication.getTitle() == null || !publication.getTitle().equalsIgnoreCase(value)) {
                        mismatch.setTitle(publication.getTitle());
                        mismatch.setTitleExternal(value);
                    }
                }
            }
            String fullVolumeString = volumeOne;
            if (volumeTwo != null)
                fullVolumeString += "(" + volumeTwo + ")";
            if (publication.getVolume() == null || !publication.getVolume().equals(fullVolumeString)) {
                mismatch.setVolume(publication.getVolume());
                mismatch.setVolumeExternal(fullVolumeString);
            }
            if (mismatch.hasValues()) {
                pubInfoMismatchList.add(mismatch.createPublicationMismatchElements(publication.getZdbID(), publication.getShortAuthorList(), publication.getAccessionNumber()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCompletePageNumbers(String value) {
        if (StringUtils.isEmpty(value))
            return "";
        StringBuilder builder = new StringBuilder();
        String[] pageValues = value.split("-");
        if (pageValues.length == 1)
            return value;
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
        if (diff < 0)
            return value;
        if (end.length() == start.length())
            builder.append(end);
        else {
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
        if (StringUtils.equals(pages, value))
            return true;
        // if not the same check if auto-completion of the second page number will make it equal
        return StringUtils.equals(pages, getCompletePageNumbers(value));
    }

    private static List<String> getElementsList(String... elements) {
        List<String> listOfElements = new ArrayList<>(elements.length);
        Collections.addAll(listOfElements, elements);
        return listOfElements;
    }

    private static List<String> errorList = new ArrayList<>();
    private static List<List<String>> pubMedIdNotFoundList = new ArrayList<>();
    private static List<List<String>> pubInfoMismatchList = new ArrayList<>();

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

        public List<String> createPublicationMismatchElements(String pubID, String authors, String pubMedID) {
            return getElementsList(authors, pubID, pubMedID,
                    doi == null ? "" : doi, doiExternal,
                    pages == null ? "" : pages, pagesExternal,
                    volume == null ? "" : volume, volumeExternal,
                    title == null ? "" : title, titleExternal);
        }
    }
}
