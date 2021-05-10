package org.zfin.orthology;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.TestConfiguration;
import org.zfin.criteria.ZfinCriteria;
import org.zfin.marker.Marker;
import org.zfin.orthology.repository.HibernateOrthologyRepository;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.FilterType;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.zfin.repository.RepositoryFactory.*;

/**
 * Test for utility methods in the repository class.
 */
public class OrthologyRepositoryTest extends AbstractDatabaseTest {

    @Before
    public void setUp() {
        TestConfiguration.configure();
    }

    @Test
    public void getOrthologsForGene() {
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        List<Ortholog> orthologs = getOrthologyRepository().getOrthologs(m);
        assertThat(orthologs, hasSize(2));
        for (Ortholog ortholog : orthologs) {
            assertThat(ortholog.getEvidenceSet(), not(empty()));
        }
    }

    @Test
    public void getEvidenceCodesForMarker() {

        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        List<String> codes = getOrthologyRepository().getEvidenceCodes(m);
        assertEquals(2, codes.size());
        assertEquals("AA", codes.get(0));
        assertEquals("NT", codes.get(1));

        // fbmpr1aa
        m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-000502-1");
        // Correa
        Publication publication = getPublicationRepository().getPublication("ZDB-PUB-050803-7");

        codes = getOrthologyRepository().getEvidenceCodes(m, publication);
        assertEquals(1, codes.size());
        assertEquals("CL", codes.get(0));

    }

    @Test
    public void getOrthologsByGene() {
        String zdbID = "ZDB-GENE-991207-24";
        Marker gene = getMarkerRepository().getMarkerByID(zdbID);
        List<Ortholog> orthologList = getOrthologyRepository().getOrthologs(gene);
        assertNotNull(orthologList);
    }

    @Test
    public void getOrthologsByNcbiGene() {
        String zdbID = "1495";
        NcbiOtherSpeciesGene ncbiGene = getOrthologyRepository().getNcbiGene(zdbID);
        assertNotNull(ncbiGene);
    }

    @Test
    public void getOrthogEvdidenceCode() {
        String code = "AA";
        EvidenceCode evidence = getOrthologyRepository().getEvidenceCode(code);
        assertNotNull(evidence);
    }

    @Test
    public void getOrthoByID() {
        String ID = "ZDB-ORTHO-151008-1";
        Ortholog ortholog = getOrthologyRepository().getOrtholog(ID);
        assertNotNull(ortholog);
    }

    @Test
    public void getOrthoByGeneId() {
        String geneID = "ZDB-GENE-991207-24";
        String ncbiID = "12385";
        Marker gene = getMarkerRepository().getMarkerByID(geneID);
        NcbiOtherSpeciesGene otherSpeciesGene = getOrthologyRepository().getNcbiGene(ncbiID);
        Ortholog ortholog = getOrthologyRepository().getOrthologByGeneAndNcbi(gene, otherSpeciesGene);
        assertNotNull(ortholog);
    }

}