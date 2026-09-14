package org.zfin.datatransfer.go.service;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.datatransfer.go.FpInferenceGafParser;
import org.zfin.datatransfer.go.GafEntry;
import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.datatransfer.go.GafValidationError;
import org.zfin.datatransfer.go.GoaGafParser;
import org.zfin.publication.presentation.PublicationService;
import org.zfin.infrastructure.ActiveData;
import org.zfin.mutant.MarkerGoTermAnnotationExtnGroup;
import org.zfin.mutant.MarkerGoTermEvidence;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

/**
 * Tests GafService methods
 */
public class GafServiceTest extends AbstractDatabaseTest {

    private GafService gafService = new GafService(GafOrganization.OrganizationEnum.GOA);
    private final String GOA_DIRECTORY = "test/gaf/goa/";
    private FpInferenceGafParser gafParser = new GoaGafParser();

    /**
     * ZFIN-10358: a "DOI:" citation resolves to the ZFIN publication carrying that DOI.
     */
    @Test
    public void getPublicationByPlainDoi() throws Exception {
        GafEntry gafEntry = new GafEntry();
        gafEntry.setPubmedId("DOI:10.1093/icb/40.2.246");
        assertThat(gafService.getPublication(gafEntry).getZdbID(), is("ZDB-PUB-041015-1"));
    }

    /**
     * ZFIN-10358 follow-up: a few hundred pub_doi values were entered as resolver URLs rather
     * than bare DOIs. ZDB-PUB-040611-2 is stored as "doi.org/10.2331/fishsci.68.sup1_765" and
     * was the reason some DANRE-mod GPAD rows still failed to load after the first fix.
     */
    @Test
    public void getPublicationByDoiStoredAsResolverUrl() throws Exception {
        GafEntry gafEntry = new GafEntry();
        gafEntry.setPubmedId("DOI:10.2331/fishsci.68.sup1_765");
        assertThat(gafService.getPublication(gafEntry).getZdbID(), is("ZDB-PUB-040611-2"));
    }

    /**
     * DOIs are case-insensitive by the DOI Handbook, and ~3000 pub_doi values contain uppercase.
     */
    @Test
    public void getPublicationByDoiIgnoresCase() throws Exception {
        GafEntry gafEntry = new GafEntry();
        gafEntry.setPubmedId("doi:10.1023/b:fish.0000030466.23085.9");
        assertThat(gafService.getPublication(gafEntry).getZdbID(), is("ZDB-PUB-060309-3"));
    }

    @Test
    public void getPublicationByUnknownDoiIsRejected() {
        GafEntry gafEntry = new GafEntry();
        gafEntry.setPubmedId("DOI:10.9999/does-not-exist-in-zfin");
        GafValidationError error = assertThrows(GafValidationError.class, () -> gafService.getPublication(gafEntry));
        assertThat(error.getMessage().contains("No pub found for DOI"), is(true));
    }

    @Test
    public void normalizeDoiStripsResolverPrefixes() {
        assertThat(PublicationService.normalizeDoi("10.1093/icb/40.2.246"), is("10.1093/icb/40.2.246"));
        assertThat(PublicationService.normalizeDoi("doi.org/10.1093/icb/40.2.246"), is("10.1093/icb/40.2.246"));
        assertThat(PublicationService.normalizeDoi("https://doi.org/10.1093/icb/40.2.246"), is("10.1093/icb/40.2.246"));
        assertThat(PublicationService.normalizeDoi("http://dx.doi.org/10.1093/icb/40.2.246"), is("10.1093/icb/40.2.246"));
        assertThat(PublicationService.normalizeDoi("DOI:10.1093/ICB/40.2.246"), is("10.1093/icb/40.2.246"));
        assertThat(PublicationService.normalizeDoi("  10.1093/icb/40.2.246  "), is("10.1093/icb/40.2.246"));
        assertNull(PublicationService.normalizeDoi(null));
        assertNull(PublicationService.normalizeDoi("   "));
    }

    @Test
    public void replaceAttribute() throws Exception {
        GafEntry gafEntry = new GafEntry();
        gafEntry.setEntryId("ZDB-GENE-000112-38");
        gafService.replaceAttributeOnGafEntry(gafEntry, "entryId", gafService.getReplacedDataMapFromEntities(ActiveData.Type.GENE, ActiveData.Type.MRPHLNO));
        assertThat(gafEntry.getEntryId(), is("ZDB-GENE-980526-115"));
    }

    @Test
    public void replaceMergedZDBIds() throws Exception {
        GafEntry gafEntry = new GafEntry();
        gafEntry.setEntryId("ZDB-GENE-000112-38");
        gafEntry.setInferences("InterPro:IPR026856|InterPro:IPR026944");
        GafEntry gafEntry2 = new GafEntry();
        gafEntry2.setEntryId("A0FJH7");
        gafEntry2.setInferences("ZFIN:ZDB-GENE-000523-1|ZFIN:ZDB-MRPHLNO-070906-6|ZFIN:ZDB-GENE-000607-37");
        List<GafEntry> gafEntryList = new ArrayList<GafEntry>(2);
        gafEntryList.add(gafEntry);
        gafEntryList.add(gafEntry2);
        gafService.replaceMergedZDBIds(gafEntryList);
        assertNotNull(gafEntryList);
        assertThat(gafEntryList.size(), is(2));
        assertThat(gafEntryList.get(0).getEntryId(), is("ZDB-GENE-980526-115"));
        assertThat(gafEntryList.get(0).getInferences(), is("InterPro:IPR026856|InterPro:IPR026944"));
        assertThat(gafEntryList.get(1).getEntryId(), is("A0FJH7"));
        assertThat(gafEntryList.get(1).getInferences(), is("ZFIN:ZDB-GENE-010501-7|ZFIN:ZDB-MRPHLNO-070906-6|ZFIN:ZDB-GENE-030131-5379"));
    }

    @Test
    public void GafParserWithMergedIds() throws Exception {
        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_noerror");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        int size = gafEntries.size();
        assertEquals(12, size);
        gafService.replaceMergedZDBIds(gafEntries);
        assertTrue(gafEntries.size() == size);
    }

    // ZFIN-10230: GPAD-Noctua emits relations as OBO IDs (e.g. RO:0002327), not names.
    // saveAnnoExtns must look these up by OBO ID, mirroring getRelQualifier's branching.
    @Test
    public void saveAnnoExtnsResolvesRelationByOboId() throws Exception {
        GafEntry entry = new GafEntry();
        entry.setEntryId("ZDB-GENE-990630-14");

        MarkerGoTermAnnotationExtnGroup group = new MarkerGoTermAnnotationExtnGroup();
        MarkerGoTermEvidence evidence = new MarkerGoTermEvidence();

        gafService.saveAnnoExtns("RO:0002327(ZFIN:foo)", group, entry, evidence);

        assertThat(group.getMgtAnnoExtns(), is(notNullValue()));
        assertEquals(1, group.getMgtAnnoExtns().size());
    }

}
