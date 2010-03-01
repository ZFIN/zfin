package org.zfin.datatransfer.webservice;

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchSequenceServiceStub;
import org.apache.log4j.Logger;
import org.zfin.sequence.EFetchDefline;
import org.zfin.sequence.Sequence;

import java.util.ArrayList;
import java.util.List;


/**
 * Fetches NCBI via services.
 * http://www.ncbi.nlm.nih.gov/corehtml/query/static/efetch_help.html
 */
public class NCBIEfetch {

    private final static Logger logger = Logger.getLogger(NCBIEfetch.class);

    //    private final static String NUCLEOTIDE_DB = "nuccore" ;
    private final static String NUCLEOTIDE_DB = "nucleotide";
    private final static String POLYPEPTIDE_DB = "protein";


    /**
     * @param accession    Accession to get sequences for.
     * @param isNucleotide Whether or not is nucleotide or protein.
     * @return A list of sequence.
     */
    public static List<Sequence> getSequenceForAccession(String accession, boolean isNucleotide) {

        List<Sequence> fastaStrings = new ArrayList<Sequence>();
        // fetch a record from Taxonomy database
        EFetchSequenceServiceStub.EFetchResult result = getFetchResults(accession,isNucleotide) ;
        if(result==null) return fastaStrings;
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

    public static Boolean validateAccession(String accession,boolean isNucleotide) {
        EFetchSequenceServiceStub.EFetchResult result = getFetchResults(accession,isNucleotide);
        if(result==null) return false ;
        if (result.getGBSet().getGBSetSequence().length > 1) {
            logger.info(result.getGBSet().getGBSetSequence().length + " sequences returned via EFetch for accession: " + accession);
        }
        return result.getGBSet().getGBSetSequence().length > 0;
    }


    private static EFetchSequenceServiceStub.EFetchResult getFetchResults(String accession, boolean isNucleotide){

        try{
            EFetchSequenceServiceStub service = new EFetchSequenceServiceStub();
            // call NCBI EFetch utility
            EFetchSequenceServiceStub.EFetchRequest req = new EFetchSequenceServiceStub.EFetchRequest();
            req.setDb((isNucleotide ? NUCLEOTIDE_DB : POLYPEPTIDE_DB));
            req.setId(accession.toUpperCase());
            req.setRetmax("1");
            return service.run_eFetch(req);
        }
        catch (Exception e) {
            if (e.getMessage().contains("Unexpected subelement Error")) {
                logger.info("Sequence not found at NCBI[" + accession.toUpperCase() + "]");
            } else {
                logger.error("Error trying to find sequence at NCBI[" + accession.toUpperCase() + "]", e);
            }
            return null ;
        }
    }
}