package org.zfin.marker;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.zfin.AbstractDatabaseTest;
import org.zfin.expression.service.ExpressionService;
import org.zfin.mapping.presentation.MappedMarkerBean;
import org.zfin.marker.presentation.*;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.MarkerDBLink;

import java.util.*;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;

/**
 * Tests for org.zfin.marker.service.MarkerService
 */
public class MarkerServiceTest extends AbstractDatabaseTest {

    private Logger logger = Logger.getLogger(MarkerServiceTest.class);

    @Test
    public void getRelatedMarkerDisplayTest() {
        //this method isn't in MarkerService, but it's used by MarkerService


        //piles of fake data
        MarkerType gene = new MarkerType();
        gene.setDisplayName("gene");
        HashSet<Marker.TypeGroup> geneGroups = new HashSet<Marker.TypeGroup>();
        geneGroups.add(Marker.TypeGroup.GENEDOM);
        gene.setTypeGroups(geneGroups);

        MarkerType clone = new MarkerType();
        HashSet<Marker.TypeGroup> cloneGroups = new HashSet<Marker.TypeGroup>();
        //todo: this will likely change to something like ALLCLONES
        cloneGroups.add(Marker.TypeGroup.CLONE);
        clone.setTypeGroups(cloneGroups);

        MarkerType transcript = new MarkerType();
        HashSet<Marker.TypeGroup> transcriptGroups = new HashSet<Marker.TypeGroup>();
        transcriptGroups.add(Marker.TypeGroup.GENEDOM);
        transcript.setTypeGroups(transcriptGroups);
        transcript.setDisplayName("transcript");

        Marker gene1 = new Marker();
        gene1.setName("gene");
        gene1.setMarkerType(gene);
        gene1.setZdbID("1");


        Clone BAC1 = new Clone();
        BAC1.setName("BAC1");
        BAC1.setMarkerType(clone);
        BAC1.setZdbID("2");

        Clone BAC2 = new Clone();
        BAC2.setName("BAC2");
        BAC2.setMarkerType(clone);
        BAC2.setZdbID("3");

        Transcript t1 = new Transcript();
        t1.setName("Transcript1");
        t1.setMarkerType(transcript);
        t1.setZdbID("4");


        Transcript t2 = new Transcript();
        t2.setName("Transcript2");
        t2.setMarkerType(transcript);
        t2.setZdbID("5");

        MarkerRelationshipType produces = new MarkerRelationshipType();
        produces.setFirstToSecondLabel("produces");
        produces.setSecondToFirstLabel("produced by");

        MarkerRelationship gene1ProducesT1 = new MarkerRelationship();
        gene1ProducesT1.setMarkerRelationshipType(produces);
        gene1ProducesT1.setFirstMarker(gene1);
        gene1ProducesT1.setSecondMarker(t1);

        RelatedMarker rm = new RelatedMarker(gene1, gene1ProducesT1);
        RelatedMarkerDisplay rmd = new RelatedMarkerDisplay();

        rmd.addRelatedMarker(rm);

        String firstLabel = rmd.keySet().iterator().next();
        TreeMap<MarkerType, TreeSet<RelatedMarker>> typeMap = rmd.get(firstLabel);
        MarkerType firstType = typeMap.keySet().iterator().next();

        assertTrue("RelatedMarkerDisplay should at least one key", rmd.keySet().size() > 0);
        assertTrue("typeMap should have at least one entry", typeMap.keySet().size() > 0);
        assertEquals("firstType should be transcript", transcript, firstType);


    }

    @Test
    public void mergeMarkerTest() {
        MergeMarkerValidator validator = new MergeMarkerValidator();
//        Antibody a = new Antibody();
//        a.setHeavyChainIsotype("a");
//
//        Antibody b = new Antibody();
//        b.setHeavyChainIsotype("b");

        assertFalse(validator.isEqualOrUnspecified("a", "b"));
        assertTrue(validator.isEqualOrUnspecified("a", "a"));
        assertTrue(validator.isEqualOrUnspecified("a", null));
        assertTrue(validator.isEqualOrUnspecified("a", ""));
        assertTrue(validator.isEqualOrUnspecified(null, null));
        assertTrue(validator.isEqualOrUnspecified("", ""));
        assertTrue(validator.isEqualOrUnspecified(null, "b"));
        assertTrue(validator.isEqualOrUnspecified("", "b"));
    }

    @Test
    public void relatedMarkerDisplayFast() {
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByAbbreviation("pax6a");
        List<MarkerRelationshipPresentation> rels;
        rels = MarkerService.getRelatedMarkerDisplayExcludeType(m, false);
        assertNotNull(rels);
        rels = MarkerService.getRelatedMarkerDisplayExcludeType(m, true);
        assertTrue(rels.size() > 3);
        assertNotNull(rels);
    }

    @Test
    public void relatedMarkerDisplayFastMultipleSuppliers() {
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-990415-72");
        List<MarkerRelationshipPresentation> rels;
        rels = MarkerService.getRelatedMarkerDisplayExcludeType(m, false);
        assertNotNull(rels);
        assertEquals(1, rels.size());
        rels = MarkerService.getRelatedMarkerDisplayExcludeType(m, true);
        assertNotNull(rels);
        assertEquals(5, rels.size());

        // the est should have one, and the
        String link1 = rels.get(0).getLinkWithAttributionAndOrderThis();
        assertNotNull(link1);
        String link2 = rels.get(1).getLinkWithAttributionAndOrderThis();
        assertNotNull(link2);
    }

    @Test
    public void relatedMarkerDisplayFastMultipleAttributions() {
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010718-1");
        List<MarkerRelationshipPresentation> rels;
        rels = MarkerService.getRelatedMarkerDisplayExcludeType(m, false);
        assertNotNull(rels);
        assertEquals(1, rels.size());
        rels = MarkerService.getRelatedMarkerDisplayExcludeType(m, true);
        assertNotNull(rels);
        assertEquals(2, rels.size());

        // the est should have 3 attributions and one orderThis
        MarkerRelationshipPresentation mrp = rels.get(0);
        assertEquals(1, mrp.getOrganizationLinks().size());
        assertEquals(3, mrp.getAttributionZdbIDs().size());

        String link1 = rels.get(0).getLinkWithAttributionAndOrderThis();
        assertNotNull(link1);
        String link2 = rels.get(1).getLinkWithAttributionAndOrderThis();
        assertNotNull(link2);
    }


    @Test
    public void geneViewController() throws Exception {
        GeneViewController geneViewController = new GeneViewController();
        geneViewController.setExpressionService(new ExpressionService());
        geneViewController.setMarkerRepository(RepositoryFactory.getMarkerRepository());
        Model model = new ExtendedModelMap();
        geneViewController.getGeneView(model, "ZDB-GENE-001103-1");
    }


    @Test
    public void getMutantsOnGene() {
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByAbbreviation("cdh1");
        assertNotNull(m);
        String display = RepositoryFactory.getMutantRepository().getMutantLinesDisplay(m.getZdbID());
        assertNotNull(display);
    }

    @Test
    public void getSequenceInfoSummary() {
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        SequenceInfo sequenceInfo = MarkerService.getSequenceInfoSummary(m);
        assertThat(sequenceInfo.getNumberDBLinks(), greaterThan(11));
        assertThat(sequenceInfo.getNumberDBLinks(), lessThan(50)); // was 18
        assertEquals(4, sequenceInfo.getDbLinks().size());
    }

    @Test
    public void getSequenceInfoFull() {
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        SequencePageInfoBean sequenceInfo = MarkerService.getSequenceInfoFull(m);
        assertThat(sequenceInfo.getDbLinks().size(), greaterThan(7));
        assertThat(sequenceInfo.getDbLinks().size(), lessThan(50)); // was 9
        Iterator<DBLink> iter = sequenceInfo.getDbLinks().iterator();
        DBLink dbLink;
        dbLink = iter.next();
        assertEquals(ForeignDBDataType.DataType.RNA, dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.REFSEQ, dbLink.getReferenceDatabase().getForeignDB().getDbName());
        dbLink = iter.next();
        assertEquals(ForeignDBDataType.DataType.RNA, dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.GENBANK, dbLink.getReferenceDatabase().getForeignDB().getDbName());
//        dbLink = iter.next();
//        assertEquals(ForeignDBDataType.DataType.RNA,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
//        assertEquals(ForeignDB.AvailableName.GENBANK,dbLink.getReferenceDatabase().getForeignDB().getDbName());

//        dbLink = iter.next();
//        assertEquals(ForeignDBDataType.DataType.POLYPEPTIDE,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
//        assertEquals(ForeignDB.AvailableName.UNIPROTKB,dbLink.getReferenceDatabase().getForeignDB().getDbName());
//
//        dbLink = iter.next();
//        assertEquals(ForeignDBDataType.DataType.POLYPEPTIDE,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
//        assertEquals(ForeignDB.AvailableName.REFSEQ,dbLink.getReferenceDatabase().getForeignDB().getDbName());

//        dbLink = iter.next();
//        assertEquals(ForeignDBDataType.DataType.POLYPEPTIDE,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
//        assertEquals(ForeignDB.AvailableName.GENPEPT,dbLink.getReferenceDatabase().getForeignDB().getDbName());

//        dbLink = iter.next();
//        assertEquals(ForeignDBDataType.DataType.SEQUENCE_CLUSTERS,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
//        assertEquals(ForeignDB.AvailableName.UNIGENE,dbLink.getReferenceDatabase().getForeignDB().getDbName());

        MarkerDBLink markerDBLink;

        Iterator<MarkerDBLink> firstIter = sequenceInfo.getFirstRelatedMarkerDBLink().iterator();
        assertEquals(1, sequenceInfo.getFirstRelatedMarkerDBLink().size());
        markerDBLink = firstIter.next();

        assertEquals(ForeignDBDataType.DataType.GENOMIC, markerDBLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.GENBANK, markerDBLink.getReferenceDatabase().getForeignDB().getDbName());
        assertEquals("eu736", markerDBLink.getMarker().getAbbreviation());


        Iterator<MarkerDBLink> secondIter = sequenceInfo.getSecondRelatedMarkerDBLink().iterator();
        assertEquals(3, sequenceInfo.getSecondRelatedMarkerDBLink().size());
        markerDBLink = secondIter.next();

        assertEquals(ForeignDBDataType.DataType.GENOMIC, markerDBLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.GENBANK, markerDBLink.getReferenceDatabase().getForeignDB().getDbName());
        assertEquals(Marker.Type.BAC, markerDBLink.getMarker().getType());
        markerDBLink = secondIter.next();
        assertEquals(Marker.Type.BAC, markerDBLink.getMarker().getType());
        markerDBLink = secondIter.next();
        assertEquals(Marker.Type.FOSMID, markerDBLink.getMarker().getType());
    }

    @Test
    public void getSnpMappedMarkers() {
        Marker marker;
        MappedMarkerBean mappedMarkerBean;

        marker = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-SNP-060626-1008");
        assertNotNull(marker);
        mappedMarkerBean = MarkerService.getSnpMappedMarkers(marker);
        assertNotNull(mappedMarkerBean);
        List<String> unmappedMarkers = mappedMarkerBean.getUnMappedMarkers();
        assertEquals(1, unmappedMarkers.size());
        assertEquals("25", unmappedMarkers.iterator().next());

        // case 7593, should not be null even if null related markers
        marker = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-SNP-060626-97");
        assertNotNull(marker);
        mappedMarkerBean = MarkerService.getSnpMappedMarkers(marker);
        assertNotNull(mappedMarkerBean);
    }

    @Test
    public void getSequenceInfoFullNotDupe() {
        Marker m = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-GENE-030616-329");
        SequencePageInfoBean sequencePageInfoBean = MarkerService.getSequenceInfoFull(m);
        Collection<MarkerDBLink> secMarkerDBLinks = sequencePageInfoBean.getSecondRelatedMarkerDBLink();
        assertEquals(2, secMarkerDBLinks.size());

    }

    @Test
    public void pullClonesOntoGeneFromTranscript() {
        Marker m = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-GENE-010606-1");
        GeneBean geneBean = new GeneBean();
        geneBean.setMarker(m);
        MarkerService.createDefaultViewForMarker(geneBean);
        assertEquals(1, geneBean.getMarkerRelationshipPresentationList().size());
        assertEquals("EST", geneBean.getMarkerRelationshipPresentationList().get(0).getMarkerType());
        MarkerService.pullClonesOntoGeneFromTranscript(geneBean);
        assertEquals(4, geneBean.getMarkerRelationshipPresentationList().size());

        assertEquals("BAC", geneBean.getMarkerRelationshipPresentationList().get(0).getMarkerType());
        assertEquals("BAC", geneBean.getMarkerRelationshipPresentationList().get(1).getMarkerType());
        assertEquals("Fosmid", geneBean.getMarkerRelationshipPresentationList().get(2).getMarkerType());
        assertEquals("EST", geneBean.getMarkerRelationshipPresentationList().get(3).getMarkerType());
    }

    @Test
    public void pullGeneOntoCloneFromTranscript() {
        Marker m = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-BAC-100127-974");
        CloneBean cloneBean = new CloneBean();
        cloneBean.setMarker(m);
        MarkerService.createDefaultViewForMarker(cloneBean);
        assertEquals(3, cloneBean.getMarkerRelationshipPresentationList().size());
        MarkerService.pullGeneOntoCloneFromTranscript(cloneBean);
        assertEquals(4, cloneBean.getMarkerRelationshipPresentationList().size());
        assertEquals("Gene", cloneBean.getMarkerRelationshipPresentationList().get(0).getMarkerType());
    }


    @Test
    public void pullGeneOntoCloneFromTranscript2() {
        Marker m = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-BAC-060503-669");
        CloneBean cloneBean = new CloneBean();
        cloneBean.setMarker(m);
        MarkerService.createDefaultViewForMarker(cloneBean);
        int count = 0;
        for (MarkerRelationshipPresentation markerRelationshipPresentation : cloneBean.getMarkerRelationshipPresentationList()) {
            if (markerRelationshipPresentation.getZdbId().startsWith("ZDB-GENE")) {
                ++count;
            }
        }
        assertEquals(9, count);
        assertThat(cloneBean.getMarkerRelationshipPresentationList().size(), greaterThan(30));
        assertThat(cloneBean.getMarkerRelationshipPresentationList().size(), lessThan(40));


        MarkerService.pullGeneOntoCloneFromTranscript(cloneBean);
        count = 0;
        for (MarkerRelationshipPresentation markerRelationshipPresentation : cloneBean.getMarkerRelationshipPresentationList()) {
            if (markerRelationshipPresentation.getZdbId().startsWith("ZDB-GENE")) {
                ++count;
            }
        }
        assertTrue(count > 10);
        assertThat(cloneBean.getMarkerRelationshipPresentationList().size(), greaterThan(30));
        assertThat(cloneBean.getMarkerRelationshipPresentationList().size(), lessThan(40));
    }
}
