package org.zfin.datatransfer.ncbi;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.HibernateUtil;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * Reads the NCBI Gene load's rows of load_publication, which the migration seeds.
 * Changes made inside the test are rolled back by {@link AbstractDatabaseTest}.
 */
public class NcbiLoadPublicationsDatabaseTest extends AbstractDatabaseTest {

    @Test
    public void theSeededPublicationsAreInPrecedenceOrder() {
        assertEquals(List.of(NCBIDirectPort.PUB_MAPPED_BASED_ON_RNA,
                        NCBIDirectPort.PUB_MAPPED_BASED_ON_NCBI_SUPPLEMENT,
                        NCBIDirectPort.PUB_MAPPED_BASED_ON_NCBI_SUPPLEMENT_GRCZ12,
                        NCBIDirectPort.PUB_MAPPED_BASED_ON_VEGA),
                NcbiLoadPublications.fromDatabase().createdByLoad());
    }

    @Test
    public void onlyTheRnaAndSupplementPublicationsAreLoadManaged() {
        assertEquals(Set.of(NCBIDirectPort.PUB_MAPPED_BASED_ON_RNA, NCBIDirectPort.PUB_MAPPED_BASED_ON_NCBI_SUPPLEMENT,
                        NCBIDirectPort.PUB_MAPPED_BASED_ON_NCBI_SUPPLEMENT_GRCZ12),
                NcbiLoadPublications.fromDatabase().loadManaged());
    }

    @Test
    public void theStrategyConstantsAreAllRecorded() {
        NcbiLoadPublications.fromDatabase().requireCreatedByLoad(NCBIDirectPort.PUB_MAPPED_BASED_ON_RNA,
                NCBIDirectPort.PUB_MAPPED_BASED_ON_VEGA, NCBIDirectPort.PUB_MAPPED_BASED_ON_NCBI_SUPPLEMENT,
                NCBIDirectPort.PUB_MAPPED_BASED_ON_NCBI_SUPPLEMENT_GRCZ12);
    }

    @Test
    public void aStrategyPublicationRemovedFromTheTableIsReported() {
        HibernateUtil.currentSession().createNativeQuery(
                        "delete from load_publication where lp_pub_zdb_id = :pub")
                .setParameter("pub", NCBIDirectPort.PUB_MAPPED_BASED_ON_VEGA).executeUpdate();
        assertThrows(IllegalStateException.class, () -> NcbiLoadPublications.fromDatabase()
                .requireCreatedByLoad(NCBIDirectPort.PUB_MAPPED_BASED_ON_VEGA));
    }
}
