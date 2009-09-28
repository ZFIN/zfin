
package org.zfin.datatransfer.webservice ;

import org.apache.log4j.Logger;
import org.zfin.sequence.Sequence;
import org.zfin.sequence.EFetchDefline;

import gov.nih.nlm.ncbi.www.soap.eutils.* ;

import java.util.List;
import java.util.ArrayList;


/**
 *  Fetches NCBI via services.
 *  http://www.ncbi.nlm.nih.gov/corehtml/query/static/efetch_help.html
 */
public class NCBIEfetch {

    private final static Logger logger = Logger.getLogger(NCBIEfetch.class);

//    private final static String NUCLEOTIDE_DB = "nuccore" ;
    private final static String NUCLEOTIDE_DB = "nucleotide" ;
    private final static String POLYPEPTIDE_DB = "protein" ;


    /**
     * @param accession Accession to get sequences for.
     * @param isNucleotide Whether or not is nucleotide or protein.
     * @return A list of sequence.
     */
    public static List<Sequence> getSequenceForAccession(String accession, boolean isNucleotide){

        List<Sequence> fastaStrings = new ArrayList<Sequence>() ;
        // fetch a record from Taxonomy database
       try
        {
            EFetchSequenceServiceStub service = new EFetchSequenceServiceStub();
            // call NCBI EFetch utility
            EFetchSequenceServiceStub.EFetchRequest req = new EFetchSequenceServiceStub.EFetchRequest();
            req.setDb( (isNucleotide ? NUCLEOTIDE_DB : POLYPEPTIDE_DB) );
            req.setId(accession.toUpperCase());
            req.setRetmax("1");
            EFetchSequenceServiceStub.EFetchResult res = service.run_eFetch(req);
            if(res.getGBSet().getGBSetSequence().length>1){
                logger.warn(res.getGBSet().getGBSetSequence().length + " sequences returned via EFetch for accession: "+ accession) ;
            }
            // results output
            for (int i = 0; i < res.getGBSet().getGBSetSequence().length; i++)
            {
                EFetchSequenceServiceStub.GBSeq_type0 gbSeq = res.getGBSet().getGBSetSequence()[i].getGBSeq();
                Sequence sequence = new Sequence() ;
                sequence.setData(gbSeq.getGBSeq_sequence());
                sequence.setDefLine(new EFetchDefline(gbSeq));
                logger.debug(sequence.getDefLine().toString());
                fastaStrings.add(sequence) ;
            }
        }
        catch (Exception e) {
            logger.error(e);
        }
        return fastaStrings ;
    }

}