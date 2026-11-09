package org.zfin.datatransfer.persistence;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.HibernateUtil;

import static org.junit.Assert.*;

/** Runs inside the transaction {@link AbstractDatabaseTest} rolls back. */
public class LoadFileRegistryTest extends AbstractDatabaseTest {

    private static final String NEW_LOAD = "Registry Test Load";
    private static final String SOURCE = "https://example.org/registry-test.txt";

    private long count(String sql) {
        return ((Number) HibernateUtil.currentSession().createNativeQuery(sql, Object.class).getSingleResult()).longValue();
    }

    @Test
    public void registersANewLoadAndFileOnce() {
        LoadFile first = LoadFileRegistry.getOrCreate(HibernateUtil.currentSession(), NEW_LOAD, SOURCE, null);
        LoadFile second = LoadFileRegistry.getOrCreate(HibernateUtil.currentSession(), NEW_LOAD, SOURCE, null);

        assertSame(first, second);
        assertEquals(NEW_LOAD, first.getLoad().getName());
        assertEquals(1, count("select count(*) from load where ld_name = '" + NEW_LOAD + "'"));
        assertEquals(1, count("select count(*) from load_file where lf_source = '" + SOURCE + "'"));
    }

    @Test
    public void aNewFileIsDumpedWithItsRows() {
        LoadFile file = LoadFileRegistry.getOrCreate(HibernateUtil.currentSession(), NEW_LOAD, SOURCE, "public.some_table");
        assertFalse(file.isExcludeFromDump());
        assertEquals("public.some_table", file.getTableName());
    }

    @Test
    public void fillsInAMissingTableNameButNeverChangesOne() {
        LoadFile file = LoadFileRegistry.getOrCreate(HibernateUtil.currentSession(), NEW_LOAD, SOURCE, null);
        assertNull(file.getTableName());

        LoadFileRegistry.getOrCreate(HibernateUtil.currentSession(), NEW_LOAD, SOURCE, "public.first_table");
        assertEquals("public.first_table", file.getTableName());

        LoadFileRegistry.getOrCreate(HibernateUtil.currentSession(), NEW_LOAD, SOURCE, "public.other_table");
        assertEquals("public.first_table", file.getTableName());
    }

    @Test
    public void findsTheSeededGeneInfoFileAndKeepsItOutOfDumps() {
        LoadFile file = LoadFileRegistry.getOrCreate(HibernateUtil.currentSession(), "NCBI_gene_info",
                "https://ftp.ncbi.nlm.nih.gov/gene/DATA/GENE_INFO/Non-mammalian_vertebrates/Danio_rerio.gene_info.gz", null);
        assertEquals("external_resource.ncbi_danio_rerio_gene_info", file.getTableName());
        assertTrue(file.isExcludeFromDump());
    }

    @Test
    public void everyLogRowCanPointAtItsFile() {
        LoadFileLog log = new LoadFileLog();
        log.setLoadName(NEW_LOAD);
        log.setSource(SOURCE);
        log.setDate(new java.util.Date());
        log.setLoadFile(LoadFileRegistry.getOrCreate(HibernateUtil.currentSession(), NEW_LOAD, SOURCE, null));
        HibernateUtil.currentSession().persist(log);
        HibernateUtil.currentSession().flush();
        assertEquals(1, count("select count(*) from load_file_log l join load_file f on f.lf_id = l.lfl_lf_id where f.lf_source = '" + SOURCE + "'"));
    }
}
