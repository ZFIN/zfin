package org.zfin.sequence.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.framework.HibernateUtil;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.repository.SequenceRepository;

import java.io.IOException;

/**
 */
//@Service
public class UniprotService {

    private Logger logger = LogManager.getLogger(UniprotService.class) ;

    //    @Autowired
    private SequenceService sequenceService = new SequenceService();
    private SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();
    private ReferenceDatabase uniprotReferenceDatabase = sequenceService.getUniprotDb();
    private String uniprotUrl ;

    public Boolean validateAccession(String accession){
        if(CollectionUtils.isNotEmpty(sequenceRepository.getMarkerDBLinksForAccession(accession, uniprotReferenceDatabase))){
            return true ;
        }

        return validateAgainstUniprotWebsite(accession) ;

    }

    public String getUniprotBaseUrl() {
        if(uniprotUrl==null){
            HibernateUtil.currentSession().refresh(uniprotReferenceDatabase);
            uniprotUrl = uniprotReferenceDatabase.getBaseURL();
        }
        return uniprotUrl;
    }

    public Boolean validateAgainstUniprotWebsite(String accession) {


        DefaultHttpClient client = new DefaultHttpClient();
        try {
            HttpResponse response = client.execute(new HttpGet(getUniprotBaseUrl() + accession));
            int statusCode = response.getStatusLine().getStatusCode();
            boolean isOk = (statusCode == HttpStatus.SC_OK);
            if (!isOk) {
                logger.error("status is " + statusCode + " trying to retrieve the accession for uniprot [" + accession + "]");
            }
            return isOk;
        } catch (IOException e) {
            logger.error(e);
            return false;
        }
    }

}
