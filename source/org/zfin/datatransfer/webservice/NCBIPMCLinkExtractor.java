package org.zfin.datatransfer.webservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class NCBIPMCLinkExtractor {
    private final HttpClient httpClient;

    public NCBIPMCLinkExtractor() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    // Data class to hold link information
    public static class PMCLink {
        private final String format;
        private final String updated;
        private final String href;

        public PMCLink(String format, String updated, String href) {
            this.format = format;
            this.updated = updated;
            this.href = href;
        }

        public String getFormat() { return format; }
        public String getUpdated() { return updated; }
        public String getHref() { return href; }

        @Override
        public String toString() {
            return String.format("PMCLink{format='%s', updated='%s', href='%s'}",
                    format, updated, href);
        }

        public String getHttpsHref() {
            if (href.startsWith("ftp://")) {
                return "https://" + href.substring(6);
            }
            return href;
        }
    }

    // Data class to hold record information
    public static class PMCRecord {
        private final String id;
        private final String citation;
        private final String license;
        private final String retracted;
        private final List<PMCLink> links;

        public PMCRecord(String id, String citation, String license,
                         String retracted, List<PMCLink> links) {
            this.id = id;
            this.citation = citation;
            this.license = license;
            this.retracted = retracted;
            this.links = links;
        }

        public String getId() { return id; }
        public String getCitation() { return citation; }
        public String getLicense() { return license; }
        public String getRetracted() { return retracted; }
        public List<PMCLink> getLinks() { return links; }

        @Override
        public String toString() {
            return String.format("PMCRecord{id='%s', citation='%s', license='%s', retracted='%s', links=%s}",
                    id, citation, license, retracted, links);
        }
    }

    // Main method to extract links from PMC ID
    public List<PMCRecord> extractLinksFromPMC(String pmcId) {
        String url = "https://www.ncbi.nlm.nih.gov/pmc/utils/oa/oa.fcgi?id=" + pmcId;

        try {
            // Create and send HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseXMLResponse(response.body());
            } else {
                System.err.println("HTTP Error: " + response.statusCode());
                return new ArrayList<>();
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error making request: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Parse XML response and extract data
    private List<PMCRecord> parseXMLResponse(String xmlContent) {
        List<PMCRecord> records = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse XML from string
            InputStream inputStream = new java.io.ByteArrayInputStream(
                    xmlContent.getBytes("UTF-8"));
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();

            // Print general information
            Element root = document.getDocumentElement();
            NodeList responseDateNodes = root.getElementsByTagName("responseDate");
            if (responseDateNodes.getLength() > 0) {
                System.out.println("Response Date: " + responseDateNodes.item(0).getTextContent());
            }

            NodeList requestNodes = root.getElementsByTagName("request");
            if (requestNodes.getLength() > 0) {
                Element requestElement = (Element) requestNodes.item(0);
                System.out.println("Request ID: " + requestElement.getAttribute("id"));
            }

            NodeList recordsNodes = root.getElementsByTagName("records");
            if (recordsNodes.getLength() > 0) {
                Element recordsElement = (Element) recordsNodes.item(0);
                System.out.println("Records returned: " + recordsElement.getAttribute("returned-count"));
                System.out.println("Total count: " + recordsElement.getAttribute("total-count"));
            }

            System.out.println("=".repeat(50));

            // Extract record information
            NodeList recordNodes = document.getElementsByTagName("record");

            for (int i = 0; i < recordNodes.getLength(); i++) {
                Node recordNode = recordNodes.item(i);

                if (recordNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element recordElement = (Element) recordNode;

                    String id = recordElement.getAttribute("id");
                    String citation = recordElement.getAttribute("citation");
                    String license = recordElement.getAttribute("license");
                    String retracted = recordElement.getAttribute("retracted");

                    System.out.println("Record ID: " + id);
                    System.out.println("Citation: " + citation);
                    System.out.println("License: " + license);
                    System.out.println("Retracted: " + retracted);
                    System.out.println("Links:");

                    // Extract links
                    List<PMCLink> links = new ArrayList<>();
                    NodeList linkNodes = recordElement.getElementsByTagName("link");

                    for (int j = 0; j < linkNodes.getLength(); j++) {
                        Node linkNode = linkNodes.item(j);

                        if (linkNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element linkElement = (Element) linkNode;

                            String format = linkElement.getAttribute("format");
                            String updated = linkElement.getAttribute("updated");
                            String href = linkElement.getAttribute("href");

                            PMCLink link = new PMCLink(format, updated, href);
                            links.add(link);

                            System.out.println("  Format: " + format);
                            System.out.println("  Updated: " + updated);
                            System.out.println("  URL: " + href);
                            System.out.println("  ---");
                        }
                    }

                    PMCRecord record = new PMCRecord(id, citation, license, retracted, links);
                    records.add(record);

                    System.out.println("=".repeat(50));
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.err.println("Error parsing XML: " + e.getMessage());
        }

        return records;
    }

    // Get links of specific format only
    public List<String> getLinksOfFormat(String pmcId, String targetFormat) {
        List<PMCRecord> records = extractLinksFromPMC(pmcId);
        List<String> filteredLinks = new ArrayList<>();

        for (PMCRecord record : records) {
            for (PMCLink link : record.getLinks()) {
                if (targetFormat.equalsIgnoreCase(link.getFormat())) {
                    filteredLinks.add(link.getHref());
                }
            }
        }

        return filteredLinks;
    }

    // Get all links as simple strings
    public List<String> getAllLinks(String pmcId) {
        List<PMCRecord> records = extractLinksFromPMC(pmcId);
        List<String> allLinks = new ArrayList<>();

        for (PMCRecord record : records) {
            for (PMCLink link : record.getLinks()) {
                allLinks.add(link.getHref());
            }
        }

        return allLinks;
    }

    public static void main(String[] args) {
        NCBIPMCLinkExtractor extractor = new NCBIPMCLinkExtractor();

        String pmcId = args[0];

        System.out.println("Extracting links for: " + pmcId);
        System.out.println("=".repeat(60));

        // Extract all data
        List<PMCRecord> records = extractor.extractLinksFromPMC(pmcId);

        if (!records.isEmpty()) {
            System.out.println("\nExtracted data structure:");
            for (PMCRecord record : records) {
                System.out.println("Record: " + record.getId());
                for (PMCLink link : record.getLinks()) {
                    System.out.println("  " + link.getFormat() + ": " + link.getHref() + " or " + link.getHttpsHref());
                }
            }

            System.out.println("\n" + "=".repeat(60));
            System.out.println("PDF links only:");
            List<String> pdfLinks = extractor.getLinksOfFormat(pmcId, "pdf");
            for (String link : pdfLinks) {
                System.out.println(link);
            }

            System.out.println("\n" + "=".repeat(60));
            System.out.println("All links:");
            List<String> allLinks = extractor.getAllLinks(pmcId);
            for (String link : allLinks) {
                System.out.println(link);
            }
        } else {
            System.out.println("No records found or error occurred.");
        }
    }
}