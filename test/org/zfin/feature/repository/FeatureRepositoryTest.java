package org.zfin.feature.repository;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.feature.*;
import org.zfin.feature.presentation.FeatureLabEntry;
import org.zfin.feature.presentation.FeaturePrefixLight;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.gwt.curation.server.CurationFilterRPCImpl;
import org.zfin.gwt.curation.server.FeatureRPCServiceImpl;
import org.zfin.gwt.curation.ui.FeatureRPCService;
import org.zfin.gwt.curation.ui.PublicationNotFoundException;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.FilterValuesDTO;
import org.zfin.gwt.root.dto.MutationDetailControlledVocabularyTermDTO;
import org.zfin.mapping.FeatureLocation;
import org.zfin.marker.Marker;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.profile.Organization;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.getFeatureRepository;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

@Log4j2
public class FeatureRepositoryTest extends AbstractDatabaseTest {

    private static final FeatureRepository featureRepository = RepositoryFactory.getFeatureRepository();

    @After
    public void closeSession() {
        super.closeSession();
        // make sure to close the session to be able to re-create the entities
        HibernateUtil.closeSession();
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
     * Check that feature list is unique
     */
    @Test
    public void getUniqueFeatureListForPublication() {
        String pubID = "ZDB-PUB-130710-52";
        List<Feature> features = featureRepository.getFeaturesByPublication(pubID);
        assertNotNull("feature list exists", CollectionUtils.isNotEmpty(features));
        HashSet<Feature> set = new HashSet<>(features);
        assertEquals("contains duplicate features", features.size(), set.size());
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
    public void getNonSaFeaturesWithGenomicMutDets() {
        List<Feature> features = featureRepository.getNonSaFeaturesWithGenomicMutDets();

        assertNotNull("feature list exists", features);
        assertTrue("non sa  features have genomic mutation details", features.size() > 0);

    }


    @Test
    public void getFeatureRelationshipTypesForPointMutationType() {
        List<String> pointMutantTypes = new ArrayList<>();
        pointMutantTypes.add(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF.toString());
        pointMutantTypes.add(FeatureMarkerRelationshipTypeEnum.CREATED_BY.toString());

        List<String> types = featureRepository.getRelationshipTypesForFeatureType(FeatureTypeEnum.POINT_MUTATION);
        assertTrue(CollectionUtils.isEqualCollection(pointMutantTypes, types));
    }

    @Test
    public void getFeatureRelationshipTypesForTransgenicInsertionType() {
        List<String> tgInsertionTypes = new ArrayList<String>();
        tgInsertionTypes.add(FeatureMarkerRelationshipTypeEnum.CONTAINS_INNOCUOUS_SEQUENCE_FEATURE.toString());
        tgInsertionTypes.add(FeatureMarkerRelationshipTypeEnum.CONTAINS_PHENOTYPIC_SEQUENCE_FEATURE.toString());
        tgInsertionTypes.add(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF.toString());
        tgInsertionTypes.add(FeatureMarkerRelationshipTypeEnum.CREATED_BY.toString());

        List<String> types = featureRepository.getRelationshipTypesForFeatureType(FeatureTypeEnum.TRANSGENIC_INSERTION);
        assertTrue(CollectionUtils.isEqualCollection(tgInsertionTypes, types));
    }

    @Test
    public void getMarkersForFeatureRelationAndSource() {
        List<Marker> attributedMarkers = getMarkerRepository().getMarkersForAttribution("ZDB-PUB-090324-13");
        assertThat(attributedMarkers.size(), greaterThan(10));
    }

    @Test
    public void getFeaturePrefixes() {
        List<String> featurePrefixes = featureRepository.getAllFeaturePrefixes();
        assertTrue(featurePrefixes.size() > 100);
        assertTrue(featurePrefixes.size() < 1000);
    }

    @Test
    public void getPrefixForLab() {
        assertEquals("Westerfield lab has line prefix 'b'", "b", featureRepository.getCurrentPrefixForLab("ZDB-LAB-970408-1"));
        assertEquals("Raible lab has line prefix 'w'", "w", featureRepository.getCurrentPrefixForLab("ZDB-LAB-980202-1"));
    }

    @Test
    public void getAllFeaturePrefixesWithDesignations() {
        List<FeaturePrefixLight> featurePrefixLights = featureRepository.getFeaturePrefixWithLabs();
        boolean containsEd = false;
        boolean containsEr = false;
        boolean containsSb = false;
        assertNotNull(featurePrefixLights);
        for (FeaturePrefixLight featurePrefixLight : featurePrefixLights) {
            if (featurePrefixLight.getPrefix().equals("ba")) {
                assertThat("Expect at least one lab for line designation 'ba'", featurePrefixLight.getLabList().size(), greaterThan(0));
            }
            if (featurePrefixLight.getPrefix().equals("b")) {
                assertThat("Expect at least one lab for line designation 'b'", featurePrefixLight.getLabList().size(), greaterThan(0));
            }
            if (featurePrefixLight.getPrefix().equals("be")) {
                assertThat("Expect at least one lab for line designation 'be'", featurePrefixLight.getLabList().size(), greaterThan(0));
            }
            if (featurePrefixLight.getPrefix().equals("bi")) {
                assertThat("Expect at least one lab for line designation 'bi'", featurePrefixLight.getLabList().size(), greaterThan(0));
            }
            if (featurePrefixLight.getPrefix().equals("bk")) {
                assertThat("Expect at least one lab for line designation 'bk'", featurePrefixLight.getLabList().size(), greaterThan(0));
            }
            if (featurePrefixLight.getPrefix().equals("ed")) {
                containsEd = true;
            }
            if (featurePrefixLight.getPrefix().equals("er")) {
                containsEr = true;
            }
            if (featurePrefixLight.getPrefix().equals("sb")) {
                containsSb = true;
            }
        }
        assertThat("No more than 1000 different prefixe lines", featurePrefixLights.size(), lessThan(1000));
        assertThat("At least 100 different prefixes lines", featurePrefixLights.size(), lessThan(1000));
        assertTrue("Feature line with prefix 'ed' found", containsEd);
        assertTrue("Feature line with prefix 'er' found", containsEr);
        assertTrue("Feature line with prefix 'sb' found", containsSb);
    }


    @Test
    public void getFeaturesForPrefixNoSources() {
        List<FeatureLabEntry> featureLabEntries = featureRepository.getFeaturesForPrefix("zf");
        assertTrue(featureLabEntries.size() > 140);
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
        assertTrue(featureLabEntries.size() < 1000);
    }

    @Test
    public void getFeaturesForPrefixShowsOtherLabs() {
        List<FeatureLabEntry> featureLabEntries = featureRepository.getFeaturesForPrefix("a");

        assertTrue(featureLabEntries.size() > 5);
        assertTrue(featureLabEntries.size() < 500);

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
        assertTrue(labs.size() < 100);
    }


    @Test
    public void getLabsOfOriginWithPrefix() {
        List<Organization> labs = featureRepository.getLabsOfOriginWithPrefix();
        assertNotNull(labs);
        log.info("number of lab: " + labs.size());
        assertTrue(labs.size() > 200);
        // just choose the first 5
        for (int i = 0; i < 5; i++) {
            // just test the toString() method
            labs.get(i).toString();
            assertTrue("Lab must have a prefix", featureRepository.getLabPrefixes(labs.get(i).getName()).size() > 0);
        }
    }

    @Test
    public void getLabPrefix() {
        List<FeaturePrefix> featurePrefixes = featureRepository.getLabPrefixes("Stainier Lab");
        assertNotNull(featurePrefixes);
        assertTrue(featurePrefixes.size() > 3);
    }

    @Test
    public void getFeaturesByPrefixAndLineNumber() {
        assertNull(featureRepository.getFeatureByPrefixAndLineNumber("notavalidprefix", "1"));
        assertNull(featureRepository.getFeatureByPrefixAndLineNumber("b", "notavalidlinenumber"));
        assertNotNull(featureRepository.getFeatureByPrefixAndLineNumber("b", "1"));
    }


    @Test
    public void getFeaturesForLab() {
        List<Feature> features = featureRepository.getFeaturesForLab("ZDB-LAB-970408-1");
        assertNotNull(features);
        assertThat(features.size(), greaterThan(10));
    }

    @Test
    public void getFeaturesForLabExist() {
        Long count = featureRepository.getFeaturesForLabCount("ZDB-LAB-970408-1");
        assertNotNull(count);
        assertThat(count, greaterThan(20L));
    }

    @Test
    public void setLabOfOriginForFeature() {

        String lab1ZdbID = "ZDB-LAB-970408-1"; // monte
        String lab2ZdbID = "ZDB-LAB-970408-13"; // kimmel
        List<Feature> features;
        int size1, size2, totalSize;

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
    }

    @Test
    public void getNumberOfFeaturesForConstruct() {
        Marker construct = getMarkerRepository().getMarkerByID("ZDB-TGCONSTRCT-070117-94");
        int numFeatures = featureRepository.getNumberOfFeaturesForConstruct(construct);
        assertTrue(numFeatures > 0);
    }

    @Test
    public void deleteLabOfOriginForFeature() {

        String lab1ZdbID = "ZDB-LAB-970408-1"; // monte
        List<Feature> features;
        int size1;

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
    }


    @Test
    public void getFeaturesFromMarker() {
        Marker marker = getMarkerRepository().getMarkerByAbbreviation("piru");
        List<Feature> featureList = featureRepository.getFeaturesByMarker(marker);
        Assert.assertNotNull(featureList);
    }

    @Test
    public void getMarkerFromFeature() {
        Feature feature = featureRepository.getFeatureByID("ZDB-ALT-050617-64");
        List<Marker> markerSet = featureRepository.getMarkerIsAlleleOf(feature);
        Assert.assertNotNull(markerSet);
    }

    @Test
    public void getDnaProteinMutationDetail() {
        Feature feature = featureRepository.getFeatureByID("ZDB-ALT-100412-3");
        for (FeatureTranscriptMutationDetail detail : feature.getFeatureTranscriptMutationDetailSet()) {
            detail.getTranscriptConsequence().getDisplayName();
        }
        Assert.assertNotNull(feature);
    }

    @Test
    public void addLabOfOriginForFeature() {

        String lab1ZdbID = "ZDB-LAB-970408-1"; // monte
        List<Feature> features;
        int size1;

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
    }

    @Test
    public void getDnaChangeList() {
        FeatureRPCService service = new FeatureRPCServiceImpl();
        List<MutationDetailControlledVocabularyTermDTO> list = service.getAminoAcidList();
        assertNotNull(list);
    }

    @Test
    public void getPossibleValues() {
        CurationFilterRPCImpl service = new CurationFilterRPCImpl();
        FilterValuesDTO dto = null;
        try {
            dto = service.getPossibleFilterValues("ZDB-PUB-151007-1");
        } catch (PublicationNotFoundException e) {
            fail();
        }
        assertNotNull(dto);
    }

    @Test
    public void getSingleAffectedFeatures() {
        // This takes too long to test.
        //List<Feature> features = getFeatureRepository().getSingleAffectedGeneAlleles();
        Feature feature = getFeatureRepository().getFeatureByID("ZDB-ALT-991130-131");
        assertFalse(getFeatureRepository().isSingleAffectedGeneAlleles(feature));

        feature = getFeatureRepository().getFeatureByID("ZDB-ALT-161003-14982");
        assertTrue(getFeatureRepository().isSingleAffectedGeneAlleles(feature));
    }

    @Test
    public void getAllFeatureLocationsOnGRCz11() {
        Feature feature = getFeatureRepository().getFeatureByID("ZDB-ALT-211102-4");
        FeatureLocation featureLocation = getFeatureRepository().getAllFeatureLocationsOnGRCz11(feature);
        assertNotNull(featureLocation);
        assertEquals("13", featureLocation.getChromosome());

    }

    @Test
    public void getAllFeatureGenomicMutationDetails() {
        List<FeatureGenomicMutationDetail> list = getFeatureRepository().getAllFeatureGenomicMutationDetails();
        assertNotNull(list);
        assertTrue(list.size() > 1000);

    }

    @Test
    public void getAllFeatureList() {
        int i = 100;
        List<Feature> list = getFeatureRepository().getAllFeatureList(i);
        assertNotNull(list);
        assertEquals(i, list.size());

    }

    @Test
    public void getFeaturesCreatedBySequenceTargetingReagent() {
        SequenceTargetingReagent str = RepositoryFactory.getMarkerRepository().getSequenceTargetingReagent("ZDB-CRISPR-230209-1");
        Set<Feature> list = getFeatureRepository().getFeaturesCreatedBySequenceTargetingReagent(str);
        assertNotNull(list);
        assertTrue(list.size() > 0);

    }

    @Test
    public void getFeatureAssay() {
        Feature feature = getFeatureRepository().getFeatureByID("ZDB-ALT-211102-4");
        FeatureAssay featureAssay = getFeatureRepository().getFeatureAssay(feature);
        assertNotNull(featureAssay);
        //assertTrue(featureAssay);

    }

    @Test
    public void getDeletionFeatures() {
        List<Feature> features = getFeatureRepository().getDeletionFeatures();
        assertNotNull(features);
        assertTrue(features.size() > 1000);
    }

    @Test
    public void getFeatureRelationshipsByPublication() {
        List<FeatureMarkerRelationship> featureMarkerRelationships = getFeatureRepository().getFeatureRelationshipsByPublication("ZDB-PUB-210716-17");
        assertNotNull(featureMarkerRelationships);
        assertEquals(7, featureMarkerRelationships.size());
    }

    @Test
    public void getFeaturesByAbbreviation() {
        List<Feature> featureMarkerRelationships = getFeatureRepository().getFeaturesByAbbreviation("hg");
        assertNotNull(featureMarkerRelationships);
        assertTrue(featureMarkerRelationships.size() > 290);
    }

    @Test
    public void getFeaturesForLabNum() {
        List<Feature> featureMarkerRelationships = getFeatureRepository().getFeaturesForLab("ZDB-LAB-970408-1", 500);
        assertNotNull(featureMarkerRelationships);
        assertTrue(featureMarkerRelationships.size() > 45);
    }

    @Test
    public void getFeaturesForLabPagination() {
        Pagination pagination = new Pagination();
        pagination.setLimit(60);
        PaginationResult<Feature> result = getFeatureRepository().getFeaturesForLab("ZDB-LAB-970408-1", pagination);
        assertNotNull(result);
        assertTrue(result.getPopulatedResults().size() > 45);
        for(Feature feature : result.getPopulatedResults()) {
            assertNotNull(feature);
            Set<FeatureAlias> aliases = feature.getAliases();
            for(FeatureAlias alias : aliases) {
                assertNotNull(alias);
            }
        }
    }

}
