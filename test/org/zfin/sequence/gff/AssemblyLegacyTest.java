package org.zfin.sequence.gff;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.HibernateUtil;

import static org.junit.Assert.assertEquals;

public class AssemblyLegacyTest extends AbstractDatabaseTest {

    private long count(String sql) {
        return ((Number) HibernateUtil.currentSession().createNativeQuery(sql, Object.class).getSingleResult()).longValue();
    }

    @Test
    public void grcv10IsListedAsALegacyAssemblyAfterTheOthers() {
        assertEquals(1, count("select count(*) from assembly where a_name = 'GRCv10' and not a_is_current "
                + "and a_gcf_identifier is null and a_order > (select max(a_order) from assembly where a_name <> 'GRCv10')"));
    }

    @Test
    public void everyAssemblyNamedInTheLocationTableIsListedInTheAssemblyTable() {
        assertEquals("sequence_feature_chromosome_location_generated names an assembly that the assembly table does not list",
                0, count("select count(distinct sfclg_assembly) from sequence_feature_chromosome_location_generated "
                        + "where sfclg_assembly not in (select a_name from assembly)"));
    }

    @Test
    public void theCurrentAssembliesAreGrcz12tuAndGrcz13abOnly() {
        assertEquals(2, count("select count(*) from assembly where a_is_current and a_name in ('GRCz12tu', 'GRCz13ab')"));
        assertEquals(0, count("select count(*) from assembly where a_is_current and a_name not in ('GRCz12tu', 'GRCz13ab')"));
    }
}
