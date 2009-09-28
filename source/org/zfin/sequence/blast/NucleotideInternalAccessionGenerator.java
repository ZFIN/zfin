package org.zfin.sequence.blast;

import org.apache.log4j.Logger;

public class NucleotideInternalAccessionGenerator extends AbstractInternalAccessionGenerator {

    private static final Logger logger = Logger.getLogger(NucleotideInternalAccessionGenerator.class);

    private static NucleotideInternalAccessionGenerator instance ;

    public static NucleotideInternalAccessionGenerator getInstance(){
        if(instance==null){
            instance = new NucleotideInternalAccessionGenerator();
        }
        return instance ; 
    }


    /**
     * @link http://zfinwinserver1.uoregon.edu/fogbugz/default.asp?3256
     */
    public static final String ZFIN_INTERNAL_ACCESSION_NUCLEOTIDE= "ZFINNUCL" ;

    public String getInternalAcessionHeader() {
        return ZFIN_INTERNAL_ACCESSION_NUCLEOTIDE ;
    }

}