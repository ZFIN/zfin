package org.zfin.marker;

import org.junit.Test;
import org.zfin.antibody.Antibody;
import org.zfin.expression.ExpressionAssay;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.publication.Publication;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.ReferenceDatabase;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 */
public class MergeMarkerUnitTest {

    // 2. need to add an alias to this markerToMergeInto
    // A - no overlap, create new alias from markertodelete and move all aliases over, add marker history for new alias
    // B - overlap in aliases only, create new alias from markertodelete and and combine attributions of matching alias, use old alias for marker history
    // C - markertomergeinto already has alias of markertodelete name, do not create new alias (no attribution to add), but move rest over
    // D - markertodelete has alias that is name of markertomergeinto, I think that this is case A
    @Test
    public void mergeAliasesNoOverlap(){
        // we have one alias, here
        Marker markerToDelete = new Marker();
        markerToDelete.setAbbreviation("dogz");

        MarkerAlias alias1 = new MarkerAlias();
        alias1.setMarker(markerToDelete);
        alias1.setAlias("mammal");
        PublicationAttribution publicationAttribution = new PublicationAttribution();
        Publication pub1 = new Publication() ;
        pub1.setZdbID("A");
        publicationAttribution.setPublication(pub1);
        alias1.addPublication(publicationAttribution);
        Set<MarkerAlias> markerToDeleteAliases = new HashSet<MarkerAlias>() ;
        markerToDeleteAliases.add(alias1) ;
        markerToDelete.setAliases(markerToDeleteAliases);


        // also has mammal alias, but with different pub
        Marker markerToMergeInto = new Marker() ;
        markerToMergeInto.setAbbreviation("catz");
        Set<MarkerAlias> markerToMergeIntoAliases = new HashSet<MarkerAlias>() ;

        MarkerAlias alias2 = new MarkerAlias();
        alias2.setMarker(markerToMergeInto);
        alias2.setAlias("mammal");
        PublicationAttribution publicationAttribution2 = new PublicationAttribution();
        Publication pub2 = new Publication() ;
        pub2.setZdbID("B");
        publicationAttribution2.setPublication(pub2);
        alias2.addPublication(publicationAttribution2);
        markerToMergeIntoAliases.add(alias2) ;

        MarkerAlias alias3 = new MarkerAlias();
        alias3.setMarker(markerToMergeInto);
        alias3.setAlias("dogz");
        markerToMergeIntoAliases.add(alias3) ;

        markerToMergeInto.setAliases(markerToMergeIntoAliases);


        // thing to test
        MarkerAlias markerAlias = MergeService.mergeAliases(markerToDelete,markerToMergeInto);


        assertNotNull(markerAlias) ;
        assertEquals("dogz",markerAlias.getAlias());
        Set<MarkerAlias> markerAliasSet = markerToMergeInto.getAliases() ;
        assertEquals(2,markerAliasSet.size()) ;
        for(MarkerAlias aMarkerAlias : markerAliasSet){
            if(aMarkerAlias.getAlias().equals("dogz")){
                assertEquals(0,aMarkerAlias.getPublications().size());
            }
            else
            if(aMarkerAlias.getAlias().equals("catz")){
                assertEquals(1,aMarkerAlias.getPublications().size());
            }
            else
            if(aMarkerAlias.getAlias().equals("mammal")){
                assertEquals(2,aMarkerAlias.getPublications().size());
            }
            else{
                fail("Alias undefined: "+ markerAlias);
            }
        }
    }

    /**
     * Here we are testing alternate keys for when we merge expression experiment between to antibodies.
     */
    @Test
    public void mergeExpressionExperiments(){
        Antibody a1 = new Antibody();
        a1.setZdbID("antibodyZdbID");
        Publication publication = new Publication();
        publication.setZdbID("pubZdbID");
        GenotypeExperiment genotypeExperiment = new GenotypeExperiment();
        genotypeExperiment.setZdbID("genotypeExperimentZdbID");
        ExpressionAssay expressionAssay = new ExpressionAssay() ;
        expressionAssay.setName("dogz");
        Clone probe = new Clone() ;
        probe.setZdbID("probeZdbID");
        Marker gene = new Marker() ;
        gene.setZdbID("geneZdbID");
        MarkerDBLink markerDBLink = new MarkerDBLink() ;
        markerDBLink.setMarker(gene);
        markerDBLink.setAccessionNumber("geneAccession");
        ReferenceDatabase referenceDatabase = new ReferenceDatabase();
        referenceDatabase.setZdbID("referenceDatabaseZdbID");
        markerDBLink.setReferenceDatabase(referenceDatabase);


        ExpressionExperiment e1 = new ExpressionExperiment();
        e1.setPublication(publication);
        e1.setGenotypeExperiment(genotypeExperiment);
        e1.setAssay(expressionAssay);
        e1.setProbe(probe);
        e1.setGene(gene);
        e1.setMarkerDBLink(markerDBLink);
        e1.setAntibody(a1);

        ExpressionExperiment e2 = new ExpressionExperiment();
        e2.setPublication(publication);
        e2.setGenotypeExperiment(genotypeExperiment);
        e2.setAssay(expressionAssay);
        e2.setProbe(probe);
        e2.setGene(gene);
        e2.setMarkerDBLink(markerDBLink);
        e2.setAntibody(a1);

        Set<ExpressionExperiment> antibodyLabelings = new HashSet<ExpressionExperiment>() ;
        antibodyLabelings.add(e1) ;
        a1.setAntibodyLabelings(antibodyLabelings);

        assertNotNull(a1.getMatchingAntibodyLabeling(e2));
        e1.setProbe(null);
        assertNull(a1.getMatchingAntibodyLabeling(e2));
        e2.setProbe(null);
        assertNotNull(a1.getMatchingAntibodyLabeling(e2));
        e1.setProbe(probe);
        Clone probe2 = new Clone();
        probe2.setZdbID("probe2ZdbID");
        e2.setProbe(probe2);
        assertNull(a1.getMatchingAntibodyLabeling(e2));
    }

}
