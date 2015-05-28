package org.zfin.datatransfer.webservice;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.zfin.datatransfer.ServiceConnectionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NCBIRequest {

    private final static String BASE_URL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/";

    private Eutil eutil;
    private Map<String, String> params = new HashMap<>();

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

    public Document go() throws ServiceConnectionException {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(BASE_URL + eutil.getPath());
            List<NameValuePair> nvps = new ArrayList<>();
            for (Map.Entry<String, String> param : params.entrySet()) {
                nvps.add(new BasicNameValuePair(param.getKey(), param.getValue()));
            }
            post.setEntity(new UrlEncodedFormEntity(nvps));
            HttpResponse response = client.execute(post);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(response.getEntity().getContent());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ServiceConnectionException("Unable to perform EUtils request", e);
        }
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
