package org.zfin.datatransfer.doi;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.zfin.datatransfer.webservice.Citexplore;
import org.zfin.framework.HibernateUtil;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.HibernatePublicationRepository;
import org.zfin.publication.repository.PublicationRepository;

import java.util.ArrayList;
import java.util.List;


/**
 * Update ZFIN Publication's DOI Ids if they are missing. Use external site (PubMed Central) to obtain Ids.
 */
public class DOIProcessor {

    public static final int ALL = -1;

    private int maxToProcess = ALL;
    private int maxAttempts = 50;
    private PublicationRepository publicationRepository = new HibernatePublicationRepository();
    private Logger logger = Logger.getLogger(DOIProcessor.class);

    private boolean reportAll = false;
    private boolean doisUpdated = false;

    private List<String> messages = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public DOIProcessor(boolean reportAll) {
        this(reportAll, Integer.MAX_VALUE);
    }

    public DOIProcessor(boolean reportAll, int maxAttempts) {
        this.reportAll = reportAll;
        if (maxAttempts == ALL) {
            this.maxAttempts = Integer.MAX_VALUE;
        } else {
            this.maxAttempts = maxAttempts;
        }

    }

    public DOIProcessor(boolean reportAll, int maxAttempts, int maxToProcess) {
        this.reportAll = reportAll;
        this.maxToProcess = (maxToProcess == ALL ? Integer.MAX_VALUE : maxToProcess);
        this.maxAttempts = (maxAttempts == ALL ? Integer.MAX_VALUE : maxAttempts);
    }


    /**
     * Gets publications that have pubMed Ids with no IDs from the database.
     *
     * @return List<Publication> Returns list of publications without a DOI.
     */
    private List<Publication> getPubmedIdsWithNoDOIs() {
        List<Publication> publicationList = publicationRepository.getPublicationsWithAccessionButNoDOIAndLessAttempts(maxAttempts, maxToProcess);
        if (reportAll || CollectionUtils.isNotEmpty(publicationList)) {
            messages.add("number of dois to populate: " + publicationList.size());
        }
        return publicationList;
    }


    /**
     * Find publications with missing DOI Ids in ZFIN, retrieve those publications from Citexplore
     * (Europe PubMed Central webservice) and obtain DOI IDs. Update ZFIN publication with DOI Id.
     * If no DOI ID available do nothing.
     */
    public void findAndUpdateDOIs() {
        try {
            List<Publication> publicationList = getPubmedIdsWithNoDOIs();
            publicationRepository.addDOIAttempts(publicationList);
            HibernateUtil.currentSession().flush();
            Citexplore wsdlConnect = new Citexplore();
            publicationList = wsdlConnect.getDoisForPubmedID(publicationList);
            DOIHTTPTester httpTester = new DOIHTTPTester();
            publicationList = httpTester.testDOIList(publicationList);
            for (Publication publication : publicationList) {
                messages.add("added doi[" + publication.getDoi() + "] for publication[" + publication.getZdbID() + "]");
            }
            updateDOIs(publicationList);
            HibernateUtil.closeSession();
        } catch (Exception e) {
            logger.error(e);
            errors.add(ExceptionUtils.getFullStackTrace(e));
        }
    }


    /**
     * updateDOIs:  sets DOI for ZDB_ID
     *
     * @param publicationList A list of publications.
     */
    private void updateDOIs(List<Publication> publicationList) {

        if (publicationList == null || publicationList.size() == 0) {
            messages.add("No sources to update");
            doisUpdated = false;
            return;
        } else {
            doisUpdated = true;
        }
        HibernateUtil.createTransaction();
        try {
            publicationRepository.updatePublications(publicationList);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            logger.error(e);
            errors.add(ExceptionUtils.getFullStackTrace(e));
            HibernateUtil.rollbackTransaction();
        }

    }

    public List<String> getMessages() {
        return messages;
    }

    public List<String> getErrors() {
        return errors;
    }

    public boolean isDoisUpdated() {
        return doisUpdated;
    }

    public static void main(String[] args) {
        try {
            DOIProcessor driver = new DOIProcessor(true);
            driver.findAndUpdateDOIs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
