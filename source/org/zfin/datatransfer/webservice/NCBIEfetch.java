package org.zfin.datatransfer.webservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zfin.datatransfer.ServiceConnectionException;
import org.zfin.datatransfer.microarray.GeoMicorarrayEntriesBean;
import org.zfin.publication.Publication;
import org.zfin.sequence.EFetchDefline;
import org.zfin.sequence.Sequence;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.URL;
import java.util.*;


/**
 * Fetches NCBI via services.
 * http://www.ncbi.nlm.nih.gov/corehtml/query/static/efetch_help.html
 */
public class NCBIEfetch {

    private final static Logger logger = LogManager.getLogger(NCBIEfetch.class);

    private final static String DOT = ".";
    private final static String UNDERSCORE = "_";
    private final static String GBSEQ = "GBSeq";
    private final static String GBSEQ_SEQUENCE = "GBSeq_sequence";

    /**
     * @param accession Accession to get sequences for.
     * @return A list of sequence.
     */
    public static List<Sequence> getSequenceForAccession(String accession) {
        return getSequenceForAccession(accession, Type.POLYPEPTIDE);
    }

    /**
     * Search NCBI API by accessions (comma separated)
     * @param accession
     * @param type
     * @param retmax
     * @return
     * @throws ServiceConnectionException
     */
    public static String searchSequenceJsonForAccession(String accession, Type type, Integer retmax) throws ServiceConnectionException {
        return new NCBIRequest(NCBIRequest.Eutil.SEARCH)
                .with("db", type.getVal())
                .with("term", accession)
                .with("retmode", "json")
                .with("retmax", retmax)
                .fetchRawText();
    }

    /**
     * Get summary info for Type by list of IDs (comma separated)
     * @param ids
     * @param type
     * @param retmax
     * @return
     * @throws ServiceConnectionException
     */
    public static String getSequenceSummaryJsonByID(String ids, Type type, Integer retmax) throws ServiceConnectionException {
        return new NCBIRequest(NCBIRequest.Eutil.SUMMARY)
                .with("db", type.getVal())
                .with("id", ids)
                .with("retmode", "json")
                .with("retmax", retmax)
                .fetchRawText();
    }

    public static List<Sequence> getSequenceForAccession(String accession, Type type) {

        List<Sequence> sequences = new ArrayList<>();
        Document result;
        try {
            result = getFetchResults(accession, type);
        } catch (ServiceConnectionException e) {
            return sequences;
        }

        NodeList gbseqNodes = result.getElementsByTagName(GBSEQ);
        for (int i = 0; i < gbseqNodes.getLength(); i++) {
            Element el = (Element) gbseqNodes.item(i);
            NodeList sequenceNodes = el.getElementsByTagName(GBSEQ_SEQUENCE);
            for (int j = 0; j < sequenceNodes.getLength(); j++) {
                Sequence sequence = new Sequence();
                sequence.setData(sequenceNodes.item(j).getTextContent());
                sequence.setDefLine(new EFetchDefline(el));
                sequences.add(sequence);
            }
        }
        return sequences;
    }

    public static boolean validateAccession(String accession) {
        try {
            Document result = getFetchResults(accession, Type.POLYPEPTIDE);
            NodeList sequenceNodes = result.getElementsByTagName(GBSEQ_SEQUENCE);
            if (sequenceNodes.getLength() > 1) {
                logger.warn("Expected 1 sequence returned via EFetch for accession " + accession + ", but got " + sequenceNodes.getLength() + ":");
                for (int i = 0; i < sequenceNodes.getLength(); i++) {
                    logger.warn(sequenceNodes.item(i).getTextContent());
                }
            }
            return sequenceNodes.getLength() > 0;
        } catch (ServiceConnectionException e) {
            logger.info("Unable to fetch " + accession + " from NCBI", e);
            return false;
        }
    }

    private static Document getFetchResults(String accession, Type type) throws ServiceConnectionException {
        return new NCBIRequest(NCBIRequest.Eutil.FETCH)
                .with("db", type.getVal())
                .with("id", accession)
                .with("retmax", 1)
                .go();
    }

    public static String createMicroarrayQuery(Collection<String> accessions, String symbol) {
        if (CollectionUtils.isEmpty(accessions) && symbol == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("txid7955[organism] AND (");
        if (symbol != null) {
            sb.append(symbol + "[gene symbol]");
            if (CollectionUtils.isNotEmpty(accessions)) {
                sb.append(" OR ");
            }
        }
        if (CollectionUtils.isNotEmpty(accessions)) {
            sb.append("(");
            boolean isFirst = true;
            for (String accession : accessions) {
                sb.append((!isFirst ? " OR " : "")).append(accession + " OR " + accession + ".*");
                isFirst = false;
            }
            sb.append(")");
        }


        // close OR
        sb.append(")");

        return sb.toString();
    }

    public static boolean hasMicroarrayData(Collection<String> accessions) {
        return hasMicroarrayData(accessions, null);
    }

    /**
     * should  match this:
     * http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=geoprofiles&term=txid7955[organism]%20AND%20rpf1[gene%20symbol]&retmax=0
     */
    public static boolean hasMicroarrayData(Collection<String> accessions, String symbol) {
        try {
            Document result = new NCBIRequest(NCBIRequest.Eutil.SEARCH)
                    .with("db", "geoprofiles")
                    .with("term", createMicroarrayQuery(accessions, symbol))
                    .with("retmax", "0")
                    .go();

            String count = XPathFactory.newInstance().newXPath().evaluate("/eSearchResult/Count", result);
            return Integer.parseInt(count) > 0;
        } catch (ServiceConnectionException e) {
            logger.error(e);
            return false;
        } catch (XPathExpressionException | NumberFormatException e) {
            return false;
        }
    }

    public static String getMicroarrayLink(Collection<String> accessions) {
        return getMicroarrayLink(accessions, null);
    }

    /**
     * should  match this:
     * http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=geoprofiles&term=txid7955[organism]%20AND%20rpf1[gene%20symbol]&retmax=0
     */
    public static String getMicroarrayLink(Collection<String> accessions, String abbreviation) {

        if (CollectionUtils.isEmpty(accessions) && abbreviation == null) {
            return null;
        }
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<a class=\"external\" href=\"http://www.ncbi.nlm.nih.gov/geoprofiles?term=");
            sb.append(createMicroarrayQuery(accessions, abbreviation));
            sb.append("\">GEO</a> ");
            sb.append("(<a href=\"/ZDB-PUB-071218-1\">1</a>)")
            ;
            return sb.toString();
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    /**
     * Fetches all gene IDs that are not in the current annotation release set.
     * Uses the endpoint:  'https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gene&term=%22Danio%20rerio%22%5bOrganism%5d%20NOT%20%22annotated%20gene%22%5bProperties%5d%20AND%20alive%5bprop%5d&retmode=json&retmax=10'
     *
     * Results look like:
     * {
     *   "header": {
     *     "type": "esearch",
     *     "version": "0.3"
     *   },
     *   "esearchresult": {
     *     "count": "5301",
     *     "retmax": "10",
     *     "retstart": "0",
     *     "idlist": [
     *       "559475",
     *       "378762",
     *       "30762",
     *       "446139",
     *       "100534776",
     *       "562883",
     *       "64886",
     *       "619391",
     *       "327274",
     *       "566708"
     *     ],
     *     "translationset": [],
     *     "translationstack": [
     *       {
     *         "term": "\"Danio rerio\"[Organism]",
     *         "field": "Organism",
     *         "count": "121374",
     *         "explode": "Y"
     *       },
     *       {
     *         "term": "\"annotated gene\"[Properties]",
     *         "field": "Properties",
     *         "count": "87034043",
     *         "explode": "N"
     *       },
     *       "NOT",
     *       {
     *         "term": "alive[prop]",
     *         "field": "prop",
     *         "count": "61682922",
     *         "explode": "N"
     *       },
     *       "AND"
     *     ],...
     * @return List of gene IDs that are not in the current annotation release set, empty list if error occurs
     */
    public static List<String> fetchGeneIDsNotInCurrentAnnotationReleaseSet() {
        System.out.println("Fetching gene IDs not in current annotation release set...");
        return fetchGeneIDsNotInCurrentAnnotationReleaseSet(10000);
    }

    /**
     * Fetches gene IDs that are not in the current annotation release set with specified maximum results.
     * 
     * @param retmax Maximum number of IDs to return (default: 10000)
     * @return List of gene IDs that are not in the current annotation release set, empty list if error occurs
     */
    public static List<String> fetchGeneIDsNotInCurrentAnnotationReleaseSet(int retmax) {
        List<String> geneIds = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        
        try {
            String jsonResponse = new NCBIRequest(NCBIRequest.Eutil.SEARCH)
                    .with("db", "gene")
                    .with("term", "\"Danio rerio\"[Organism] NOT \"annotated gene\"[Properties] AND alive[prop]")
                    .with("retmode", "json")
                    .with("retmax", retmax)
                    .fetchRawText();
            
            // Parse JSON response using Jackson
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode esearchResult = rootNode.get("esearchresult");
            
            if (esearchResult == null) {
                logger.warn("Could not find esearchresult in NCBI gene search response");
                return Collections.emptyList();
            }
            
            JsonNode idList = esearchResult.get("idlist");
            if (idList == null || !idList.isArray()) {
                logger.warn("Could not find idlist array in NCBI gene search response");
                return Collections.emptyList();
            }
            
            // Extract gene IDs from the JSON array
            for (JsonNode idNode : idList) {
                String geneId = idNode.asText();
                if (!geneId.isEmpty()) {
                    geneIds.add(geneId);
                }
            }
            
            logger.info("Fetched " + geneIds.size() + " gene IDs not in current annotation release set");
            
        } catch (ServiceConnectionException e) {
            logger.error("Failed to fetch gene IDs not in current annotation release set", e);
        } catch (Exception e) {
            logger.error("Error parsing NCBI gene search response", e);
        }
        
        return geneIds;
    }

    /**
     * Fetches gene IDs that are not "alive" with specified maximum results.
     *
     * @param retmax Maximum number of IDs to return
     * @return List of gene IDs that are not "alive", empty list if error occurs
     */
    public static List<String> fetchGeneIDsNotAlive(int retmax) {
        List<String> geneIds = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String jsonResponse = new NCBIRequest(NCBIRequest.Eutil.SEARCH)
                    .with("db", "gene")
                    .with("term", "\"Danio rerio\"[Organism] NOT alive[prop]")
                    .with("retmode", "json")
                    .with("retmax", retmax)
                    .fetchRawText();

            // Parse JSON response using Jackson
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode esearchResult = rootNode.get("esearchresult");

            if (esearchResult == null) {
                logger.warn("Could not find esearchresult in NCBI gene search response");
                return Collections.emptyList();
            }

            JsonNode idList = esearchResult.get("idlist");
            if (idList == null || !idList.isArray()) {
                logger.warn("Could not find idlist array in NCBI gene search response");
                return Collections.emptyList();
            }

            // Extract gene IDs from the JSON array
            for (JsonNode idNode : idList) {
                String geneId = idNode.asText();
                if (!geneId.isEmpty()) {
                    geneIds.add(geneId);
                }
            }

            logger.info("Fetched " + geneIds.size() + " gene IDs not in current annotation release set");

        } catch (ServiceConnectionException e) {
            logger.error("Failed to fetch gene IDs not in current annotation release set", e);
        } catch (Exception e) {
            logger.error("Error parsing NCBI gene search response", e);
        }

        return geneIds;
    }

    /**
     * Queries NCBI Gene database to find if gene IDs have been replaced with new IDs.
     * Checks if genes have status "secondary" and returns a map of old ID to new ID for any replacements found.
     *
     * @param geneIDs List of gene IDs to check for replacements
     * @return Map of old gene ID to replacement gene ID for any genes that have been replaced
     */
    public static Map<String, String> getReplacedGeneID(List<String> geneIDs) {
        Map<String, String> replacements = new HashMap<>();

        if (geneIDs == null || geneIDs.isEmpty()) {
            return replacements;
        }

        try {
            // Join gene IDs with commas for batch request
            String ids = String.join(",", geneIDs);

            Document result = new NCBIRequest(NCBIRequest.Eutil.FETCH)
                    .with("db", "gene")
                    .with("id", ids)
                    .with("retmode", "xml")
                    .go();

            XPath xPath = XPathFactory.newInstance().newXPath();

            // Process each Entrezgene record in the response
            NodeList entrezgeneList = result.getElementsByTagName("Entrezgene");

            for (int i = 0; i < entrezgeneList.getLength(); i++) {
                Element entrezgene = (Element) entrezgeneList.item(i);

                // Get the original gene ID
                NodeList geneIdNodes = entrezgene.getElementsByTagName("Gene-track_geneid");
                if (geneIdNodes.getLength() == 0) {
                    continue;
                }
                String originalGeneId = geneIdNodes.item(0).getTextContent();

                // Check if the gene has secondary status
                NodeList statusNodes = entrezgene.getElementsByTagName("Gene-track_status");
                if (statusNodes.getLength() == 0) {
                    continue;
                }
                Element statusElement = (Element) statusNodes.item(0);
                String status = statusElement.getAttribute("value");

                if ("secondary".equals(status)) {
                    // Look for the current GeneID in the Gene-track_current-id section
                    NodeList currentIds = entrezgene.getElementsByTagName("Gene-track_current-id");
                    if (currentIds.getLength() > 0) {
                        Element currentIdElement = (Element) currentIds.item(0);
                        NodeList dbtags = currentIdElement.getElementsByTagName("Dbtag");

                        for (int j = 0; j < dbtags.getLength(); j++) {
                            Element dbtag = (Element) dbtags.item(j);
                            NodeList dbNameNodes = dbtag.getElementsByTagName("Dbtag_db");
                            if (dbNameNodes.getLength() == 0) {
                                continue;
                            }
                            String dbName = dbNameNodes.item(0).getTextContent();

                            if ("GeneID".equals(dbName)) {
                                NodeList objectIdNodes = dbtag.getElementsByTagName("Object-id_id");
                                if (objectIdNodes.getLength() > 0) {
                                    String replacementId = objectIdNodes.item(0).getTextContent();
                                    replacements.put(originalGeneId, replacementId);
                                    logger.info("Gene ID " + originalGeneId + " has been replaced with " + replacementId);
                                }
                                break;
                            }
                        }
                    }
                }
            }

        } catch (ServiceConnectionException e) {
            logger.error("Failed to fetch gene replacement info for gene IDs: " + geneIDs, e);
        } catch (Exception e) {
            logger.error("Error parsing XML response for gene IDs: " + geneIDs, e);
        }

        return replacements;
    }

    public static GeoMicorarrayEntriesBean getMicroarraySequences() throws Exception {
        GeoMicorarrayEntriesBean bean = new GeoMicorarrayEntriesBean();
        XPath xPath = XPathFactory.newInstance().newXPath();

        Document result = new NCBIRequest(NCBIRequest.Eutil.SEARCH)
                .with("db", "geoprofiles")
                .with("term", "txid7955[organism]")
                .with("retmax", "0")
                .with("usehistory", "y")
                .go();

        String webEnvKey = xPath.evaluate("/eSearchResult/WebEnv", result);
        String queryKey = xPath.evaluate("/eSearchResult/QueryKey", result);
        int totalGeoAccessions = Integer.parseInt(xPath.evaluate("/eSearchResult/Count", result));

        logger.warn("Total geo accessions: " + totalGeoAccessions);
        int batchSize = 10000;

        long totalTime = 0;

        for (int i = 1; i < totalGeoAccessions; i += batchSize) {
            long startTime = System.currentTimeMillis();

            Document summaryResult;
            try {
                summaryResult = new NCBIRequest(NCBIRequest.Eutil.SUMMARY)
                        .with("WebEnv", webEnvKey)
                        .with("query_key", queryKey)
                        .with("retstart", i)
                        .with("retmax", batchSize)
                        .go();
            } catch (ServiceConnectionException e) {
                logger.error("Failed to retrieve a batch at [" + i + "]");
                throw e;
            }

            NodeList docSummaries = summaryResult.getElementsByTagName("DocumentSummary");
            for (int j = 0; j < docSummaries.getLength(); j++)  {
                Element docSummary = (Element) docSummaries.item(j);

                NodeList geneName = docSummary.getElementsByTagName("geneName");
                for (int k = 0; k < geneName.getLength(); k++) {
                    bean.addGeneSymbol(geneName.item(k).getTextContent());
                }

                NodeList gbacc = docSummary.getElementsByTagName("GBACC");
                for (int k = 0; k < gbacc.getLength(); k++) {
                    bean.addAccession(gbacc.item(k).getTextContent());
                }
            }
            long endTime = System.currentTimeMillis();
            totalTime += (endTime - startTime);

            logMessage(i, batchSize, totalTime, totalGeoAccessions);
        }
        logMessage(totalGeoAccessions, batchSize, totalTime, totalGeoAccessions);

        return bean;
    }

    private static void logMessage(int i, int batchSize, long totalTime, int totalGeoAccessions) {
        int processed = i + batchSize;
        float avgTime = (float) (totalTime / 1000) / (float) processed;
        float estTotal = totalGeoAccessions * avgTime;
        float estRemaining = estTotal - (totalTime / 1000);
        // processed N / M (X s) est Remaining (M s) estTotalTime (Q s) totalTimeSpent (P s) avg Time (R s)
        String message = "GEO retrieved\n ";
        message += " Processed : " + (processed) + "/" + (totalGeoAccessions);
        message += " est Remaining (" + estRemaining + " s)";
        message += " est Total time (" + estTotal + " s)";
        message += " time spent (" + (totalTime / 1000) + " s)";
        message += " avgTime per entry (" + avgTime + " s)";
        logger.info(message);
    }

    /**
     * Destructive method.
     *
     * @param accession Accession to fix.
     * @return Accession without a . or _ in the wrong spot.
     */
    public static String fixAccession(String accession) {
        accession = cleanDot(accession);
        accession = cleanUnderscore(accession);
        return accession;
    }

    private static String cleanUnderscore(String accession) {
        int dotIndex = accession.lastIndexOf(UNDERSCORE);
        if (dotIndex > 2) {
            return accession.substring(0, dotIndex);
        } else {
            return accession;
        }
    }

    private static String cleanDot(String accession) {
        int dotIndex = accession.lastIndexOf(DOT);
        if (dotIndex > 3) {
            return accession.substring(0, dotIndex);
        } else {
            return accession;
        }
    }

    public static List<NameRecord> retrieveAuthorInfoForSinglePublication(Publication publication) {
        String accession = publication.getAccessionNumber().toString();
        String pubId = publication.getZdbID();
        return retrieveAuthorInfo(List.of(accession), Map.of(accession, pubId));
    }

    public static List<NameRecord> retrieveAuthorInfo(List<String> accessionBatch, Map<String, String> accessionMap) {
        String ids = String.join(",", accessionBatch);
        String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&api_key=47c9eadd39b0bcbfac58e3e911930d143109&retmode=xml&id=" + ids;
        Set<NameRecord> nameList = new HashSet<>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(new URL(url).openStream());
            NodeList articleList = doc.getElementsByTagName("PubmedArticle");
            for (int articleIndex = 0; articleIndex < articleList.getLength(); articleIndex++) {
                Node node = articleList.item(articleIndex);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String accession = element.getElementsByTagName("PMID").item(0).getTextContent();
                    NodeList authors = element.getElementsByTagName("Author");
                    for (int authorIndex = 0; authorIndex < authors.getLength(); authorIndex++) {
                        Node authorNode = authors.item(authorIndex);
                        if (authorNode.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        Element authorElement = (Element) authorNode;
                        String lastname = getNullSafeElement(authorElement.getElementsByTagName("LastName").item(0));
                        String firstname = getNullSafeElement(authorElement.getElementsByTagName("ForeName").item(0));
                        String middleName = getNullSafeElement(authorElement.getElementsByTagName("Initials").item(0));
                        NameRecord record = new NameRecord(firstname, middleName, lastname, accessionMap.get(accession), accession);
                        if (lastname != null) {
                            nameList.add(record);
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            return new ArrayList<>();
        }
        return new ArrayList<>(nameList);
    }

    public record NameRecord(
        String firstName,
        String middleName,
        String lastName,
        String pubId,
        String accession
    ) {
    }


    private static String getNullSafeElement(Node node) {
        String name = null;
        if (node != null)
            name = node.getTextContent();
        return name;
    }

    public enum Type {
        POLYPEPTIDE("protein"), NUCLEOTIDE("nucleotide");

        private String val;

        Type(String val) {
            this.val = val;
        }

        public String getVal() {
            return val;
        }
    }
}