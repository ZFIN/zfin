package org.zfin.feature.service;


import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.zfin.AbstractDatabaseTest;
import org.zfin.AppConfig;
import org.zfin.feature.Feature;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.ui.DuplicateEntryException;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.service.MarkerAttributionService;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.*;

/**
 * Tests for org.zfin.feature.service.FeatureAttributionService
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
@WebAppConfiguration
public class FeatureAttributionServiceTest extends AbstractDatabaseTest {

    @Test
    public void alleleAttributionTriggersGeneAttributionTest() {
        InfrastructureRepository infrastructureRepository = getInfrastructureRepository();

        // allele hu715
        Feature hu715Feature = getFeatureRepository().getFeatureByID("ZDB-ALT-030922-6");

        // Pfeffer
        Publication publication = getPublicationRepository().getPublication("ZDB-PUB-980916-4");

        try {
            FeatureAttributionService.addFeatureAttribution(hu715Feature.getAbbreviation(), publication.getZdbID());
        } catch (TermNotFoundException e) {
            fail("Caught TermNotFoundException");
        } catch (DuplicateEntryException e) {
            fail("Caught DuplicateEntryException");
        }

        //assert the record attribution now exists for the morpholino and publication:
        RecordAttribution alleleRecordAttribution = infrastructureRepository.getRecordAttribution(hu715Feature.getZdbID(), publication.getZdbID(), RecordAttribution.SourceType.STANDARD);
        assertNotNull("allele record should be found", alleleRecordAttribution);
        assertEquals("allele ID should match", hu715Feature.getZdbID(), alleleRecordAttribution.getDataZdbID());

        //assert MO's related gene(s) are now associated with the publication
        Set<Marker> genes = hu715Feature.getAffectedGenes();
        assertEquals("Should get 1 genes for this allele", 1, genes.size());
        for (Marker gene : genes) {
            RecordAttribution geneRecordAttribution = infrastructureRepository.getRecordAttribution(gene.getZdbID(), publication.getZdbID(), RecordAttribution.SourceType.STANDARD);
            assertNotNull("gene retrieved from attribution should not be null", geneRecordAttribution);
            assertNotNull("gene ID should not be null", geneRecordAttribution.getDataZdbID());
            assertEquals("gene ID should match", gene.getZdbID(), geneRecordAttribution.getDataZdbID());
        }
    }

    @Test
    @Ignore
    public void certainFeaturesShouldNotCreateAttributionTest() {
        //Do we want to handle these feature types differently? (FeatureTypeEnum)
//        TRANSGENIC_INSERTION
//        POINT_MUTATION
//        DELETION
//        INSERTION
//        INDEL
//        TRANSLOC
//        INVERSION
//        DEFICIENCY
//        COMPLEX_SUBSTITUTION
//        SEQUENCE_VARIANT
//        UNSPECIFIED
//        MNV
        fail("TODO: I assume there are some features that we won't want to add related attributions for?");
    }
}