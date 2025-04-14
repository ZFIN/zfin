package org.zfin.feature;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.NativeQuery;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.expression.ExpressionResult2;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.feature.repository.FeatureService;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.presentation.PhenotypeOnMarkerBean;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.FeatureDBLink;
import org.zfin.sequence.ReferenceDatabase;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.zfin.framework.HibernateUtil.currentSession;

import org.junit.Ignore;

public class FeatureServiceTest extends AbstractDatabaseTest {

    static Logger logger = LogManager.getLogger(FeatureServiceTest.class);
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
    @Ignore
    public void zircLinksTest() {
        Feature feature = featureRepository.getFeatureByID("ZDB-ALT-020426-42");

        Set<FeatureDBLink> featureDbLinks = FeatureService.getSummaryDbLinks(feature);
        FeatureDBLink zircGenoLink = FeatureService.getZIRCGenoLink(feature);
        assertThat("Feature has zirc genotyping protocol", zircGenoLink, is(notNullValue()));
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

    @Test
    public void checkFeatureGenomeEvidenceMapping() {
        String evidenceCode = "TAS";
        String termId = FeatureService.getFeatureGenomeLocationEvidenceCodeTerm(evidenceCode);
    }

    @Test
    public void checkFeatureWithCleanPhenotypeOnPub() {
        Feature feature = featureRepository.getFeatureByID("ZDB-ALT-190821-6");
        PhenotypeOnMarkerBean bean = FeatureService.getPhenotypeOnFeature(feature);
        assertNotNull(bean);
    }


    @Test
    public void checkFeatureWithCleanPhenotypeOnPubPerformance() {
        Calendar start = Calendar.getInstance();
        List<String> ids = List.of("ZDB-ALT-980203-444", " ZDB-ALT-110504-1", " ZDB-ALT-140521-1", " ZDB-ALT-250331-5", " ZDB-ALT-250331-4", " ZDB-ALT-120723-3", " ZDB-ALT-990423-22", " ZDB-ALT-060821-4");
        for (String id : ids) {
            Feature feature = featureRepository.getFeatureByID("ZDB-ALT-190821-6");
            PhenotypeOnMarkerBean bean = FeatureService.getPhenotypeOnFeature(feature);
            assertNotNull(bean);
        }
        Calendar end = Calendar.getInstance();
        long timediff = end.getTimeInMillis() - start.getTimeInMillis();
        assertTrue(timediff < 3000);
    }


    @Test
    public void testRefactorLogicRetainedForGetPhenotypeFromExpressionsByFeature() {
        List<String> ids = List.of("ZDB-ALT-980203-444", "ZDB-ALT-110504-1", "ZDB-ALT-140521-1", "ZDB-ALT-250331-5", "ZDB-ALT-250331-4", "ZDB-ALT-120723-3", "ZDB-ALT-990423-22", "ZDB-ALT-060821-4");
        for (String id : ids) {
            System.out.println("Comparing results of 2 versions of getPhenotypeFromExpressionsByFeature for " + id);
            List<ExpressionResult2> phenotypes = RepositoryFactory.getExpressionRepository().getPhenotypeFromExpressionsByFeature(id);
            List<ExpressionResult2> phenotypes2 = RepositoryFactory.getExpressionRepository().getPhenotypeFromExpressionsByFeatureSlowPerformance(id);
            boolean isEqual = CollectionUtils.isEqualCollection(phenotypes, phenotypes2);
            if (isEqual) {
                System.out.println("Phenotypes are equal for getPhenotypeFromExpressionsByFeature for " + id + " size: " + phenotypes.size() + " = " + phenotypes2.size());
            } else {
                fail("Phenotypes are not equal for getPhenotypeFromExpressionsByFeature for " + id);
            }
        }
    }

    @Test
    public void testRefactorLogicRetainedForGetPhenotypeFromExpressionsByFeature2() {
        NativeQuery<String> query = currentSession().createNativeQuery("select feature_zdb_id from feature order by random() limit 100", String.class);
        List<String> ids = query.list();
        ids.addAll(List.of("ZDB-ALT-980203-444", "ZDB-ALT-110504-1", "ZDB-ALT-140521-1", "ZDB-ALT-250331-5", "ZDB-ALT-250331-4", "ZDB-ALT-120723-3", "ZDB-ALT-990423-22", "ZDB-ALT-060821-4"));

        for (String id : ids) {
            System.out.println("Comparing results of 2 versions of getPhenotypeFromExpressionsByFeature for " + id);
            List<ExpressionResult2> phenotypes = RepositoryFactory.getExpressionRepository().getPhenotypeFromExpressionsByFeature(id);
            List<ExpressionResult2> phenotypes2 = RepositoryFactory.getExpressionRepository().getPhenotypeFromExpressionsByFeatureSlowPerformance(id);
            boolean isEqual = CollectionUtils.isEqualCollection(phenotypes, phenotypes2);
            if (isEqual) {
                System.out.println("Phenotypes are equal for getPhenotypeFromExpressionsByFeature for " + id + " size: " + phenotypes.size() + " = " + phenotypes2.size() + "\n");
            } else {
                fail("Phenotypes are not equal for getPhenotypeFromExpressionsByFeature for " + id);
            }
        }
    }

    @Test
    public void checkFeatureWithCleanPhenotypeOnPubPerformance3() {
        Calendar start = Calendar.getInstance();
        List<ExpressionResult2> phenotypes;
        String id = "ZDB-ALT-980203-444";
        phenotypes = RepositoryFactory.getExpressionRepository().getPhenotypeFromExpressionsByFeature(id);
        System.out.println("Phenotypes size");
        System.out.println(phenotypes.size());
        Calendar end = Calendar.getInstance();
        long timediff = end.getTimeInMillis() - start.getTimeInMillis();
        System.out.println("Time taken: " + timediff);
        assertTrue(timediff < 3000);
    }


}

