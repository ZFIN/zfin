package org.zfin.datatransfer.webservice;

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchSequenceServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zfin.datatransfer.microarray.GeoMicorarrayEntriesBean;
import org.zfin.properties.ZfinProperties;
import org.zfin.sequence.EFetchDefline;
import org.zfin.sequence.Sequence;

import java.util.*;


/**
 * Fetches NCBI via services.
 * http://www.ncbi.nlm.nih.gov/corehtml/query/static/efetch_help.html
 */
public class NCBIEfetch {

    private final static Logger logger = Logger.getLogger(NCBIEfetch.class);

    private final static String POLYPEPTIDE_DB = "protein";
    private final static String DOT = ".";
    private final static String UNDERSCORE = "_";
//    private final static String SEQUENCE_DB = "sequence"; // should end up using this one


    /**
     * @param accession Accession to get sequences for.
     * @return A list of sequence.
     */
    public static List<Sequence> getSequenceForAccession(String accession) {

        List<Sequence> fastaStrings = new ArrayList<Sequence>();
        // fetch a record from Taxonomy database
        EFetchSequenceServiceStub.EFetchResult result = getFetchResults(accession);
        if (result == null) return fastaStrings;
        // results output
        for (int i = 0; i < result.getGBSet().getGBSetSequence().length; i++) {
            EFetchSequenceServiceStub.GBSeq_type0 gbSeq = result.getGBSet().getGBSetSequence()[i].getGBSeq();
            Sequence sequence = new Sequence();
            sequence.setData(gbSeq.getGBSeq_sequence());
            sequence.setDefLine(new EFetchDefline(gbSeq));
            logger.debug(sequence.getDefLine().toString());
            fastaStrings.add(sequence);
        }

        return fastaStrings;
    }

    public static Boolean validateAccession(String accession) {
        EFetchSequenceServiceStub.EFetchResult result = getFetchResults(accession);
        if (result == null) return false;
        if (result.getGBSet().getGBSetSequence().length > 1) {
            logger.info(result.getGBSet().getGBSetSequence().length + " sequences returned via EFetch for accession: " + accession);
        }
        return result.getGBSet().getGBSetSequence().length > 0;
    }


    private static EFetchSequenceServiceStub.EFetchResult getFetchResults(String accession) {

        try {
            EFetchSequenceServiceStub service = new EFetchSequenceServiceStub();
            // call NCBI EFetch utility
            EFetchSequenceServiceStub.EFetchRequest req = new EFetchSequenceServiceStub.EFetchRequest();
//            req.setDb((isNucleotide ? NUCLEOTIDE_DB : POLYPEPTIDE_DB));
            req.setDb(POLYPEPTIDE_DB);
            req.setId(accession.toUpperCase());
            req.setRetmax("1");
            return service.run_eFetch(req);
        } catch (Exception e) {
            if (e.getMessage().contains("Unexpected subelement Error")
                    ||
                    e.getMessage().contains("Unexpected subelement Bioseq-set")
                    ) {
                logger.error("Sequence not found at NCBI[" + accession.toUpperCase() + "]");
                logger.info(e);
            } else {
                logger.error("Error trying to find sequence at NCBI[" + accession.toUpperCase() + "]", e);
            }
            return null;
        }
    }

    public static String createMicroarrayQuery(Collection<String> accessions, String symbol) {
        if (CollectionUtils.isEmpty(accessions) && symbol == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("txid7955[organism] AND (");
        if (symbol != null) {
            sb.append(symbol + "[gene symbol]");
            if(CollectionUtils.isNotEmpty(accessions)){
                sb.append(" OR ");
            }
        }
        if(CollectionUtils.isNotEmpty(accessions)){
           sb.append("(");
           boolean isFirst = true ;
           for(String accession : accessions) {
               sb.append( (!isFirst ? " OR " : "")).append( accession+" OR "+accession+".*" );
               isFirst = false;
           }
           sb.append(")");
        }


        // close OR
        sb.append(")");

        return sb.toString();
    }

    public static boolean hasMicroarrayData(Collection<String> accessions) {
        return hasMicroarrayData(accessions, null) ;
    }

    /**
     * should  match this:
     * http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=geoprofiles&term=txid7955[organism]%20AND%20rpf1[gene%20symbol]&retmax=0
     */
    public static boolean hasMicroarrayData(Collection<String> accessions, String symbol) {
        try {
            EUtilsServiceStub service = new EUtilsServiceStub();
            EUtilsServiceStub.ESearchRequest request = new EUtilsServiceStub.ESearchRequest();
            request.setDb("geoprofiles");
            request.setTool("geo");
            request.setTerm(createMicroarrayQuery(accessions,symbol));
            request.setRetMax("0");
            EUtilsServiceStub.ESearchResult result = service.run_eSearch(request);

            if (result == null) return false;
            if (Integer.parseInt(result.getCount()) > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error(e);
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
//            if (NCBIEfetch.hasMicroarrayData(accessions, abbreviation)) {
                StringBuilder sb = new StringBuilder();
                sb.append("<a href=\"http://www.ncbi.nlm.nih.gov/geoprofiles?term=");
                sb.append(createMicroarrayQuery(accessions,abbreviation));
                sb.append("\">GEO</a> ");
                sb.append("(<a href=\"/")
                        .append(ZfinProperties.getWebDriver())
                        .append("?MIval=aa-pubview2.apg&OID=ZDB-PUB-071218-1\">1</a>)")
                ;
                return sb.toString();
//            }
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    public static Set<String> getPlatformsForZebrafishMicroarrays() throws Exception{

        EUtilsServiceStub service = new EUtilsServiceStub();
        EUtilsServiceStub.ESearchRequest request = new EUtilsServiceStub.ESearchRequest();
        request.setDb("geo");
        request.setTerm("txid7955[organism]");
        request.setRetStart("0");
        request.setRetMax("0");
        request.setUsehistory("y");
        EUtilsServiceStub.ESearchResult result = service.run_eSearch(request);
        String webEnvKey = result.getWebEnv();

        int totalGeoAccessions = Integer.parseInt(result.getCount());

        logger.info("Total geo accessions: " + totalGeoAccessions);
        int batchSize = 10000 ; // max
        int testMaxToProcess = 1000000 ; // max should be 250K

        Set<String> platforms = new HashSet<String>();
        EUtilsServiceStub.ESummaryRequest summaryRequest = new EUtilsServiceStub.ESummaryRequest();
        request.setRetMax(String.valueOf(batchSize));

        long totalTime = 0 ;

        for(int i = 0 ; i < totalGeoAccessions ; i+=batchSize){
            long startTime = System.currentTimeMillis();
            summaryRequest.setRetstart(String.valueOf(i));
            summaryRequest.setRetmax(String.valueOf(batchSize));
            summaryRequest.setDb("geo");
            summaryRequest.setWebEnv(webEnvKey);
            summaryRequest.setQuery_key("1");
            EUtilsServiceStub.ESummaryResult summaryResult = service.run_eSummary(summaryRequest);
            EUtilsServiceStub.DocSumType[] docTypes = summaryResult.getDocSum();
            for(EUtilsServiceStub.DocSumType docType : docTypes){
                // get outer ioccurenct
                for(EUtilsServiceStub.ItemType itemType   : docType.getItem()){
                    if(itemType.getName().equals("GPL")){
                        platforms.add(itemType.getItemContent());
                    }
                }
            }
            long endTime = System.currentTimeMillis();
            totalTime += (endTime - startTime) ;
            logger.info("time per : "+ ((endTime - startTime) / (1000f*batchSize ) ) + " i["+i+"]");
            if(i >= testMaxToProcess){
                double avgTime = (totalTime) / (1000f*testMaxToProcess) ;
                logger.info("total time avg: "+  avgTime );
                logger.info("est time: "+ avgTime*totalGeoAccessions);
                return platforms;
            }
        }

        return platforms;
    }

    public static GeoMicorarrayEntriesBean getMicroarraySequences() throws Exception{
        GeoMicorarrayEntriesBean bean = new GeoMicorarrayEntriesBean();

        EUtilsServiceStub service = new EUtilsServiceStub();
        EUtilsServiceStub.ESearchRequest request = new EUtilsServiceStub.ESearchRequest();
        request.setDb("geo");
        request.setTerm("txid7955[organism]");
        request.setRetStart("0");
        request.setRetMax("0");
        request.setUsehistory("y");
        EUtilsServiceStub.ESearchResult result = service.run_eSearch(request);
        String webEnvKey = result.getWebEnv();

        int totalGeoAccessions = Integer.parseInt(result.getCount());

        logger.info("Total geo accessions: " + totalGeoAccessions);
        int batchSize = 10000 ; // max
        int testMaxToProcess = 1000000 ; // max should be 250K
//        int testMaxToProcess = 30000 ; // max should be 250K

        EUtilsServiceStub.ESummaryRequest summaryRequest = new EUtilsServiceStub.ESummaryRequest();
        request.setRetMax(String.valueOf(batchSize));

        long totalTime = 0 ;

        for(int i = 1 ; i < totalGeoAccessions ; i+=batchSize){
            long startTime = System.currentTimeMillis();
            summaryRequest.setRetstart(String.valueOf(i));
            summaryRequest.setRetmax(String.valueOf(batchSize));
            summaryRequest.setDb("geo");
            summaryRequest.setWebEnv(webEnvKey);
            summaryRequest.setQuery_key("1");
            EUtilsServiceStub.ESummaryResult summaryResult = service.run_eSummary(summaryRequest);
            EUtilsServiceStub.DocSumType[] docTypes = summaryResult.getDocSum();
            for(EUtilsServiceStub.DocSumType docType : docTypes){
                // get outer ioccurenct
//                EUtilsServiceStub.ItemType localItemType = docType.getItem()[0];
                for(EUtilsServiceStub.ItemType itemType   : docType.getItem()){
                    if(itemType.getName().equals("geneName")){
                        bean.addGeneSymbol(itemType.getItemContent());
                    }
                    if(itemType.getName().equals("GBACC")){
                        if(itemType.getItemContent()!=null){
                            bean.addAccession(cleanDot(itemType.getItemContent()));
                        }
                    }
                }
            }
            long endTime = System.currentTimeMillis();
            totalTime += (endTime - startTime) ;
            logger.info("time per : "+ ((endTime - startTime) / (1000f*batchSize ) ) + " i["+i+"]");
            if(i >= testMaxToProcess){
                double avgTime = (totalTime) / (1000f*testMaxToProcess) ;
                logger.info("total time avg: "+  avgTime );
                logger.info("est time: "+ avgTime*totalGeoAccessions);
                return bean ;
            }
        }
        double avgTime = (totalTime) / (1000f*testMaxToProcess) ;
        logger.info("total time avg: "+  avgTime );
        logger.info("est time: "+ avgTime*totalGeoAccessions);

        return bean ;
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
}