package org.zfin.publication;

import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

public class PubMedValidationReport extends AbstractValidateDataReportTask {

    public static final String PUB_MED = "pubmed";

    private static Integer numberOfPublicationsToScan;

    public static void main(String[] args) throws Exception {
        PubMedValidationReport task = new PubMedValidationReport();
        task.jobName = args[0];
        task.baseDir = args[1];
        task.propertyFilePath = args[2];
        if (args.length > 3)
            if (StringUtils.isNotEmpty(args[3]))
                numberOfPublicationsToScan = Integer.parseInt(args[3]);
        task.init();
        task.execute();
    }

    public void execute() {
        List<Publication> publicationList = getPublicationRepository().getPublicationWithPubMedId(numberOfPublicationsToScan);
        System.out.println(publicationList.size() + " publications are scanned");
        int index = -1;
        for (Publication publication : publicationList) {
            if (index++ % 10 == 0) {
                System.out.print(index);
                if (index % 1000 == 0)
                    System.out.println("...");
                else
                    System.out.print("...");
            }
            checkValidPubMedRecord(publication);
        }
        createErrorReport(null, pubMedIdNotFoundList, "faulty-PubMed-IDs");
        createErrorReport(null, pubInfoMismatchList, "faulty-Pub-Info");
        if (pubMedIdNotFoundList.size() > 0)
            for (String error : errorList)
                System.out.println(error);
    }

    private void checkValidPubMedRecord(Publication publication) {
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
                    if (publication.getPages() == null || !publication.getPages().equals(value)) {
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
            if (mismatch.hasValues())
                pubInfoMismatchList.add(mismatch.createPublicationMismatchElements(publication.getZdbID(), publication.getShortAuthorList(), publication.getAccessionNumber()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> getElementsList(String... elements) {
        List<String> listOfElements = new ArrayList<>(elements.length);
        Collections.addAll(listOfElements, elements);
        return listOfElements;
    }

    private List<String> errorList = new ArrayList<>();
    private List<List<String>> pubMedIdNotFoundList = new ArrayList<>();
    private List<List<String>> pubInfoMismatchList = new ArrayList<>();

    class PublicationMismatch {
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
