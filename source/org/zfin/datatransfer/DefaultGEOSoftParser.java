package org.zfin.datatransfer;

import org.apache.log4j.Logger;

/**
 * To parse the GEO 2715 file.

 */
public class DefaultGEOSoftParser extends AbstractGEOSoftProcessor {

    private final Logger logger = Logger.getLogger( DefaultGEOSoftParser.class) ;

    public String parseLine(String line) {
        String[] strings = line.split("\t") ;
        if(strings.length>=getAccessionColumn()){
            String accessionNumber = strings[getAccessionColumn()-1]; // Genbank Accession
            // need to check to see if upper-case
            if(accessionNumber.toUpperCase().equals(accessionNumber) && accessionNumber.length()>4){
                return fixAccession(accessionNumber) ;
            }
        }
        return null ;
    }

}
