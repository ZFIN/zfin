package org.zfin.datatransfer.ncbi;

import org.apache.commons.lang3.StringUtils;

/**
 * Resolves the {@code assembly} column (NCBI's gene2accession.gz, 0-indexed column 12,
 * e.g. "Reference GRCz12tu Primary Assembly") to ZFIN's internal assembly primary key.
 * Shared between the incremental {@link NCBIDirectPort} load and {@link RefSeqAssemblyBackfillTask}
 * so both resolve the same accession the same way.
 */
public final class NcbiAssemblyResolver {

    public static final long GRCZ12TU_ASSEMBLY_ID = 1L;
    public static final long GRCZ12AB_ASSEMBLY_ID = 2L;

    private NcbiAssemblyResolver() {
    }

    public static Long resolveAssemblyId(String rawAssemblyField) {
        if (StringUtils.isBlank(rawAssemblyField)) {
            return null;
        }
        if (rawAssemblyField.contains("GRCz12tu")) {
            return GRCZ12TU_ASSEMBLY_ID;
        }
        if (rawAssemblyField.contains("GRCz12ab")) {
            return GRCZ12AB_ASSEMBLY_ID;
        }
        return null;
    }
}
