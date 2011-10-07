package org.zfin.datatransfer.webservice;

import org.apache.log4j.Logger;
import org.zfin.publication.Publication;
import uk.ac.ebi.cdb.webservice.Doc2LocResultBean;
import uk.ac.ebi.cdb.webservice.Doc2LocResultListBean;
import uk.ac.ebi.cdb.webservice.WSCitationImpl;
import uk.ac.ebi.cdb.webservice.WSCitationImplService;

import javax.xml.ws.WebServiceRef;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;


/**
 * Class Citexplore.  Collects DOIs from the Citexplorer webservice.
 */
public class Citexplore {

    @WebServiceRef(wsdlLocation = "http://www.ebi.ac.uk/webservices/citexplore/v1.0/service?wsdl")
    private static final WSCitationImplService service = new WSCitationImplService();
    private final String PMID_TOKEN = "pmid";
    public final static String DOI_URL = "http://dx.doi.org";

    private Logger logger = Logger.getLogger(Citexplore.class);


    /**
     * getDoisForPubmedID.
     * Note:  This is a destructive method which changes the input structure.
     * Iterates through the Publication list and updates the DOI, if it exists, from the CiteXplorer Webservice.
     * The DOI is the key and doc2loc is the webservice method from the compiled client-side webservice code.
     * Many IDs are returned, one of which may be the DOI, which always contains the DOI_URL link.
     *
     * @param publicationList Publications with accession numbers but no DOIs.
     * @return List<Publication>
     */
//    @SuppressWarnings({"PointlessBooleanExpression"})
    public List<Publication> getDoisForPubmedID(List<Publication> publicationList) {
        try {
            logger.debug("Invoking doc2loc operation on wscitationImpl port");
            Doc2LocResultListBean doc2LocResultListBean;
            List<Doc2LocResultBean> doc2LocResultBeanCollection;
            String urlString, doiValue;
            int counter = 0;
            boolean hasDOI;
            int initSize = publicationList.size();

            WSCitationImpl port = service.getWSCitationImplPort();
            Iterator<Publication> iter = publicationList.iterator();
            while (iter.hasNext()) {
                Publication publication = iter.next();
                try {
                    doc2LocResultListBean = port.doc2Loc(PMID_TOKEN, publication.getAccessionNumber());
                    doc2LocResultBeanCollection = doc2LocResultListBean.getDoc2LocResultBeanCollection();
                    hasDOI = false;
                    for (Doc2LocResultBean doc2LocResultBean : doc2LocResultBeanCollection) {
                        urlString = doc2LocResultBean.getUrl();
                        // Valid DOI's do not end with "/"
                        if (urlString.contains(DOI_URL) && false==urlString.endsWith("/")) {
                            doiValue = urlString.substring(DOI_URL.length() + 1);
                            logger.info("added doi[" + doiValue + "]  for pmid[" + publication.getAccessionNumber() + "]");
                            publication.setDoi(doiValue);
                            hasDOI = true;
                        }
                    }


                    if (hasDOI == false) {
                        logger.info("doi not found for pmid[" + publication.getAccessionNumber() + "]");
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
        }
        catch (Exception e) {
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



