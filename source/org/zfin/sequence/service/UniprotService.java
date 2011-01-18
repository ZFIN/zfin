package org.zfin.sequence.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.repository.SequenceRepository;

import java.io.IOException;

/**
 */
//@Service
public class UniprotService {

    private Logger logger = Logger.getLogger(UniprotService.class) ;

    //    @Autowired
    private SequenceService sequenceService = new SequenceService();
    private SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();
    private ReferenceDatabase uniprotReferenceDatabase = sequenceService.getUniprotDb();

    public Boolean validateAccession(String accession){
        if(CollectionUtils.isNotEmpty(sequenceRepository.getMarkerDBLinksForAccession(accession, uniprotReferenceDatabase))){
            return true ;
        }

        return validateAgainstUniprotWebsite(accession) ;

    }

    public Boolean validateAgainstUniprotWebsite(String accession) {


        HttpClient client = new HttpClient()   ;
        GetMethod method = new GetMethod(uniprotReferenceDatabase.getBaseURL()+accession);
        try {
            int statusCode = client.executeMethod(method);
            if(statusCode != HttpStatus.SC_OK){
                logger.error("status is "+statusCode + " trying to retrieve the accession for uniprot ["+accession+"]");
                return false ;
            }else{
                return true ;
            }
        } catch (IOException e) {
            logger.error(e);
            return false ;
        }
    }

}
