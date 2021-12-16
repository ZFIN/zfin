package org.zfin.marker;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.zfin.AbstractDatabaseTest;
import org.zfin.AppConfig;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.ui.DuplicateEntryException;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.service.MarkerAttributionService;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.publication.Publication;

import java.util.List;

import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.*;

/**
 * Tests for org.zfin.marker.service.MarkerAttributionService
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
@WebAppConfiguration
public class MarkerAttributionServiceTest extends AbstractDatabaseTest {

    @Test
    public void morpholinoAttributionTriggersGeneTargetAttributionTest() {
        InfrastructureRepository infrastructureRepository = getInfrastructureRepository();

        // morpholino MO1-a2ml
        Marker m = getMarkerRepository().getMarkerByID("ZDB-MRPHLNO-090212-1");

        // Pfeffer
        Publication publication = getPublicationRepository().getPublication("ZDB-PUB-980916-4");

        try {
            MarkerAttributionService.addAttributionForMarkerName(m.getAbbreviation(), publication.getZdbID());
        } catch (TermNotFoundException e) {
            fail("Caught TermNotFoundException");
        } catch (DuplicateEntryException e) {
            fail("Caught DuplicateEntryException");
        }

        //assert the record attribution now exists for the morpholino and publication:
        RecordAttribution morpholinoRecordAttribution = infrastructureRepository.getRecordAttribution(m.getZdbID(), publication.getZdbID(), RecordAttribution.SourceType.STANDARD);
        assertNotNull("morpholino record should be found", morpholinoRecordAttribution);
        assertEquals("morpholino ID should match", m.getZdbID(), morpholinoRecordAttribution.getDataZdbID());

        //assert MO's related gene(s) are now associated with the publication
        SequenceTargetingReagent str = getMarkerRepository().getSequenceTargetingReagent(m.zdbID);
        List<Marker> genes = str.getTargetGenes();
        assertEquals("Should get 2 genes for this morpholino", 2, genes.size());
        for(Marker gene : genes) {
            RecordAttribution geneRecordAttribution = infrastructureRepository.getRecordAttribution(gene.getZdbID(), publication.getZdbID(), RecordAttribution.SourceType.STANDARD);
            assertNotNull("gene retrieved from attribution should not be null", geneRecordAttribution.getDataZdbID());
            assertEquals("gene ID should match", gene.getZdbID(), geneRecordAttribution.getDataZdbID());
        }
    }

    @Test
    public void onlyMakeStrAttributionsOnJournalTypePublicationsTest() {
        InfrastructureRepository infrastructureRepository = getInfrastructureRepository();

        // morpholino MO1-a2ml
        Marker m = getMarkerRepository().getMarkerByID("ZDB-MRPHLNO-090212-1");

        // Transgenic Line Submissions (curation type of publication)
        Publication publication = getPublicationRepository().getPublication("ZDB-PUB-100216-1");

        try {
            MarkerAttributionService.addAttributionForMarkerName(m.getAbbreviation(), publication.getZdbID());
        } catch (TermNotFoundException e) {
            fail("Caught TermNotFoundException");
        } catch (DuplicateEntryException e) {
            fail("Caught DuplicateEntryException");
        }

        //assert the record attribution now exists for the morpholino and publication:
        RecordAttribution morpholinoRecordAttribution = infrastructureRepository.getRecordAttribution(m.getZdbID(), publication.getZdbID(), RecordAttribution.SourceType.STANDARD);
        assertNotNull("morpholino record should be found", morpholinoRecordAttribution);
        assertEquals("morpholino ID should match", m.getZdbID(), morpholinoRecordAttribution.getDataZdbID());

        //assert MO's related gene(s) are now associated with the publication
        SequenceTargetingReagent str = getMarkerRepository().getSequenceTargetingReagent(m.zdbID);
        List<Marker> genes = str.getTargetGenes();
        assertEquals("Should get 2 genes for this morpholino", 2, genes.size());
        for(Marker gene : genes) {
            RecordAttribution geneRecordAttribution = infrastructureRepository.getRecordAttribution(gene.getZdbID(), publication.getZdbID(), RecordAttribution.SourceType.STANDARD);
            assertNull("gene retrieved from attribution SHOULD BE null since the publication is not journal type", geneRecordAttribution);
        }
    }

    @Test
    public void nonStrAttributionDoesNotTriggerGeneTargetAttributionTest() {
        InfrastructureRepository infrastructureRepository = getInfrastructureRepository();

        // Antibody Ab1-opn4
        Marker antibody = getMarkerRepository().getMarkerByID("ZDB-ATB-120124-1");

        // Pfeffer
        Publication publication = getPublicationRepository().getPublication("ZDB-PUB-980916-4");

        try {
            MarkerAttributionService.addAttributionForMarkerName(antibody.getAbbreviation(), publication.getZdbID());
        } catch (TermNotFoundException e) {
            fail("Caught TermNotFoundException");
        } catch (DuplicateEntryException e) {
            fail("Caught DuplicateEntryException");
        }

        //assert the record attribution now exists for the morpholino and publication:
        RecordAttribution recordAttribution = infrastructureRepository.getRecordAttribution(antibody.getZdbID(), publication.getZdbID(), RecordAttribution.SourceType.STANDARD);
        assertNotNull("antibody record should be found", recordAttribution);
        assertEquals("antibody ID should match", antibody.getZdbID(), recordAttribution.getDataZdbID());

        //assert antibody's related gene(s) are not associated with the publication
        SequenceTargetingReagent str = getMarkerRepository().getSequenceTargetingReagent(antibody.zdbID);
        assertNull(str);
    }

}
