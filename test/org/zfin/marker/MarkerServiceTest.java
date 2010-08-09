package org.zfin.marker;

import org.junit.Test;
import org.zfin.marker.presentation.MergeMarkerValidator;
import org.zfin.marker.presentation.RelatedMarker;
import org.zfin.marker.presentation.RelatedMarkerDisplay;

import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.Assert.*;

/**
 * Tests for org.zfin.marker.MarkerService
 */
public class MarkerServiceTest {

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

}
