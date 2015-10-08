package org.zfin.orthology;

import org.junit.Test;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;

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

    private EvidenceCode getEvidenceCode(String code) {
        EvidenceCode evidence = new EvidenceCode();
        evidence.setCode(code);
        return evidence;
    }

}
