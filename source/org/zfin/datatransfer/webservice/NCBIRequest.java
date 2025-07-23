package org.zfin.datatransfer.webservice;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.zfin.datatransfer.ServiceConnectionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
public class NCBIRequest {

    private final static String BASE_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
    private final static int MAX_ATTEMPTS = 3;
    private final static String UNAVAILABLE = "Resource temporarily unavailable";

    private Eutil eutil;
    private Map<String, String> params = new HashMap<>();
    private int attempts = 0;

    public NCBIRequest(Eutil eutil) {
        this.eutil = eutil;
        params.put("retmode", "xml");
    }

    public NCBIRequest with(String name, String value) {
        params.put(name, value);
        return this;
    }

    public NCBIRequest with(String name, int value) {
        return with(name, Integer.toString(value));
    }

    private NCBIRequest withFasta() {
        return with("retmode", "text")
                .with("rettype", "fasta");
    }
    
    public String getFasta() throws IOException {
        this.withFasta();
        InputStream content = getHttpResponseContent();

        return new BufferedReader(new InputStreamReader(content))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    public Document go() throws ServiceConnectionException {
        attempts += 1;
        if (attempts > MAX_ATTEMPTS) {
            throw new ServiceConnectionException("Could not get successful response from NCBI after " + MAX_ATTEMPTS + " attempts");
        }
        try {
            InputStream response = getHttpResponseContent();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document result = docBuilder.parse(response);
            boolean unavailable = result.getDocumentElement().getTextContent().contains(UNAVAILABLE);
            if (unavailable) {
                // make another attempt
                return go();
            } else {
                return result;
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.error("Unable to perform EUtils request", e);

            //convert stack trace to string
            String stackTrace = ExceptionUtils.getStackTrace(e);
            log.error("Stack trace:\n" + stackTrace);

            if (e.getCause() != null) {
                String causeTrace = ExceptionUtils.getStackTrace(e.getCause());
                log.error("Cause trace:\n" + causeTrace);
            }

            throw new ServiceConnectionException("Unable to perform EUtils request", e);
        }
    }

    public String fetchRawText() throws ServiceConnectionException {
        try {
            InputStream response = getHttpResponseContent();
            String content = IOUtils.toString(response, "UTF-8");
            return content;
        } catch (IOException e) {
            throw new ServiceConnectionException("Unable to perform EUtils request", e);
        }
    }

    private InputStream getHttpResponseContent() throws IOException {
        CloseableHttpClient client = HttpClientBuilder.create().build();

        HttpPost post = new HttpPost(BASE_URL + eutil.getPath());
        post.setConfig(RequestConfig
                .custom()
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build());

        List<NameValuePair> nvps = new ArrayList<>();
        for (Map.Entry<String, String> param : params.entrySet()) {
            nvps.add(new BasicNameValuePair(param.getKey(), param.getValue()));
        }
        UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nvps, "UTF-8");
        String formContentsString = IOUtils.toString(urlEncodedFormEntity.getContent(), "UTF-8");
        post.setEntity(urlEncodedFormEntity);
        log.debug("Posting to URI: " + post.getURI());

        HttpResponse response = client.execute(post);
        InputStream responseContent = response.getEntity().getContent();
        if (response.getStatusLine().getStatusCode() != 200) {
            String errorMessage = "Error response from NCBI: " + response.getStatusLine().getStatusCode() +
                                  " - " + response.getStatusLine().getReasonPhrase();
            log.error(errorMessage);
            throw new IOException(errorMessage);
        }

        //responseContent is a lazy decompressing stream, so we need to eager load the contents before closing connection
        InputStream eagerLoadedInputStream = new ByteArrayInputStream(responseContent.readAllBytes());

        client.close();
        return eagerLoadedInputStream;
    }

    public enum Eutil {
        FETCH("efetch.fcgi"),
        SEARCH("esearch.fcgi"),
        SUMMARY("esummary.fcgi");

        private String path;

        Eutil(String path) {
            this.path = path;
        }

        String getPath() {
            return path;
        }
    }
}
