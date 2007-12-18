package org.zfin.datatransfer;

import org.zfin.sequence.Accession;
import org.zfin.sequence.ReferenceDatabase;

import java.util.List;
import java.util.Set;


public abstract class SoftParser {

    public abstract Set<String> parseUniqueNumbers() ;
//    public Set<Accession> parse(ReferenceDatabase... referenceDatabase) ;
    public abstract Set<Accession> parse() ;

    /**
     * Destructive method.
     * @param accession
     * @return Accession without a .
     */
    public String fixAccession(String accession){
        int dotIndex = accession.indexOf(".") ;
        if( dotIndex>0){
            return accession.substring(0,dotIndex) ;
        }
        else{
            return accession ; 
        }
    }
}
