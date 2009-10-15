package org.zfin.datatransfer;

import org.zfin.sequence.Accession;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
* To parse the GEO 1319 file.
 */
public class GPLSoftParser1319 extends GPLProcessor {


    private final Logger logger = Logger.getLogger( GPLSoftParser1319.class ) ;

    protected String getFileName() {
        return "GPL1319" ;
    }


    public Set<String> parseUniqueNumbers() {

        Set<String> accessionNumbers = new HashSet<String>() ;

        // copy file to temp
        File file = null ;
        BufferedReader reader = null ;
        try{
            file = downloadFile();
            reader = new BufferedReader(new FileReader(file)) ;
            String buffer ;

            // read from input file to output file
            boolean startReading = false ;
            int count = 0 ;
            while( (buffer = reader.readLine())!= null ){
                if(buffer.startsWith("!platform_table_end")){
                    logger.info("number of geo accession from GPL1319: "+count);
                    return accessionNumbers;
                }
                else
                if(startReading==true){
                    StringTokenizer tokenizer = new StringTokenizer(buffer,"\t");
                    String id = tokenizer.nextToken(); // ID
                    String accessionNumber = tokenizer.nextToken() ;
                    if(id.startsWith("Dr.")){
                        if(accessionNumber.length()>4){
                            accessionNumbers.add(fixAccession(accessionNumber)) ;
                            ++count ;
                        }
                        else{
                            logger.error("bad parse info GPL1319:\n"+buffer);
                        }
                    }
                }
                else
                if(buffer.startsWith("!platform_table_begin")){
                    startReading = true ;
                }
            }
        }
        catch(Exception e){
            logger.error("failed to parse: " + e);
        }
        finally{
            if(file!=null){
                try{
                    reader.close() ;
                }
                catch(Exception e){
                    logger.error("fail to close: " + e);
                }
            }
        }
        return accessionNumbers ;


    }

//    public Set<Accession> parse(ReferenceDatabase... referenceDatabases) {
    public Set<Accession> parse() {


        Set<Accession> accessions = new HashSet<Accession>() ;
        Set<String> accessionNumbers = parseUniqueNumbers() ; 


        for(String accessionNumber : accessionNumbers){
//            for(ReferenceDatabase referenceDatabase: referenceDatabases){
                Accession accession = new Accession() ; 
                accession.setNumber(accessionNumber) ; 
//                accession.setReferenceDatabase(referenceDatabase) ; 
                accessions.add(accession) ; 
//            }
        }

        return accessions ; 


    }
    // open and read

}
