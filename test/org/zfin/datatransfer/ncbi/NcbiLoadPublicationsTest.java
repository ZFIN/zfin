package org.zfin.datatransfer.ncbi;

import org.junit.Test;
import org.zfin.datatransfer.ncbi.NcbiLoadPublications.Row;
import org.zfin.datatransfer.ncbi.port.PortSqlHelper;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class NcbiLoadPublicationsTest {

    private final NcbiLoadPublications publications = new NcbiLoadPublications(List.of(
            new Row("ZDB-PUB-1", 1, true),
            new Row("ZDB-PUB-2", 2, true),
            new Row("ZDB-PUB-3", 3, false),
            new Row("ZDB-PUB-4", null, false)));

    @Test
    public void createdByLoadAreThoseWithAPrecedenceInOrder() {
        assertEquals(List.of("ZDB-PUB-1", "ZDB-PUB-2", "ZDB-PUB-3"), publications.createdByLoad());
    }

    @Test
    public void loadManagedAreThoseTheLoadReplaces() {
        assertEquals(Set.of("ZDB-PUB-1", "ZDB-PUB-2"), publications.loadManaged());
    }

    @Test
    public void requireAcceptsPublicationsTheLoadCreates() {
        publications.requireCreatedByLoad("ZDB-PUB-1", "ZDB-PUB-3");
    }

    @Test
    public void requireNamesEveryPublicationThatIsMissingOrHasNoPrecedence() {
        try {
            publications.requireCreatedByLoad("ZDB-PUB-1", "ZDB-PUB-4", "ZDB-PUB-9");
            fail("expected an IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("ZDB-PUB-4, ZDB-PUB-9"));
            assertFalse(e.getMessage(), e.getMessage().contains("ZDB-PUB-1,"));
        }
    }

    @Test
    public void sqlHelperListsThePublicationsItIsGiven() {
        String sql = PortSqlHelper.getSqlForGeneAndRnagDbLinksSupportedByLoadPubsFromFdbContId(
                "ZDB-FDBCONT-1", publications.createdByLoad());
        assertTrue(sql, sql.endsWith("recattrib_source_zdb_id in ('ZDB-PUB-1','ZDB-PUB-2','ZDB-PUB-3'))"));
    }

    @Test
    public void sqlHelperEscapesQuotes() {
        String sql = PortSqlHelper.getSqlForGeneAndRnagDbLinksSupportedByLoadPubsFromFdbContId(
                "ZDB-FDBCONT-1", List.of("a'b"));
        assertTrue(sql, sql.contains("('a''b')"));
    }
}
