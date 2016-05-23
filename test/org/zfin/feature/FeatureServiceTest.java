package org.zfin.feature;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.feature.repository.FeatureService;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.FeatureDBLink;
import org.zfin.sequence.ReferenceDatabase;

import java.util.Collection;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.notNullValue;

public class FeatureServiceTest extends AbstractDatabaseTest {

    static Logger logger = Logger.getLogger(FeatureServiceTest.class);
    FeatureRepository featureRepository = RepositoryFactory.getFeatureRepository();

    @Test
    public void summaryPageLinksTest() {
        Feature feature = featureRepository.getFeatureByID("ZDB-ALT-130627-1");
        Set<FeatureDBLink> featureDbLinks = FeatureService.getSummaryDbLinks(feature);
        assertThat("Feature has summary page dblinks", featureDbLinks, is(notNullValue()));
    }

    @Test
    public void genbankLinksTest() {
        Feature feature = featureRepository.getFeatureByID("ZDB-ALT-100113-10");

        Set<FeatureDBLink> featureDbLinks = FeatureService.getSummaryDbLinks(feature);
        Set<FeatureDBLink> genbankFeatureDbLinks = FeatureService.getGenbankDbLinks(feature);
        assertThat("Feature has genbank dblinks", genbankFeatureDbLinks, is(notNullValue()));
    }

    @Test
    public void getReferenceDatabaseDna() {
        // check that the version number is stripped off...
        ReferenceDatabase referenceDatabase = FeatureService.getForeignDbMutationDetailDna("NM_212779.1");
        assertThat(referenceDatabase, is(notNullValue()));
    }

    @Test
    public void sb60ShouldHaveDnaChangeAttribution() {
        Feature feature = featureRepository.getFeatureByID("ZDB-ALT-071127-4");
        Collection<PublicationAttribution> attributions = FeatureService.getDnaChangeAttributions(feature);
        assertThat(attributions, is(notNullValue()));
        assertThat(attributions, is(not(empty())));
    }

    @Test
    public void sb60ShouldHaveTranscriptConsequenceAttribution() {
        Feature feature = featureRepository.getFeatureByID("ZDB-ALT-071127-4");
        Collection<PublicationAttribution> attributions = FeatureService.getTranscriptConsequenceAttributions(feature);
        assertThat(attributions, is(notNullValue()));
        assertThat(attributions, is(not(empty())));
    }

    @Test
    public void sb60ShouldHaveProteinConsequenceAttribution() {
        Feature feature = featureRepository.getFeatureByID("ZDB-ALT-071127-4");
        Collection<PublicationAttribution> attributions = FeatureService.getProteinConsequenceAttributions(feature);
        assertThat(attributions, is(notNullValue()));
        assertThat(attributions, is(not(empty())));
    }
}

