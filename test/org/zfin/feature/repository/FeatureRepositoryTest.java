package org.zfin.feature.repository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.TestConfiguration;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.feature.FeaturePrefix;
import org.zfin.feature.presentation.FeatureLabEntry;
import org.zfin.feature.presentation.FeaturePrefixLight;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.marker.Marker;
import org.zfin.people.Lab;
import org.zfin.people.Organization;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;

public class FeatureRepositoryTest extends AbstractDatabaseTest {

    private Logger logger = Logger.getLogger(FeatureRepositoryTest.class);

    private static FeatureRepository featureRepository = RepositoryFactory.getFeatureRepository();

    @Before
    public void setUp() {
        TestConfiguration.configure();
    }

    /**
     * Check that genotype anh^m149 has background AB.
     */
    @Test
    public void getFeatureForPublication() {
        //  publication: Abdelilah
        String[] pubIdList = new String[]{
                "ZDB-PUB-970210-18",
                "ZDB-PUB-100702-19", // has features with null prefixes
        };
        for (String pubID : pubIdList) {
            List<Feature> features = featureRepository.getFeaturesByPublication(pubID);

            assertNotNull("feature list exists", features);
            assertTrue("has features", features.size() > 0);
        }

    }

    /**
     * Check that genotype anh^m149 has background AB.
     */
    @Test
    public void getFeatureMarkerRelationshipsForPublication() {
        //  publication: PGE2 (Goessling)
        String pubID = "ZDB-PUB-090324-13";
        List<FeatureMarkerRelationship> features = featureRepository.getFeatureRelationshipsByPublication(pubID);

        assertNotNull("feature list exists", features);
        assertTrue("has features marker relationships", features.size() > 0);

    }

    @Test
    public void getFeatureRelationshipTypesForPointMutationType() {
        List<String> pointMutantTypes = new ArrayList<String>();
        pointMutantTypes.add(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF.toString());

        List<String> types = featureRepository.getRelationshipTypesForFeatureType(FeatureTypeEnum.POINT_MUTATION);
        assertTrue(CollectionUtils.isEqualCollection(pointMutantTypes, types));
    }

    @Test
    public void getFeatureRelationshipTypesForTransgenicInsertionType() {
        List<String> tgInsertionTypes = new ArrayList<String>();
        tgInsertionTypes.add(FeatureMarkerRelationshipTypeEnum.CONTAINS_INNOCUOUS_SEQUENCE_FEATURE.toString());
        tgInsertionTypes.add(FeatureMarkerRelationshipTypeEnum.CONTAINS_PHENOTYPIC_SEQUENCE_FEATURE.toString());
        tgInsertionTypes.add(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF.toString());

        List<String> types = featureRepository.getRelationshipTypesForFeatureType(FeatureTypeEnum.TRANSGENIC_INSERTION);
        assertTrue(CollectionUtils.isEqualCollection(tgInsertionTypes, types));
    }

    @Test
    public void getMarkersForFeatureRelationAndSource() {
        List<Marker> markers = featureRepository.getMarkersForFeatureRelationAndSource("is allele of", "ZDB-PUB-090324-13");
        List<Marker> attributedMarkers = RepositoryFactory.getMarkerRepository().getMarkersForAttribution("ZDB-PUB-090324-13");
        assertEquals(attributedMarkers.size(), markers.size());
        assertTrue(CollectionUtils.isEqualCollection(attributedMarkers, markers));
    }

    @Test
    public void getFeaturePrefixes() {
        List<String> featurePrefixes = featureRepository.getAllFeaturePrefixes();
        assertTrue(featurePrefixes.size() > 100);
        assertTrue(featurePrefixes.size() < 300);
    }

    @Test
    public void getPrefixForLab() {
        assertEquals("b", featureRepository.getCurrentPrefixForLab("ZDB-LAB-970408-1")); // westerfield
        assertEquals("w", featureRepository.getCurrentPrefixForLab("ZDB-LAB-980202-1")); // raible should be 'w', not 'b'
    }

    // TODO: fix durin line designation edit phase.  Was getting a weird trigger error
//    @Test
//    public void setPrefixForLab(){
//        HibernateUtil.createTransaction();
//        assertEquals("w",featureRepository.getCurrentPrefixForLab("ZDB-LAB-980202-1")); // raible should be 'w', not 'b'
//        featureRepository.setCurrentLabPrefix("ZDB-LAB-980202-1","b") ;
//        HibernateUtil.currentSession().flush();
//        assertEquals("b",featureRepository.getCurrentPrefixForLab("ZDB-LAB-980202-1")); // raible should be 'w', not 'b'
//        HibernateUtil.rollbackTransaction();
//    }

    @Test
    public void getAllFeaturePrefixesWithDesignations() {
        List<FeaturePrefixLight> featurePrefixLights = featureRepository.getFeaturePrefixWithLabs();
        boolean containsEd = false;
        boolean containsEr = false;
        boolean containsSb = false;
        boolean containsZy = false;
        for (FeaturePrefixLight featurePrefixLight : featurePrefixLights) {
            if (featurePrefixLight.getPrefix().equals("ba")) {
                assertTrue(featurePrefixLight.getLabList().size() > 0);
            }
            if (featurePrefixLight.getPrefix().equals("bc")) {
                assertTrue(featurePrefixLight.getLabList().size() > 0);
            }
            if (featurePrefixLight.getPrefix().equals("be")) {
                assertTrue(featurePrefixLight.getLabList().size() > 0);
            }
            if (featurePrefixLight.getPrefix().equals("bi")) {
                assertTrue(featurePrefixLight.getLabList().size() > 0);
            }
            if (featurePrefixLight.getPrefix().equals("bk")) {
                assertTrue(featurePrefixLight.getLabList().size() > 0);
            }
            if (featurePrefixLight.getPrefix().equals("ed")) {
                assertNull(featurePrefixLight.getLabList());
                containsEd = true;
            }
            if (featurePrefixLight.getPrefix().equals("er")) {
                assertNull(featurePrefixLight.getLabList());
                containsEr = true;
            }
            if (featurePrefixLight.getPrefix().equals("sb")) {
                assertNull(featurePrefixLight.getLabList());
                containsSb = true;
            }
        }
        assertNotNull(featurePrefixLights);
        assertTrue(featurePrefixLights.size() > 100);
        assertTrue(featurePrefixLights.size() < 300);
        assertTrue(containsEd);
        assertTrue(containsEr);
        assertTrue(containsSb);
    }


    @Test
    public void getFeaturesForPrefixNoSources() {
        List<FeatureLabEntry> featureLabEntries = featureRepository.getFeaturesForPrefix("zf");
        assertTrue(featureLabEntries.size() > 140);
        assertTrue(featureLabEntries.size() < 200);
    }

    @Test
    public void getFeaturesForPrefixHasDominant() {
        List<FeatureLabEntry> featureLabEntries = featureRepository.getFeaturesForPrefix("hi");
        boolean hasDominant = false;

        for (int i = 0; i < featureLabEntries.size() && !hasDominant; i++) {
            if (featureLabEntries.get(i).getFeature().getAbbreviation().startsWith("d") && featureLabEntries.get(i).getFeature().getDominantFeature()) {
                hasDominant = true;
            }
        }

        assertTrue(hasDominant);
        assertTrue(featureLabEntries.size() > 400);
        assertTrue(featureLabEntries.size() < 500);
    }

    @Test
    public void getFeaturesForPrefixShowsOtherLabs() {
        List<FeatureLabEntry> featureLabEntries = featureRepository.getFeaturesForPrefix("a");

        assertTrue(featureLabEntries.size() > 5);
        assertTrue(featureLabEntries.size() < 50);

        for (FeatureLabEntry featureLabEntry : featureLabEntries) {
            if (featureLabEntry.getFeature().getAbbreviation().equals("a75")) {
                assertEquals("The Zon Lab", featureLabEntry.getSourceOrganization().getName());
                assertTrue(featureLabEntry.getSourceOrganization().isActive());
                assertFalse(featureLabEntry.isCurrent());
            }
        }

    }

    @Test
    public void getLabsWithFeaturesForPrefix() {
        List<Organization> labs = featureRepository.getLabsWithFeaturesForPrefix("b");
        assertTrue(labs.size() > 5);
        assertTrue(labs.size() < 20);
    }


    @Test
    public void getLabsOfOriginWithPrefix() {
        List<Organization> labs = featureRepository.getLabsOfOriginWithPrefix();
        assertNotNull(labs);
        logger.error("number of lab: " + labs.size());
        assertTrue(labs.size() > 200); // should be around 283, otherwise closer to 300
        // just choose the first 5
        for (int i = 0; i < 5; i++) {
            // just test the tostring method
            labs.get(i).toString();
            assertNotSame("Lab must have a prefix", 0, featureRepository.getLabPrefixes(labs.get(i).getName()));
        }
        // assert that affolter is not in there
    }

    //    s	true
//m	false
//sk	false
//st	false
    @Test
    public void getLabPrefix() {
        List<FeaturePrefix> featurePrefixes = featureRepository.getLabPrefixes("Stainier Lab");
        assertNotNull(featurePrefixes);
        assertEquals(4, featurePrefixes.size());
        assertEquals("s", featurePrefixes.get(0).getPrefixString());
        assertTrue(featurePrefixes.get(0).isActiveForSet());
        assertEquals("m", featurePrefixes.get(1).getPrefixString());
        assertFalse(featurePrefixes.get(1).isActiveForSet());
        assertEquals("sk", featurePrefixes.get(2).getPrefixString());
        assertFalse(featurePrefixes.get(2).isActiveForSet());
        assertEquals("st", featurePrefixes.get(3).getPrefixString());
        assertFalse(featurePrefixes.get(3).isActiveForSet());
    }

    @Test
    public void attributedFeatures() {
        // Uemura, et al.
        String pubID = "ZDB-PUB-050202-4";
        List<Feature> features = featureRepository.getFeaturesForAttribution(pubID);
        assertNotNull(features);

        List<Marker> markers = RepositoryFactory.getMarkerRepository().getMarkersForAttribution(pubID);
        assertNotNull(markers);
    }

    @Test
    public void getFeaturesByPrefixAndLineNumber() {
        assertNull(featureRepository.getFeatureByPrefixAndLineNumber("b", "1234"));
        assertNull(featureRepository.getFeatureByPrefixAndLineNumber("notavalidprefix", "1"));
        assertNull(featureRepository.getFeatureByPrefixAndLineNumber("b", "notavalidlinenumber"));
        assertNotNull(featureRepository.getFeatureByPrefixAndLineNumber("b", "1"));
    }


    @Test
    public void getFeaturesForLab() {
        List<Feature> features = featureRepository.getFeaturesForLab("ZDB-LAB-970408-1");
        assertNotNull(features);
        assertTrue(features.size() < 100 && features.size() > 10);

    }

    @Test
    public void setLabOfOriginForFeature() {

        String lab1ZdbID = "ZDB-LAB-970408-1"; // monte
        String lab2ZdbID = "ZDB-LAB-970408-13"; // kimmel
        List<Feature> features;
        int size1, size2, totalSize;

        try {
            HibernateUtil.createTransaction();

            features = featureRepository.getFeaturesForLab(lab1ZdbID);
            size1 = features.size();
            assertTrue(size1 > 0);

            features = featureRepository.getFeaturesForLab(lab2ZdbID);
            size2 = features.size();
            assertTrue(size2 > 0);

            totalSize = size1 + size2;
            assertTrue(totalSize > 0);


            Organization lab2 = RepositoryFactory.getProfileRepository().getLabById(lab2ZdbID);
            features = featureRepository.getFeaturesForLab(lab1ZdbID);
            for (Feature feature : features) {
                featureRepository.setLabOfOriginForFeature(lab2, feature);
            }


            features = featureRepository.getFeaturesForLab(lab1ZdbID);
            size1 = features.size();
            assertEquals(0, size1);

            features = featureRepository.getFeaturesForLab(lab2ZdbID);
            size2 = features.size();
            assertEquals(totalSize, size2);

        } catch (Exception e) {
            fail(e.toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }

    @Test
    public void deleteLabOfOriginForFeature() {

        String lab1ZdbID = "ZDB-LAB-970408-1"; // monte
        List<Feature> features;
        int size1;

        try {
            HibernateUtil.createTransaction();

            features = featureRepository.getFeaturesForLab(lab1ZdbID);
            size1 = features.size();
            assertTrue(size1 > 0);


            features = featureRepository.getFeaturesForLab(lab1ZdbID);
            for (Feature feature : features) {
                featureRepository.deleteLabOfOriginForFeature(feature);
            }


            features = featureRepository.getFeaturesForLab(lab1ZdbID);
            size1 = features.size();
            assertEquals(0, size1);

        } catch (Exception e) {
            fail(e.toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }

    @Test
    public void addLabOfOriginForFeature() {

        String lab1ZdbID = "ZDB-LAB-970408-1"; // monte
        List<Feature> features;
        int size1;

        try {
            HibernateUtil.createTransaction();

            features = featureRepository.getFeaturesForLab(lab1ZdbID);
            size1 = features.size();
            assertTrue(size1 > 0);

            Feature myFeature = features.get(0);

            features = featureRepository.getFeaturesForLab(lab1ZdbID);
            for (Feature feature : features) {
                featureRepository.deleteLabOfOriginForFeature(feature);
            }


            features = featureRepository.getFeaturesForLab(lab1ZdbID);
            size1 = features.size();
            assertEquals(0, size1);


            int added = featureRepository.addLabOfOriginForFeature(myFeature, lab1ZdbID);
            assertEquals(1, added);

            features = featureRepository.getFeaturesForLab(lab1ZdbID);
            size1 = features.size();
            assertEquals(1, size1);

        } catch (Exception e) {
            fail(e.toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }

}
