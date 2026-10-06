package org.zfin.datatransfer.go.service;

import org.junit.Test;
import org.zfin.datatransfer.go.GafEntry;
import org.zfin.datatransfer.go.GafJobEntry;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/** The removal-safety guard's attribution logic, without a database. */
public class RemovalAttributionUnitTest {

    private static GafEntry rejected(String entityId, String goTermId) {
        GafEntry entry = new GafEntry();
        entry.setEntryId(entityId);
        entry.setGoTermId(goTermId);
        return entry;
    }

    private static GafJobEntry removal(String markerZdbID, String goTermID, String owningOrg) {
        GafJobEntry entry = new GafJobEntry("ZDB-MRKRGOEV-TEST-1");
        entry.setOwningOrganization(owningOrg);
        entry.setMarkerZdbID(markerZdbID);
        entry.setGoTermID(goTermID);
        return entry;
    }

    @Test
    public void gpadEntityIdPrefixIsStripped() {
        Set<String> keys = GafService.attributionKeys(List.of(
            rejected("ZFIN:ZDB-GENE-000112-47", "GO:0090575")));
        assertEquals(Set.of(GafService.attributionKey("ZDB-GENE-000112-47", "GO:0090575")), keys);
    }

    @Test
    public void unresolvableSubjectsAreNotAttributed() {
        Set<String> keys = GafService.attributionKeys(List.of(
            rejected("UniProtKB:A0A0G2KC95", "GO:0006357"),
            rejected(null, "GO:0006357"),
            rejected("ZFIN:ZDB-GENE-1", null)));
        assertTrue("nothing should be attributable from these", keys.isEmpty());
    }

    @Test
    public void aRemovalMatchingARejectedRowIsWithheld() {
        Set<String> keys = GafService.attributionKeys(List.of(
            rejected("ZFIN:ZDB-GENE-1", "GO:0000001")));
        List<GafJobEntry> hit = GafService.removalsAttributableTo(
            List.of(removal("ZDB-GENE-1", "GO:0000001", "GOA")), "GOA", keys);
        assertEquals(1, hit.size());
    }

    /** The file genuinely dropped this row; withholding it would block every legitimate cutover. */
    @Test
    public void aRemovalWithNoMatchingRejectionIsApplied() {
        Set<String> keys = GafService.attributionKeys(List.of(
            rejected("ZFIN:ZDB-GENE-1", "GO:0000001")));
        List<GafJobEntry> hit = GafService.removalsAttributableTo(
            List.of(removal("ZDB-GENE-2", "GO:0000002", "GOA")), "GOA", keys);
        assertTrue(hit.isEmpty());
    }

    @Test
    public void removalsOfAnotherOrganizationAreNotTouched() {
        Set<String> keys = GafService.attributionKeys(List.of(
            rejected("ZFIN:ZDB-GENE-1", "GO:0000001")));
        List<GafJobEntry> hit = GafService.removalsAttributableTo(
            List.of(removal("ZDB-GENE-1", "GO:0000001", "Noctua")), "GOA", keys);
        assertTrue("a GOA pass must not withhold Noctua's removals", hit.isEmpty());
    }

    /**
     * organizationCreatedBy carries assigned_by values, which never equal the organization names
     * the load prunes. Matching must not depend on that field.
     */
    @Test
    public void matchingDoesNotDependOnOrganizationCreatedBy() {
        Set<String> keys = GafService.attributionKeys(List.of(
            rejected("ZFIN:ZDB-GENE-1", "GO:0000001")));
        GafJobEntry entry = removal("ZDB-GENE-1", "GO:0000001", "GOA");
        entry.setOrganizationCreatedBy("InterPro");
        assertEquals(1, GafService.removalsAttributableTo(List.of(entry), "GOA", keys).size());
    }

    /** The ND-replacement path records removals with no owning organization. */
    @Test
    public void untaggedRemovalsAreNeverWithheld() {
        Set<String> keys = GafService.attributionKeys(List.of(
            rejected("ZFIN:ZDB-GENE-1", "GO:0000001")));
        assertTrue(GafService.removalsAttributableTo(
            List.of(removal("ZDB-GENE-1", "GO:0000001", null)), "GOA", keys).isEmpty());
    }

    @Test
    public void noRejectionsMeansNothingIsEverWithheld() {
        assertTrue(GafService.removalsAttributableTo(
            List.of(removal("ZDB-GENE-1", "GO:0000001", "GOA")), "GOA", Set.of()).isEmpty());
    }
}
