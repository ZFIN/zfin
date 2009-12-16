package org.zfin.datatransfer.doi;

import org.apache.log4j.Logger;
import org.zfin.datatransfer.webservice.Citexplore;
import org.zfin.publication.Publication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * Class DOIHTTPTester tests
 */
public class DOIHTTPTester {


    private Logger logger = Logger.getLogger(DOIHTTPTester.class);

    public List<Publication> testDOIList(List<Publication> publications) {

//        System.err.println("dois to test: " + sources.size()) ;
        logger.info("dois to test: " + publications.size());
        Iterator<Publication> iter = publications.iterator();
        Publication pub;
        while (iter.hasNext()) {
            pub = iter.next();
            if (pub.getDoi() == null || checkPubExists(pub) == false) {
                logger.info("INVALID doi[" + pub.getDoi() + "]");
                iter.remove();
            } else {
                logger.info("VALID doi[" + pub.getDoi() + "]");
            }
        }
        logger.info("dois still valid: " + publications.size());
        return publications;
    }

    public boolean checkPubExists(Publication publication) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            String connectionString = Citexplore.DOI_URL + "/" + publication.getDoi();
            HttpURLConnection.setFollowRedirects(true);
            URL url = new URL(connectionString);
            connection = (HttpURLConnection) url.openConnection();
            if (connection.getResponseCode() != connection.HTTP_OK) {
                logger.warn("BAD Connection: " + connection.getResponseCode());
                return false;
            }
            // read stream
            // if title is 'Not Found', return false, else return true

            int i = 0;

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = reader.readLine();
            while (line != null) {
                if (line.indexOf("<title>") >= 0 || line.indexOf("<TITLE>") >= 0) {
                    if (line.indexOf("DOI Not Found") > 0) {
                        logger.info("bad DOI connection: " + publication.getDoi());
                        return false;
                    } else {
                        return true;
                    }
                }
                line = reader.readLine();
            }
            logger.warn("error in html:" + connectionString);
            return false;
        }
        catch (Exception e) {
            logger.error(e);
            return false;
        }
        finally {
            try {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    reader.close();
                }
            }
            catch (Exception e) {
                logger.error(e);
            }
        }

    }

} 


