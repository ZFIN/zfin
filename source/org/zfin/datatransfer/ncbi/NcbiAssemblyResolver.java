package org.zfin.datatransfer.ncbi;

import org.apache.commons.lang3.StringUtils;
import org.zfin.sequence.gff.Assembly;
import org.zfin.sequence.gff.AssemblyDAO;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resolves the {@code assembly} column (NCBI's gene2accession.gz, 0-indexed column 12,
 * e.g. "Alternate GRCz12tu" or "Reference GRCz13ab Primary Assembly") to ZFIN's internal
 * assembly primary key, by matching a whitespace-separated word of that text against the
 * {@code assembly} table's names. Build one per run with {@link #fromDatabase()}, which reads
 * the table once; shared between the incremental {@link NCBIDirectPort} load and
 * {@link RefSeqAssemblyBackfillTask} so both resolve the same accession the same way.
 */
public final class NcbiAssemblyResolver {

    private final Map<String, Long> assemblyIdsByName;

    public NcbiAssemblyResolver(Map<String, Long> assemblyIdsByName) {
        this.assemblyIdsByName = Map.copyOf(assemblyIdsByName);
    }

    public static NcbiAssemblyResolver fromDatabase() {
        return new NcbiAssemblyResolver(new AssemblyDAO().findAllSortedAssemblies().stream()
                .collect(Collectors.toMap(Assembly::getName, Assembly::getId)));
    }

    public Long resolveAssemblyId(String rawAssemblyField) {
        if (StringUtils.isBlank(rawAssemblyField)) {
            return null;
        }
        for (String word : rawAssemblyField.trim().split("\\s+")) {
            Long assemblyId = assemblyIdsByName.get(word);
            if (assemblyId != null) {
                return assemblyId;
            }
        }
        return null;
    }
}
