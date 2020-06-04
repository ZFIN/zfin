package org.zfin.datatransfer.webservice;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.zfin.datatransfer.ServiceConnectionException;
import org.zfin.datatransfer.microarray.GeoMicorarrayEntriesBean;
import org.zfin.sequence.EFetchDefline;
import org.zfin.sequence.Sequence;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
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

    public enum Type {
        POLYPEPTIDE("protein"), NUCLEOTIDE("nucleotide");

        private String val;

        Type(String val) {
            this.val = val;
        }

        String getVal() {
            return val;
        }
    }
}