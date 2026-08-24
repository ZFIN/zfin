package org.zfin.sequence.blast;

import org.junit.Test;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Covers the report.properties parsing that drives the staleness half of
 * Validate-Blast-Databases_d. Needs no database or blast server.
 */
public class BlastDatabaseStalenessPolicyTest {

    private static final String JOB = "Validate-Blast-Databases_d";

    private static Properties properties(String... keyValuePairs) {
        Properties properties = new Properties();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            properties.setProperty(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return properties;
    }

    @Test
    public void defaultAppliesWhenNothingIsConfigured() {
        BlastDatabaseStalenessPolicy policy = BlastDatabaseStalenessPolicy.fromProperties(properties(), JOB);

        assertEquals(BlastDatabaseStalenessPolicy.DEFAULT_MAX_AGE_DAYS, policy.defaultMaxAgeDays());
        assertEquals(BlastDatabaseStalenessPolicy.DEFAULT_MAX_AGE_DAYS, policy.maxAgeDays("refseq_zf_rna").getAsInt());
    }

    @Test
    public void overrideWinsOverDefault() {
        BlastDatabaseStalenessPolicy policy = BlastDatabaseStalenessPolicy.fromProperties(properties(
                JOB + ".maxAgeDays", "90",
                JOB + ".maxAgeDays.zfin_cdna_seq", "3"), JOB);

        assertEquals(3, policy.maxAgeDays("zfin_cdna_seq").getAsInt());
        assertEquals(90, policy.maxAgeDays("refseq_zf_rna").getAsInt());
    }

    @Test
    public void exemptDatabasesAreNotAgeChecked() {
        BlastDatabaseStalenessPolicy policy = BlastDatabaseStalenessPolicy.fromProperties(properties(
                JOB + ".staleExemptDatabases", " repbase_zf , vega_zfin ,, vega_transcript "), JOB);

        assertEquals(Set.of("repbase_zf", "vega_zfin", "vega_transcript"), policy.exempt());
        assertTrue(policy.maxAgeDays("repbase_zf").isEmpty());
        assertFalse(policy.maxAgeDays("refseq_zf_rna").isEmpty());
    }

    @Test
    public void exemptionBeatsAnOverride() {
        BlastDatabaseStalenessPolicy policy = BlastDatabaseStalenessPolicy.fromProperties(properties(
                JOB + ".maxAgeDays.vega_zfin", "7",
                JOB + ".staleExemptDatabases", "vega_zfin"), JOB);

        assertTrue(policy.maxAgeDays("vega_zfin").isEmpty());
    }

    @Test
    public void nonPositiveMaxAgeTurnsTheCheckOff() {
        BlastDatabaseStalenessPolicy policy = BlastDatabaseStalenessPolicy.fromProperties(properties(
                JOB + ".maxAgeDays", "0"), JOB);

        assertTrue(policy.maxAgeDays("refseq_zf_rna").isEmpty());
    }

    @Test
    public void unparseableValuesFallBackInsteadOfBlowingUp() {
        BlastDatabaseStalenessPolicy policy = BlastDatabaseStalenessPolicy.fromProperties(properties(
                JOB + ".maxAgeDays", "soon",
                JOB + ".maxAgeDays.refseq_zf_rna", "also soon"), JOB);

        assertEquals(BlastDatabaseStalenessPolicy.DEFAULT_MAX_AGE_DAYS, policy.defaultMaxAgeDays());
        assertEquals(BlastDatabaseStalenessPolicy.DEFAULT_MAX_AGE_DAYS, policy.maxAgeDays("refseq_zf_rna").getAsInt());
    }

    @Test
    public void otherJobsPropertiesAreIgnored() {
        BlastDatabaseStalenessPolicy policy = BlastDatabaseStalenessPolicy.fromProperties(properties(
                "Some-Other-Job_d.maxAgeDays", "1",
                "Some-Other-Job_d.maxAgeDays.refseq_zf_rna", "1",
                "Some-Other-Job_d.staleExemptDatabases", "refseq_zf_rna"), JOB);

        assertEquals(Map.of(), policy.overrides());
        assertEquals(Set.of(), policy.exempt());
        assertEquals(BlastDatabaseStalenessPolicy.DEFAULT_MAX_AGE_DAYS, policy.maxAgeDays("refseq_zf_rna").getAsInt());
    }

    @Test
    public void shippedConfigurationParsesAsIntended() {
        Properties properties = properties(
                JOB + ".maxAgeDays", "90",
                JOB + ".maxAgeDays.zfin_cdna_seq", "3",
                JOB + ".maxAgeDays.refseq_zf_rna", "10",
                JOB + ".staleExemptDatabases", "repbase_zf,vega_zfin,zfinEnsemblTscript");
        BlastDatabaseStalenessPolicy policy = BlastDatabaseStalenessPolicy.fromProperties(properties, JOB);

        assertEquals(3, policy.maxAgeDays("zfin_cdna_seq").getAsInt());
        assertEquals(10, policy.maxAgeDays("refseq_zf_rna").getAsInt());
        assertEquals(90, policy.maxAgeDays("gbk_zf_mrna").getAsInt());
        assertTrue(policy.maxAgeDays("zfinEnsemblTscript").isEmpty());
    }
}
