package org.zfin.httpunittest;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.zfin.AbstractSmokeTest;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Main Integration Test class.
 * Tests main pages of ZFIN.
 * @deprecated  Use JWebUnit style smoke test.
 */
public class MarkerViewSmokeTest extends AbstractSmokeTest{


    private static final Logger LOG = Logger.getLogger(MarkerViewSmokeTest.class);

    static private HashMap<String, Object> mimeTypes = new HashMap<String, Object>();

    static {
        mimeTypes.put("text/html;charset=ISO-8859-1", Boolean.TRUE);
        mimeTypes.put("text/html", Boolean.TRUE);
    }

    static HashMap<String, String> markerTypes = new HashMap<String, String>();

    private StringBuffer testUrl = new StringBuffer();

    static {
        markerTypes.put("fgf8a", "Gene");
        markerTypes.put("zgc:55262", "Gene");
        markerTypes.put("wu:cegs655", "Gene");
        markerTypes.put("si:dkey-21k10.2", "Gene");
        markerTypes.put("cb46", "EST");
        markerTypes.put("MGC:55191", "cDNA");
        markerTypes.put("CH73-1F23", "BAC");
        markerTypes.put("CH1073-16E3", "FOSMID");
        markerTypes.put("BUSM1-25N23", "PAC");
        markerTypes.put("bz1a16.z", "BAC END");
        markerTypes.put("pac150e16", "PAC END");
        markerTypes.put("1ad1330", "RAPD");
        markerTypes.put("gof13", "SSLP");
        markerTypes.put("18af.1190", "STS");
//        markerTypes.put("Tg(kdr:GFP)", "Transgenic Construct");
    }


    @Before
    public void setUp() {
        super.setUp();
        testUrl.append("http://").append(domain);
    }

//    @Test
    public void markerViewPages() throws Exception {
        testUrl.append("/");
        testUrl.append(mutant);
        testUrl.append("/action/marker/view/");
        MarkerRepository mr = RepositoryFactory.getMarkerRepository();

        List<String> successPages = new ArrayList<String>();
        for (String markerName : markerTypes.keySet()) {
            Marker marker = mr.getMarkerByAbbreviation(markerName);
            if (marker == null) {
                LOG.error("No Marker found with symbol: " + markerName);
                break;
            }
            String contents = loadURL(testUrl.toString() + marker.getZdbID());
            if (contents != null) {
                String type = markerTypes.get(markerName);
                String title = "ZFIN: " + type + ": " + markerName;
                if (contents.indexOf(title) == -1)
                    LOG.error("Could not find page with title " + title);
                else
                    successPages.add(markerName);
            }
        }
        LOG.info(successPages.size() + " Pages successfully retrieved");
        assertEquals("Number of web pages", markerTypes.size(), successPages.size());
        for (String page : successPages) {
            LOG.info(page);
        }

    }

    private String loadURL(String url) throws IOException {
        URL u = new URL(url);
        HttpURLConnection uc;
        StringBuffer body = new StringBuffer();
        try {
            uc = (HttpURLConnection) u.openConnection();
            uc.setAllowUserInteraction(false);
            if (uc.getResponseCode() == 200) {
                String ct = uc.getContentType();

                if (mimeTypes.get(ct) != null) {

                    BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), "ISO-8859-1"));

                    String line;
                    body = new StringBuffer(2048);
                    while ((line = in.readLine()) != null) {
                        body.append(line);
                        body.append("\n");
                    }
                    in.close();
                } else {
                    LOG.error("Unsupported MIME type (" + ct + ") type so ignoring: " + url);
                }
            } else {
                LOG.error("Unexpected response code: " + uc.getResponseCode() + " for URL: " + url);
            }
        }
        catch (FileNotFoundException e) {
            LOG.error("No content found for URL: " + url);
        }
        return body.toString();
    }

}
