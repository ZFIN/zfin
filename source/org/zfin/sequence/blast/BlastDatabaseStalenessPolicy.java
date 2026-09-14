package org.zfin.sequence.blast;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Properties;
import java.util.Set;

/**
 * How old each blast database is allowed to get before {@link ValidateBlastDatabases}
 * complains about it.
 * <p/>
 * Read out of report.properties so the thresholds can be tuned without a deploy of
 * new code, using these keys (jobName is Validate-Blast-Databases_d):
 * <pre>
 *   &lt;jobName&gt;.maxAgeDays=90                  default for every database
 *   &lt;jobName&gt;.maxAgeDays.zfin_cdna_seq=3     per-database override
 *   &lt;jobName&gt;.staleExemptDatabases=a,b,c     never checked for age
 * </pre>
 *
 * @param defaultMaxAgeDays Age limit for databases with no override. Zero or less
 *                          turns the staleness check off altogether.
 * @param overrides         Per-database age limits, keyed by blast database abbreviation.
 * @param exempt            Abbreviations whose age is never checked, for databases
 *                          whose upstream source is retired or frozen.
 */
public record BlastDatabaseStalenessPolicy(int defaultMaxAgeDays,
                                           Map<String, Integer> overrides,
                                           Set<String> exempt) {

    private static final Logger LOG = LogManager.getLogger(BlastDatabaseStalenessPolicy.class);

    public static final String MAX_AGE_DAYS = "maxAgeDays";
    public static final String STALE_EXEMPT_DATABASES = "staleExemptDatabases";
    public static final int DEFAULT_MAX_AGE_DAYS = 90;

    public BlastDatabaseStalenessPolicy {
        overrides = Collections.unmodifiableMap(new HashMap<>(overrides));
        exempt = Collections.unmodifiableSet(new HashSet<>(exempt));
    }

    public static BlastDatabaseStalenessPolicy fromProperties(Properties properties, String jobName) {
        String defaultPrefix = jobName + "." + MAX_AGE_DAYS;
        int defaultMaxAgeDays = parseDays(properties.getProperty(defaultPrefix), DEFAULT_MAX_AGE_DAYS, defaultPrefix);

        Map<String, Integer> overrides = new HashMap<>();
        String overridePrefix = defaultPrefix + ".";
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(overridePrefix)) {
                String abbrev = key.substring(overridePrefix.length());
                int days = parseDays(properties.getProperty(key), defaultMaxAgeDays, key);
                overrides.put(abbrev, days);
            }
        }

        Set<String> exempt = new HashSet<>();
        String exemptValue = properties.getProperty(jobName + "." + STALE_EXEMPT_DATABASES);
        if (exemptValue != null) {
            Arrays.stream(exemptValue.split(","))
                    .map(String::trim)
                    .filter(abbrev -> !abbrev.isEmpty())
                    .forEach(exempt::add);
        }

        return new BlastDatabaseStalenessPolicy(defaultMaxAgeDays, overrides, exempt);
    }

    private static int parseDays(String value, int fallback, String key) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            LOG.warn("Ignoring unparseable value [" + value + "] for " + key + ", using " + fallback);
            return fallback;
        }
    }

    /**
     * The age limit for one database, or empty when its age should not be checked.
     */
    public OptionalInt maxAgeDays(String abbrev) {
        if (exempt.contains(abbrev)) {
            return OptionalInt.empty();
        }
        int maxAgeDays = overrides.getOrDefault(abbrev, defaultMaxAgeDays);
        return maxAgeDays > 0 ? OptionalInt.of(maxAgeDays) : OptionalInt.empty();
    }
}
