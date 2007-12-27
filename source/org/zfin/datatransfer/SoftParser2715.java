package org.zfin.datatransfer;

import org.zfin.sequence.Accession;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.properties.ZfinProperties;
import org.apache.log4j.Logger;

import java.util.*;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * To parse the GEO 2715 file.

 */
public class SoftParser2715 extends SoftParser{

    private final Logger infoLogger = Logger.getLogger( ZfinProperties.MICROARRAY_INFO) ;
    private final Logger errorLogger = Logger.getLogger( ZfinProperties.MICROARRAY_ERROR) ;

    public Set<String> parseUniqueNumbers() {
        Set<String> accessionNumbers = new HashSet<String>() ;

        String gpl2715Directory = System.getProperty("GPL2715",null) ;
        if(gpl2715Directory==null){
            return accessionNumbers ;
        }
                // copy file to temp
        File file = null ;
        BufferedReader reader = null ;
        try{
            file = new File(gpl2715Directory) ;
            reader = new BufferedReader(new FileReader(file)) ;
            String buffer ;

            // read from input file to output file
            boolean startReading = false ;
            int count = 0 ;
            while( (buffer = reader.readLine())!= null ){
                if(buffer.startsWith("!platform_table_end")){
                    infoLogger.info("number of geo accession from GPL2715: "+count);
                    return accessionNumbers ;
                }
                else
                if(startReading==true){
                    StringTokenizer tokenizer = new StringTokenizer(buffer,"\t");
                    String id = tokenizer.nextToken(); // ID
//                    if(id.startsWith("Dr.")){
                        String accessionNumber = tokenizer.nextToken(); // Genbank Accession
                        if(accessionNumber.length()>4){
                            accessionNumbers.add(fixAccession(accessionNumber)) ;
                            ++count ;
                        }
                }
                else
                if(buffer.startsWith("!platform_table_begin")){
                    startReading = true ;
                }
            }
        }
        catch(Exception e){
            errorLogger.error("fail to parse: " + e);
        }
        finally{
            if(file!=null){
                try{
                    reader.close() ;
                }
                catch(Exception e){
                    errorLogger.error("fail to close: " + e);
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

}
