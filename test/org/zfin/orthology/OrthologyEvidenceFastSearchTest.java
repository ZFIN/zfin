package org.zfin.orthology;

import org.junit.Test;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OrthologyEvidenceFastSearchTest {


    /**
     * The two objects differ only in the organism and thus are considered equal.
     */
    @Test
    public void twoRecordsAreEqual() {
        OrthologyEvidenceFastSearch fastSearchOne = new OrthologyEvidenceFastSearch();
        fastSearchOne.setCode(OrthoEvidence.Code.AA);
        Marker marker = new Marker();
        marker.setZdbID("ZDB-GENE-990415-8");
        fastSearchOne.setMarker(marker);
        fastSearchOne.setOrganism(Species.HUMAN.toString());
        Publication pub = new Publication();
        pub.setZdbID("ZDB-PUB-030129-1");
        fastSearchOne.setPublication(pub);

        OrthologyEvidenceFastSearch fastSearchTwo = new OrthologyEvidenceFastSearch();
        fastSearchTwo.setCode(OrthoEvidence.Code.AA);
        fastSearchTwo.setMarker(marker);
        fastSearchTwo.setPublication(pub);
        fastSearchTwo.setOrganism(Species.MOUSE.toString());

        assertTrue("The objects are considerd equal", fastSearchOne.equals(fastSearchTwo));

    }

    /**
     * The two objects have different evidence code and thus should be different.
     */
    @Test
    public void twoRecordsAreNotEqual() {
        OrthologyEvidenceFastSearch fastSearchOne = new OrthologyEvidenceFastSearch();
        fastSearchOne.setCode(OrthoEvidence.Code.AA);
        Marker marker = new Marker();
        marker.setZdbID("ZDB-GENE-990415-8");
        fastSearchOne.setMarker(marker);
        fastSearchOne.setOrganism(Species.HUMAN.toString());
        Publication pub = new Publication();
        pub.setZdbID("ZDB-PUB-030129-1");
        fastSearchOne.setPublication(pub);

        OrthologyEvidenceFastSearch fastSearchTwo = new OrthologyEvidenceFastSearch();
        fastSearchTwo.setCode(OrthoEvidence.Code.CL);
        fastSearchTwo.setMarker(marker);
        fastSearchTwo.setPublication(pub);
        fastSearchTwo.setOrganism(Species.HUMAN.toString());

        assertTrue("The objects are considerd not equal", !fastSearchOne.equals(fastSearchTwo));

    }

    /**
     * Two orthologies for mouse and human with the same evidence code and pub
     * while a third one is with a different evidence code.
     */
    @Test
    public void twoOrthologiesSimilar() {
        Marker marker = new Marker();
        marker.setZdbID("ZDB-GENE-990415-8");
        Publication pub = new Publication();
        pub.setZdbID("ZDB-PUB-030129-1");

        Orthologue orthologyOne = new Orthologue();
        orthologyOne.setOrganism(Species.HUMAN);
        orthologyOne.setGene(marker);
        OrthoEvidence evidenceOne = new OrthoEvidence();
        evidenceOne.setOrthologueEvidenceCode(OrthoEvidence.Code.AA);
        evidenceOne.setPublication(pub);
        Set<OrthoEvidence> evidences = new HashSet<OrthoEvidence>();
        evidences.add(evidenceOne);
        orthologyOne.setEvidence(evidences);

        Orthologue orthologyTwo = new Orthologue();
        orthologyTwo.setOrganism(Species.MOUSE);
        orthologyTwo.setGene(marker);
        OrthoEvidence evidenceThree = new OrthoEvidence();
        evidenceThree.setOrthologueEvidenceCode(OrthoEvidence.Code.AA);
        evidenceThree.setPublication(pub);
        Set<OrthoEvidence> evidencesTwo = new HashSet<OrthoEvidence>();
        evidencesTwo.add(evidenceThree);
        orthologyTwo.setEvidence(evidencesTwo);

        Set<Orthologue> orthologs = new HashSet<Orthologue>();
        orthologs.add(orthologyOne);
        orthologs.add(orthologyTwo);

        Set<OrthologyEvidenceFastSearch> fastSearches = OrthologyEvidenceService.getOrthoEvidenceFastSearches(orthologs);

        assertEquals("There should be three objects", 1, fastSearches.size());
        OrthologyEvidenceFastSearch search = fastSearches.iterator().next();
        assertTrue("Organis List", "Mouse:Human".equals(search.getOrganism()) || "Human:Mouse".equals(search.getOrganism()));

    }

    /**
     * Two orthologies for mouse and human with the same evidence code and pub
     * while a third one is with a different evidence code.
     */
    @Test
    public void threeOrthologiesTwoSimilar() {
        Marker marker = new Marker();
        marker.setZdbID("ZDB-GENE-990415-8");
        Publication pub = new Publication();
        pub.setZdbID("ZDB-PUB-030129-1");
        Publication pubTwo = new Publication();
        pubTwo.setZdbID("ZDB-PUB-030129-11");

        Orthologue orthologyOne = new Orthologue();
        orthologyOne.setOrganism(Species.HUMAN);
        orthologyOne.setGene(marker);
        OrthoEvidence evidenceOne = new OrthoEvidence();
        evidenceOne.setOrthologueEvidenceCode(OrthoEvidence.Code.AA);
        evidenceOne.setPublication(pub);
        OrthoEvidence evidenceTwo = new OrthoEvidence();
        evidenceTwo.setOrthologueEvidenceCode(OrthoEvidence.Code.CL);
        evidenceTwo.setPublication(pub);
        Set<OrthoEvidence> evidences = new HashSet<OrthoEvidence>();
        evidences.add(evidenceOne);
        evidences.add(evidenceTwo);
        orthologyOne.setEvidence(evidences);

        Orthologue orthologyTwo = new Orthologue();
        orthologyTwo.setOrganism(Species.MOUSE);
        orthologyTwo.setGene(marker);
        OrthoEvidence evidenceThree = new OrthoEvidence();
        evidenceThree.setOrthologueEvidenceCode(OrthoEvidence.Code.AA);
        evidenceThree.setPublication(pub);
        OrthoEvidence evidenceFour = new OrthoEvidence();
        evidenceFour.setOrthologueEvidenceCode(OrthoEvidence.Code.AA);
        evidenceFour.setPublication(pubTwo);
        Set<OrthoEvidence> evidencesTwo = new HashSet<OrthoEvidence>();
        evidencesTwo.add(evidenceThree);
        evidencesTwo.add(evidenceFour);
        orthologyTwo.setEvidence(evidencesTwo);

        Set<Orthologue> orthologs = new HashSet<Orthologue>();
        orthologs.add(orthologyOne);
        orthologs.add(orthologyTwo);

        Set<OrthologyEvidenceFastSearch> fastSearches = OrthologyEvidenceService.getOrthoEvidenceFastSearches(orthologs);

        assertEquals("There should be three objects", 3, fastSearches.size());

        OrthologyEvidenceFastSearch itemOne = new OrthologyEvidenceFastSearch();
        itemOne.setOrganism(Species.MOUSE.toString());
        itemOne.setCode(OrthoEvidence.Code.AA);
        itemOne.setMarker(marker);
        itemOne.setPublication(pubTwo);
        for( OrthologyEvidenceFastSearch search : fastSearches){
            if(search.equals(itemOne)){
                assertEquals("List contains Mouse record", "Mouse", search.getOrganism());
                assertEquals("List contains Mouse record", OrthoEvidence.Code.AA, search.getCode());
            }
        }

        OrthologyEvidenceFastSearch itemTwo = new OrthologyEvidenceFastSearch();
        itemTwo.setOrganism(Species.MOUSE.toString() + ":" + Species.HUMAN.toString());
        itemTwo.setCode(OrthoEvidence.Code.AA);
        itemTwo.setMarker(marker);
        itemTwo.setPublication(pub);
        for( OrthologyEvidenceFastSearch search : fastSearches){
            if(search.equals(itemTwo)){
                assertTrue("List contains Mouse record", ("Human:Mouse".equals(search.getOrganism()) ||
                                                          "Mouse:Human".equals(search.getOrganism())));
                assertEquals("List contains Mouse record", OrthoEvidence.Code.AA, search.getCode());
            }
        }

    }

}
