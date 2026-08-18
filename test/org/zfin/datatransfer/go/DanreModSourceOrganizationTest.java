package org.zfin.datatransfer.go;

import org.junit.Test;
import org.zfin.datatransfer.go.GafOrganization.OrganizationEnum;

import static org.junit.Assert.*;

/**
 * Ownership routing for the unified GO Central GPAD load. Getting this wrong is not a reporting
 * cosmetic: the resolved organization scopes add/update/REMOVE, so a mis-homed row lands in
 * another source's removal scope.
 */
public class DanreModSourceOrganizationTest {

    @Test
    public void zfinCurationGoesToNoctua() {
        assertEquals(OrganizationEnum.NOCTUA,
                DanreModSourceOrganization.resolve("ZFIN", "PMID:12345"));
    }

    /** assigned_by wins over the reference: ZFIN curation stays Noctua-owned whatever it cites. */
    @Test
    public void zfinBeatsThePhyloReference() {
        assertEquals(OrganizationEnum.NOCTUA,
                DanreModSourceOrganization.resolve("ZFIN", "GO_REF:0000033"));
    }

    @Test
    public void phyloReferenceGoesToPaintWhoeverAssertedIt() {
        assertEquals(OrganizationEnum.PAINT,
                DanreModSourceOrganization.resolve("GO_Central", "GO_REF:0000033"));
        // the legacy FP-Inference file asserted the same content as GOC
        assertEquals(OrganizationEnum.PAINT,
                DanreModSourceOrganization.resolve("GOC", "GO_REF:0000033"));
    }

    /**
     * The reason this is keyed on the reference rather than assigned_by: GO_Central is only
     * *nearly* a proxy for phylo. 24 of its 62,221 rows are on other references and must not be
     * dragged into PAINT's removal scope.
     */
    @Test
    public void goCentralOnANonPhyloReferenceStaysInGoa() {
        assertEquals(OrganizationEnum.GOA,
                DanreModSourceOrganization.resolve("GO_Central", "GO_REF:0000117"));
        assertEquals(OrganizationEnum.GOA,
                DanreModSourceOrganization.resolve("GO_Central", "PMID:28869969"));
    }

    @Test
    public void everythingElseGoesToGoa() {
        assertEquals(OrganizationEnum.GOA,
                DanreModSourceOrganization.resolve("UniProt", "GO_REF:0000104"));
        assertEquals(OrganizationEnum.GOA,
                DanreModSourceOrganization.resolve("InterPro", "GO_REF:0000002"));
        assertEquals(OrganizationEnum.GOA,
                DanreModSourceOrganization.resolve("IntAct", "PMID:12345"));
    }

    /** Never NPE and never silently pick an owner that could remove someone else's rows. */
    @Test
    public void nullsFallBackToTheDefault() {
        assertEquals(OrganizationEnum.GOA, DanreModSourceOrganization.resolve(null, null));
        assertEquals(OrganizationEnum.GOA, DanreModSourceOrganization.resolve(null, "PMID:1"));
        assertEquals(OrganizationEnum.GOA, DanreModSourceOrganization.resolve("UniProt", null));
        assertEquals(OrganizationEnum.PAINT,
                DanreModSourceOrganization.resolve(null, "GO_REF:0000033"));
    }

    @Test
    public void whitespaceIsTolerated() {
        assertEquals(OrganizationEnum.NOCTUA, DanreModSourceOrganization.resolve(" ZFIN ", null));
        assertEquals(OrganizationEnum.PAINT,
                DanreModSourceOrganization.resolve("GO_Central", " GO_REF:0000033 "));
    }

    /**
     * The per-source removal loop iterates this set. An org that can be resolved to but is
     * missing here would have rows written into it that are never pruned.
     */
    @Test
    public void everyResolvableOrgIsInTheRemovalLoop() {
        var orgs = DanreModSourceOrganization.allTargetOrganizations();
        assertTrue("Noctua missing from the removal loop", orgs.contains(OrganizationEnum.NOCTUA));
        assertTrue("PAINT missing from the removal loop", orgs.contains(OrganizationEnum.PAINT));
        assertTrue("GOA missing from the removal loop", orgs.contains(OrganizationEnum.GOA));
        assertEquals(3, orgs.size());
    }
}
