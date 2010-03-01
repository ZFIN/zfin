package org.zfin.datatransfer.webservice;

import org.apache.axis.AxisFault;
import org.apache.log4j.Logger;
import org.zfin.gwt.root.dto.InferenceCategory;
import uk.ac.ebi.webservices.WSDbfetchClient;


/**
 * Fetches EBI via services.
 * http://www.ebi.ac.uk/Tools/webservices/clients/dbfetch
 */
public class EBIFetch {

    private final static Logger logger = Logger.getLogger(EBIFetch.class);


    public static Boolean validateAccession(String accession) {
        WSDbfetchClient client = new WSDbfetchClient();
        try {
            String data = client.fetchData(InferenceCategory.UNIPROTKB.name()+ ":" +accession,null,null);
            logger.debug("data returned for sequence:\n"+data); 
            return data.contains("AC   "+accession) ;
//            BufferedReader reader = new BufferedReader(new StringReader(data));
//            RichSequenceIterator iterator = RichSequence.IOTools.readUniProt(reader ,new SimpleNamespace(""));
//            if(iterator.hasNext()){
//                return iterator.nextRichSequence().getAccession().equals(accession) ;
//            }
//            else{
//                return false ;
//            }
        }
        catch (Exception e) {
            if(e instanceof AxisFault && e.getMessage().contains("DbfNoEntryFoundException")){
                logger.info("Entry not found: "+accession);
            }
            else{
                logger.error("Failed to retrieve accession",e);
            }
            return false ;
        }
    }


}