package org.zfin.datatransfer.go.service;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.datatransfer.go.FpInferenceGafParser;
import org.zfin.datatransfer.go.GafEntry;
import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.datatransfer.go.GoaGafParser;
import org.zfin.infrastructure.ActiveData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * Tests GafService methods
 */
public class GafServiceTest extends AbstractDatabaseTest {

    private GafService gafService = new GafService(GafOrganization.OrganizationEnum.GOA);
    private final String GOA_DIRECTORY = "test/gaf/goa/";
    private FpInferenceGafParser gafParser = new GoaGafParser();

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

}
