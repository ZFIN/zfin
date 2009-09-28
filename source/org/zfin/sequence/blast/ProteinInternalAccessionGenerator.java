package org.zfin.sequence.blast;

import org.apache.log4j.Logger;

public class ProteinInternalAccessionGenerator extends AbstractInternalAccessionGenerator {

    private static final Logger logger = Logger.getLogger(ProteinInternalAccessionGenerator.class);

    private static ProteinInternalAccessionGenerator instance ;

    public static ProteinInternalAccessionGenerator getInstance(){
        if(instance==null){
            instance = new ProteinInternalAccessionGenerator();
        }
        return instance ;
    }

    /**
     * @link http://zfinwinserver1.uoregon.edu/fogbugz/default.asp?3256
     */
    public static final String ZFIN_INTERNAL_ACCESSION_PROTEIN = "ZFINPROT" ;

    public String getInternalAcessionHeader() {
        return ZFIN_INTERNAL_ACCESSION_PROTEIN;
    }

}