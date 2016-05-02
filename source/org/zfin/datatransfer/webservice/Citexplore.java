package org.zfin.datatransfer.webservice;

import ebi.ws.client.ResponseWrapper;
import ebi.ws.client.Result;
import ebi.ws.client.WSCitationImpl;
import ebi.ws.client.WSCitationImplService;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.publication.Publication;

import javax.xml.ws.WebServiceRef;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;


/**
 * Retrieves DOIs from the Citexplore webservice for given pubMed ID (= accessionNumber).
 */
public class Citexplore {

    @WebServiceRef(wsdlLocation = "http://www.ebi.ac.uk/europepmc/webservices/soap?wsdl")
    private static final WSCitationImplService service = new WSCitationImplService();
    public final static String DOI_URL = "http://dx.doi.org";

    private Logger logger = Logger.getLogger(Citexplore.class);


    /**
     * Iterates through the Publication list and updates the DOI, if it exists, from the CiteXplorer Webservice.
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

            WSCitationImpl port = service.getWSCitationImplPort();
            Iterator<Publication> iter = publicationList.iterator();
            while (iter.hasNext()) {
                Publication publication = iter.next();
                try {
                    String query = publication.getAccessionNumber();
                    String resultType = "lite";
                    String email = "cmpich@zfin.org";
                    ResponseWrapper response = port.searchPublications(query, resultType, 0, "25",false, email);
                    if (response.getHitCount() == 0) {
                        logger.debug("No Publication with accession number " + publication.getAccessionNumber() + " found in Europe PubMed Central");
                    }
                    if (response.getHitCount() > 1)
                        logger.debug("More than one Publication with accession number " + publication.getAccessionNumber() + " found in Europe PubMed Central");
                    hasDOI = false;
                    String doiValue;
                    for (Result publicationBean : response.getResultList().getResult()) {
                        doiValue = publicationBean.getDOI();
                        if (StringUtils.isNotEmpty(doiValue) && publicationBean.getId().equals(publication.getAccessionNumber())) {
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

} 



