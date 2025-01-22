package org.zfin.infrastructure.seo;

import lombok.extern.log4j.Log4j2;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.util.FileUtil;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Log4j2
public class ReadAndQuerySitemapTask extends AbstractScriptWrapper {

    public static void main(String[] args) throws IOException {
        ReadAndQuerySitemapTask task = new ReadAndQuerySitemapTask();
        task.runTask();
    }

    private void runTask() {
        initAll();
        String sitemapIndex = System.getenv("SITEMAP_INDEX");
        if (sitemapIndex == null) {
            throw new RuntimeException("SITEMAP_INDEX environment variable is not set");
        }

        String delayMs = System.getenv("DELAY_MS");
        if (delayMs == null) {
            throw new RuntimeException("DELAY_MS environment variable is not set");
        }
        Long delay = Long.parseLong(delayMs);

        List<String> urls = new ArrayList<>();

        XmlSitemapIndexSet xmlSitemapIndexSet = readSitemapIndex(sitemapIndex);
        for (XmlSitemapIndexEntry xmlSitemapIndexEntry : xmlSitemapIndexSet.getSitemaps()) {
            System.out.println("Reading from " + xmlSitemapIndexEntry.getLoc());
            XmlUrlSet xmlUrlSet = readSitemap(xmlSitemapIndexEntry.getLoc());
            for (XmlUrl xmlUrl : xmlUrlSet.getXmlUrls()) {
                urls.add(xmlUrl.getLoc());
            }
        }

        if (System.getenv("RANDOMIZE") != null) {
            Collections.shuffle(urls);
        }

        for (String url : urls) {
            checkUrl(url, delay);
            pause(delay);
        }
    }

    private void pause(Long delay) {
        if (delay == 0) {
            return;
        }
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void checkUrl(String url, Long delay) {
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.connect();

            String returnStatus = connection.getHeaderField(null);
            System.out.println("OK: \"" + returnStatus + "\" " + url);
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + url);
        } catch (IOException e) {
            System.out.println("Error: " + url);
        }
    }

    private XmlUrlSet readSitemap(String loc) {
        XmlUrlSet xmlUrlSet = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(XmlUrlSet.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            File tempFile = downloadFileToTemporaryFile(loc);
            if (loc.endsWith(".gz")) {
                try (GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(tempFile))) {
                    xmlUrlSet = (XmlUrlSet) unmarshaller.unmarshal(gzipInputStream);
                }
            } else {
                xmlUrlSet = (XmlUrlSet) unmarshaller.unmarshal(tempFile);
            }
        } catch (JAXBException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return xmlUrlSet;
    }

    private XmlSitemapIndexSet readSitemapIndex(String sitemapIndexUrl) {
        XmlSitemapIndexSet xmlSitemapIndexSet = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(XmlSitemapIndexSet.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            xmlSitemapIndexSet = (XmlSitemapIndexSet) unmarshaller.unmarshal(downloadFileToTemporaryFile(sitemapIndexUrl));
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return xmlSitemapIndexSet;
    }

    private File downloadFileToTemporaryFile(String url) {
        try {
            File tempFile = File.createTempFile("sitemap", ".xml");
            downloadFile(url, tempFile.getAbsolutePath());
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void downloadFile(String url, String filename) {
        try {
            FileUtil.copyURLtoFileIgnoringSSLErrors(url, new File(filename));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
