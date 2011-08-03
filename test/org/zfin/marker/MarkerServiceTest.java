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

import static org.junit.Assert.*;

/**
 * Tests for org.zfin.marker.service.MarkerService
 */
public class MarkerServiceTest extends AbstractDatabaseTest{

    private Logger logger = Logger.getLogger(MarkerServiceTest.class) ;

    String[] geneIds = new String[]{
            "ZDB-GENE-980526-474",
            "ZDB-GENE-980528-2059",
            "ZDB-GENE-990415-72",
            "ZDB-GENE-980526-332",
            "ZDB-GENE-980526-526",
            "ZDB-GENE-980526-268",
            "ZDB-GENE-980526-260",
            "ZDB-GENE-000329-5",
            "ZDB-GENE-990415-173",
            "ZDB-GENE-990415-8",
            "ZDB-GENE-980526-125",
            "ZDB-GENE-980526-87",
            "ZDB-GENE-011207-1",
            "ZDB-GENE-980526-426",
    };

    @Test
    public void getRelatedMarkerDisplayTest() {
        //this method isn't in MarkerService, but it's used by MarkerService


        //piles of fake data
        MarkerType gene = new MarkerType();
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

        RelatedMarker rm = new RelatedMarker(gene1,gene1ProducesT1);
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
    public void mergeMarkerTest(){
        MergeMarkerValidator validator = new MergeMarkerValidator();
//        Antibody a = new Antibody();
//        a.setHeavyChainIsotype("a");
//
//        Antibody b = new Antibody();
//        b.setHeavyChainIsotype("b");

        assertFalse(validator.isEqualOrUnspecified("a","b"));
        assertTrue(validator.isEqualOrUnspecified("a","a"));
        assertTrue(validator.isEqualOrUnspecified("a",null));
        assertTrue(validator.isEqualOrUnspecified("a",""));
        assertTrue(validator.isEqualOrUnspecified(null,null));
        assertTrue(validator.isEqualOrUnspecified("",""));
        assertTrue(validator.isEqualOrUnspecified(null,"b"));
        assertTrue(validator.isEqualOrUnspecified("","b"));
    }

    @Test
    public void relatedMarkerDisplayFast(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByAbbreviation("pax6a");
        List<MarkerRelationshipPresentation> rels ;
        rels = MarkerService.getRelatedMarkerDisplayExcludeType(m, false) ;
        assertNotNull(rels);
        rels = MarkerService.getRelatedMarkerDisplayExcludeType(m, true) ;
        assertTrue(rels.size() > 3);
        assertNotNull(rels);
    }

    @Test
    public void relatedMarkerDisplayFastMultipleSuppliers(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-990415-72");
        List<MarkerRelationshipPresentation> rels ;
        rels = MarkerService.getRelatedMarkerDisplayExcludeType(m, false) ;
        assertNotNull(rels);
        assertEquals(1,rels.size());
        rels = MarkerService.getRelatedMarkerDisplayExcludeType(m, true) ;
        assertNotNull(rels);
        assertEquals(5,rels.size());

        // the est should have one, and the
        String link1 = rels.get(0).getLinkWithAttributionAndOrderThis();
        assertNotNull(link1);
        String link2 = rels.get(1).getLinkWithAttributionAndOrderThis();
        assertNotNull(link2);
    }

    @Test
    public void relatedMarkerDisplayFastMultipleAttributions(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010718-1");
        List<MarkerRelationshipPresentation> rels ;
        rels = MarkerService.getRelatedMarkerDisplayExcludeType(m, false) ;
        assertNotNull(rels);
        assertEquals(1,rels.size());
        rels = MarkerService.getRelatedMarkerDisplayExcludeType(m, true) ;
        assertNotNull(rels);
        assertEquals(2,rels.size());

        // the est should have 3 attributions and one orderThis
        MarkerRelationshipPresentation mrp = rels.get(0) ;
        assertEquals(1,mrp.getOrganizationLinks().size());
        assertEquals(3,mrp.getAttributionZdbIDs().size());

        String link1 = rels.get(0).getLinkWithAttributionAndOrderThis();
        assertNotNull(link1);
        String link2 = rels.get(1).getLinkWithAttributionAndOrderThis();
        assertNotNull(link2);
    }


    @Test
    public void geneViewController() throws Exception{
        GeneViewController geneViewController = new GeneViewController();
        geneViewController.setExpressionService(new ExpressionService());
        Model model = new ExtendedModelMap();
        geneViewController.getGeneView(model,"ZDB-GENE-001103-1");
    }


    @Test
    public void getMutantsOnGene() {
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByAbbreviation("cdh1");
        assertNotNull(m);
        String display = RepositoryFactory.getMutantRepository().getMutantLinesDisplay(m.getZdbID());
        assertNotNull(display);
    }

    @Test
    public void getSequenceInfoSummary(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        SequenceInfo sequenceInfo = MarkerService.getSequenceInfoSummary(m);
        // should be 6 here! other 2 are related . .
        assertEquals(4,sequenceInfo.getDbLinks().size());
        Iterator<DBLink> iter = sequenceInfo.getDbLinks().iterator() ;
        DBLink dbLink ;
        dbLink = iter.next();
//        assertEquals("NM_131820",dbLink.getAccessionNumber());
        assertEquals(ForeignDBDataType.DataType.RNA,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.REFSEQ,dbLink.getReferenceDatabase().getForeignDB().getDbName());
        dbLink = iter.next();
        assertEquals(ForeignDBDataType.DataType.GENOMIC,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.GENBANK,dbLink.getReferenceDatabase().getForeignDB().getDbName());
        assertEquals("BX322631",dbLink.getAccessionNumber());
        dbLink = iter.next();
        assertEquals(ForeignDBDataType.DataType.POLYPEPTIDE,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.UNIPROTKB,dbLink.getReferenceDatabase().getForeignDB().getDbName());
        dbLink = iter.next();
        assertEquals(ForeignDBDataType.DataType.SEQUENCE_CLUSTERS,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.UNIGENE,dbLink.getReferenceDatabase().getForeignDB().getDbName());
    }

    @Test
    public void getSequenceInfoFull(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        SequencePageInfoBean sequenceInfo = MarkerService.getSequenceInfoFull(m);
        // should be 6 here! other 2 are related . .
        assertEquals(6,sequenceInfo.getDbLinks().size());
        Iterator<DBLink> iter = sequenceInfo.getDbLinks().iterator() ;
        DBLink dbLink ;
        dbLink = iter.next();
        assertEquals(ForeignDBDataType.DataType.RNA,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.REFSEQ,dbLink.getReferenceDatabase().getForeignDB().getDbName());
        dbLink = iter.next();
        assertEquals(ForeignDBDataType.DataType.RNA,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.GENBANK,dbLink.getReferenceDatabase().getForeignDB().getDbName());
        dbLink = iter.next();
        assertEquals(ForeignDBDataType.DataType.RNA,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.GENBANK,dbLink.getReferenceDatabase().getForeignDB().getDbName());

        dbLink = iter.next();
        assertEquals(ForeignDBDataType.DataType.POLYPEPTIDE,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.UNIPROTKB,dbLink.getReferenceDatabase().getForeignDB().getDbName());

        dbLink = iter.next();
        assertEquals(ForeignDBDataType.DataType.POLYPEPTIDE,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.REFSEQ,dbLink.getReferenceDatabase().getForeignDB().getDbName());

//        dbLink = iter.next();
//        assertEquals(ForeignDBDataType.DataType.POLYPEPTIDE,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
//        assertEquals(ForeignDB.AvailableName.GENPEPT,dbLink.getReferenceDatabase().getForeignDB().getDbName());

        dbLink = iter.next();
        assertEquals(ForeignDBDataType.DataType.SEQUENCE_CLUSTERS,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.UNIGENE,dbLink.getReferenceDatabase().getForeignDB().getDbName());

        MarkerDBLink markerDBLink;

        Iterator<MarkerDBLink> firstIter = sequenceInfo.getFirstRelatedMarkerDBLink().iterator();
        assertEquals(1,sequenceInfo.getFirstRelatedMarkerDBLink().size());
        markerDBLink = firstIter.next();

        assertEquals(ForeignDBDataType.DataType.GENOMIC,markerDBLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.GENBANK,markerDBLink.getReferenceDatabase().getForeignDB().getDbName());
        assertEquals("eu736",markerDBLink.getMarker().getAbbreviation());


        Iterator<MarkerDBLink> secondIter = sequenceInfo.getSecondRelatedMarkerDBLink().iterator();
        assertEquals(1,sequenceInfo.getSecondRelatedMarkerDBLink().size());
        markerDBLink = secondIter.next();

        assertEquals(ForeignDBDataType.DataType.GENOMIC,markerDBLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.GENBANK,markerDBLink.getReferenceDatabase().getForeignDB().getDbName());
        assertEquals("DKEY-173L11",markerDBLink.getMarker().getAbbreviation());
    }

    @Test
    public void getSnpMappedMarkers(){
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-SNP-060626-1008");
        MappedMarkerBean mappedMarkerBean = MarkerService.getSnpMappedMarkers(marker);
        assertNotNull(mappedMarkerBean);
        List<String> unmappedMarkers = mappedMarkerBean.getUnMappedMarkers();
        assertEquals(1,unmappedMarkers.size());
        assertEquals("25",unmappedMarkers.iterator().next());
    }
}
