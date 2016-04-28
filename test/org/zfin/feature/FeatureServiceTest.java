package org.zfin.feature;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.feature.repository.FeatureService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.FeatureDBLink;
import org.zfin.sequence.ReferenceDatabase;

import java.util.Set;

import static org.junit.Assert.assertNotNull;

public class FeatureServiceTest extends AbstractDatabaseTest {

    static Logger logger = Logger.getLogger(FeatureServiceTest.class);
    FeatureRepository featureRepository = RepositoryFactory.getFeatureRepository();

    @Test
    public void summaryPageLinksTest() {
        Feature feature = featureRepository.getFeatureByID("ZDB-ALT-130627-1");
        Set<FeatureDBLink> featureDbLinks = FeatureService.getSummaryDbLinks(feature);
        assertNotNull("Feature has summary page dblinks", featureDbLinks);
    }

    @Test
    public void genbankLinksTest() {
        Feature feature = featureRepository.getFeatureByID("ZDB-ALT-100113-10");

        Set<FeatureDBLink> featureDbLinks = FeatureService.getSummaryDbLinks(feature);
        Set<FeatureDBLink> genbankFeatureDbLinks = FeatureService.getGenbankDbLinks(feature);
        assertNotNull("Feature has genbank dblinks", genbankFeatureDbLinks);
    }

    @Test
    public void getReferenceDatabaseDna() {
        // check that the version number is stripped off...
        ReferenceDatabase referenceDatabase = FeatureService.getForeignDbMutationDetailDna("NM_212779.1");
        assertNotNull(referenceDatabase);
    }
}

