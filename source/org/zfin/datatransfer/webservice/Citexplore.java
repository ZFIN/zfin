package org.zfin.datatransfer.webservice;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.publication.Publication;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;


/**
 * Retrieves DOIs from the Citexplore webservice for given pubMed ID (= accessionNumber).
 */
public class Citexplore {

    public final static String DOI_URL = "http://dx.doi.org";
    public final static String EPMC_SEARCH = "http://www.ebi.ac.uk/europepmc/webservices/rest/search";

    private Logger logger = Logger.getLogger(Citexplore.class);

    /**
     * Iterates through the Publication list and updates the DOI, if it exists, from the Europe PMC REST service.
     *
     * @param publicationList Publications with accession numbers but no DOIs.
     * @return List<Publication>
     */
    public List<Publication> getDoisForPubmedID(List<Publication> publicationList) {
        logger.setLevel(Level.INFO);
        try {
            int counter = 0;
            boolean hasDOI;
            int initSize = publicationList.size();

            Iterator<Publication> iter = publicationList.iterator();
            while (iter.hasNext()) {
                Publication publication = iter.next();
                try {
                    URL request = new URIBuilder(EPMC_SEARCH)
                            .addParameter("query", "ext_id:" + publication.getAccessionNumber())
                            .addParameter("format", "json")
                            .build()
                            .toURL();

                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    EPMCSearchResponse response = mapper.readValue(request, EPMCSearchResponse.class);

                    if (response.getHitCount() == 0) {
                        logger.debug("No Publication with accession number " + publication.getAccessionNumber() + " found in Europe PubMed Central");
                    }

                    if (response.getHitCount() > 1) {
                        logger.debug("More than one Publication with accession number " + publication.getAccessionNumber() + " found in Europe PubMed Central");
                    }

                    hasDOI = false;
                    String doiValue;
                    for (EPMCResult result : response.getResultList().getResult()) {
                        doiValue = result.getDoi();
                        if (StringUtils.isNotEmpty(doiValue) && result.getId().equals(publication.getAccessionNumber())) {
                            logger.debug("added doi[" + doiValue + "]  for pmid[" + publication.getAccessionNumber() + "]");
                            publication.setDoi(doiValue);
                            hasDOI = true;
                        }
                    }

                    if (!hasDOI) {
                        logger.debug("doi not found for PubMed ID[" + publication.getAccessionNumber() + "]");
                        publication.setDoi(null);
                        iter.remove();
                    }

                    ++counter;
                    if (counter % 100 == 0) {
                        printStatus(counter, initSize);
                    }
                } catch (Exception e) {
                    logger.error("protocol exception getting doi for pub: " + publication, e);
                }
            }
        } catch (Exception e) {
            logger.error("Unable to access dois:\n" + e);
        }

        return publicationList;
    }


    private void printStatus(int counter, int initSize) {
        NumberFormat nf = NumberFormat.getPercentInstance();
        double percent = (((double) counter / ((double) initSize - 1.0)));
        logger.info(counter + " of " + initSize + " = " + nf.format(percent));
    }

    static private class EPMCSearchResponse {
        private int hitCount;
        private EPMCResultList resultList;

        public int getHitCount() {
            return hitCount;
        }

        public void setHitCount(int hitCount) {
            this.hitCount = hitCount;
        }

        public EPMCResultList getResultList() {
            return resultList;
        }

        public void setResultList(EPMCResultList resultList) {
            this.resultList = resultList;
        }
    }

    static private class EPMCResultList {
        List<EPMCResult> result;

        public List<EPMCResult> getResult() {
            return result;
        }

        public void setResult(List<EPMCResult> result) {
            this.result = result;
        }
    }

    static private class EPMCResult {
        private Integer id;
        private String doi;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getDoi() {
            return doi;
        }

        public void setDoi(String doi) {
            this.doi = doi;
        }
    }

} 



