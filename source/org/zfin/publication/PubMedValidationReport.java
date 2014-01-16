package org.zfin.publication;

import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.properties.ZfinProperties;
import org.zfin.repository.SessionCreator;

import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

public class PubMedValidationReport {

    public static final String PUB_MED = "pubmed";

    private static String propertyFileDirectory;

    public static void main(String[] args) throws Exception {
        String propertyFilePath = args[0];
        PubMedValidationReport task = new PubMedValidationReport();
        task.propertyFileDirectory = propertyFilePath;
        task.init();
        task.execute();
    }

    private void init() {
        ZfinProperties.init(propertyFileDirectory);
        new HibernateSessionCreator(false);
    }

    public void execute() {
        List<Publication> publicationList = getPublicationRepository().getPublicationWithPubMedId(10000);
        for (Publication publication : publicationList) {
            checkValidPubMedRecord(publication);
        }
        if (errorList.size() > 0)
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
                errorList.add("Could not find PubMed record for " + publication.getZdbID() + "[" + publication.getAccessionNumber() + "]");
                return;
            }
            if (docSum.length > 1) {
                errorList.add("More than one PubMed record found for " + publication.getZdbID() + "[" + publication.getAccessionNumber() + "]");
                return;
            }
            StringBuilder builder = new StringBuilder();
            for (EUtilsServiceStub.DocSumType aDocSum : docSum) {
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
                        if (!publication.getPages().equals(value))
                            builder.append("Pages Mismatch: Is [" + publication.getPages() + "] Should [" + value + "]");
                    }
                    if (elementName.equals("DOI") && value != null) {
                        if (!publication.getDoi().equals(value))
                            builder.append("DOI Mismatch: Is [" + publication.getDoi() + "] Should [" + value + "]");
                    }
                    if (elementName.equals("Title") && value != null) {
                        if (value.endsWith("."))
                            value = value.substring(0, value.length() - 1);
                        if (!publication.getTitle().equalsIgnoreCase(value))
                            builder.append("Title Mismatch: Is [" + publication.getTitle() + "] Should [" + value + "]");
                    }
                }
                String fullVolumeString = volumeOne;
                if (volumeTwo != null)
                    fullVolumeString += "(" + volumeTwo + ")";
                if (!publication.getVolume().equals(fullVolumeString))
                    builder.append("Volume Mismatch: Is [" + publication.getVolume() + "] Should [" + fullVolumeString + "]");
            }
            if (builder.length() > 0) {
                String prefix = "Publication " + publication.getZdbID() + " [" + publication.getAccessionNumber() + "]: ";
                errorList.add(prefix + builder.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> errorList = new ArrayList<>();
}
