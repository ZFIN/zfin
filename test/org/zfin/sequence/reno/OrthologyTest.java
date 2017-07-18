package org.zfin.sequence.reno;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.orthology.EvidenceCode;
import org.zfin.orthology.NcbiOtherSpeciesGene;
import org.zfin.orthology.Ortholog;
import org.zfin.orthology.OrthologEvidence;
import org.zfin.orthology.repository.OrthologyRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.zfin.repository.RepositoryFactory.getOrthologyRepository;

public class OrthologyTest extends AbstractDatabaseTest {

    private static MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private static PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();
    private static OrthologyRepository orthoRepository = getOrthologyRepository();

    @Test
    // Test that there are Redundancy runs in the database
    // Test the test redundancy run
    public void insertSingleOrthology() {
        Marker pax2a = markerRepository.getMarkerByAbbreviation("fsb");
        Publication publication = pubRepository.getPublication("ZDB-PUB-030905-1");

        Ortholog ortho = new Ortholog();
        ortho.setZebrafishGene(pax2a);
        Set<OrthologEvidence> evidences = new HashSet<>();
        OrthologEvidence evidence = new OrthologEvidence();
        evidence.setEvidenceCode(getEvidenceCode("AA"));
        evidence.setPublication(publication);
        evidence.setOrtholog(ortho);
        evidences.add(evidence);
        ortho.setEvidenceSet(evidences);

        NcbiOtherSpeciesGene ncbiOtherSpeciesGene = getOrthologyRepository().getNcbiGene("2253");
        ortho.setNcbiOtherSpeciesGene(ncbiOtherSpeciesGene);

        orthoRepository.saveOrthology(ortho, publication);

        String zdbID = ortho.getZdbID();
        assertTrue("ID created for Ortholog", zdbID != null && zdbID.startsWith("ZDB-ORTHO"));

        List<String> evids = getOrthologyRepository().getEvidenceCodes(pax2a);
        assertTrue("One evidence code created", evids != null);
        assertEquals("One evidence code created", evids.size(), 1);
        assertEquals("One evidence code created", evids.get(0), "AA");

    }

    private EvidenceCode getEvidenceCode(String aa) {
        EvidenceCode evidenceCode = new EvidenceCode();
        evidenceCode.setCode(aa);
        return evidenceCode;
    }

}
