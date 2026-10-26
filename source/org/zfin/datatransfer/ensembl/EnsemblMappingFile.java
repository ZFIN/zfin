package org.zfin.datatransfer.ensembl;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Reads a z11 -> newer assembly Ensembl gene mapping file (the CSV built by map_ensembl_z11_to_z12.py).
 *
 * <p>Required columns: {@code zdb_id} plus at least one assembly column. The assembly columns are
 * {@code z12tu}, {@code z12ab} and {@code z13ab}. {@code z11} is accepted but never loaded, because those links
 * already exist. An optional {@code conflict} column flags rows whose gene assignment is in doubt. Any other
 * column is ignored. A plain or gzipped file is accepted.
 *
 * <p>Only unambiguous accessions end up in {@link #entries()}. Everything else is kept, with the reason, in
 * {@link #rejected()} (unusable cell), {@link #conflicts()} (flagged by the file) or {@link #ambiguous()} (the
 * same ENSDARG given to several genes) so the load can report what it left out.
 */
public class EnsemblMappingFile {

    /** Mapping file column -> assembly name in the {@code assembly} table. */
    public static final Map<String, String> ASSEMBLY_COLUMNS = Map.of(
            "z12tu", "GRCz12tu",
            "z12ab", "GRCz12ab",
            "z13ab", "GRCz13ab");

    /** Stable load order for the assemblies. */
    public static final List<String> ASSEMBLY_COLUMN_ORDER = List.of("z12tu", "z12ab", "z13ab");

    /** The ENSDARG number range each assembly's IDs come from; the others are only checked for format. */
    private static final Map<String, String> ID_PREFIX = Map.of(
            "z12tu", "ENSDARG00160",
            "z12ab", "ENSDARG00180");

    private static final Pattern ENSDARG = Pattern.compile("ENSDARG\\d{11}");
    private static final Pattern GENE_ZDB_ID = Pattern.compile("ZDB-[A-Z]+-\\d{6}-\\d+");

    /** One gene -> ENSDARG -> assembly association to load. */
    public record Entry(String zdbId, String ensdarg, String assemblyName, long lineNumber) {
    }

    /** A mapping cell that was not usable, with the reason. */
    public record Rejected(long lineNumber, String zdbId, String column, String value, String reason) {
    }

    /** Rows held back because the file flags them in its {@code conflict} column. */
    public record Conflict(long lineNumber, String zdbId, String ensdarg, String assemblyName, String conflict) {
    }

    /** An ENSDARG the file assigns to more than one gene (in this assembly), so it is not loaded for any of them. */
    public record Ambiguous(long lineNumber, String zdbId, String ensdarg, String assemblyName, Set<String> genes) {
    }

    private final List<Entry> entries = new ArrayList<>();
    private final List<Rejected> rejected = new ArrayList<>();
    private final List<Conflict> conflicts = new ArrayList<>();
    private final List<Ambiguous> ambiguous = new ArrayList<>();
    private final Map<String, Set<String>> genesByAccession = new HashMap<>();
    private final Set<String> assemblyColumnsPresent = new LinkedHashSet<>();
    private long rowCount;
    private long duplicateCount;

    public static EnsemblMappingFile read(Path file, boolean includeConflicts) throws IOException {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(file))) {
            in.mark(2);
            boolean gzipped = in.read() == 0x1f && in.read() == 0x8b;
            in.reset();
            try (Reader reader = new InputStreamReader(gzipped ? new GZIPInputStream(in) : in, StandardCharsets.UTF_8)) {
                return read(reader, includeConflicts);
            }
        }
    }

    public static EnsemblMappingFile read(Reader reader, boolean includeConflicts) throws IOException {
        EnsemblMappingFile result = new EnsemblMappingFile();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .setAllowMissingColumnNames(true)
                .build();
        try (CSVParser parser = format.parse(reader)) {
            Map<String, Integer> header = parser.getHeaderMap();
            if (header == null || !header.containsKey("zdb_id")) {
                throw new IllegalArgumentException("Mapping file has no 'zdb_id' column. Columns found: "
                        + (header == null ? "none" : header.keySet()));
            }
            for (String column : ASSEMBLY_COLUMN_ORDER) {
                if (header.containsKey(column)) {
                    result.assemblyColumnsPresent.add(column);
                }
            }
            if (result.assemblyColumnsPresent.isEmpty()) {
                throw new IllegalArgumentException("Mapping file has none of the assembly columns "
                        + ASSEMBLY_COLUMN_ORDER + ". Columns found: " + header.keySet());
            }
            Map<String, Entry> unique = new LinkedHashMap<>();
            for (CSVRecord record : parser) {
                result.rowCount++;
                result.readRow(record, header.containsKey("conflict"), includeConflicts, unique);
            }
            for (Entry entry : unique.values()) {
                Set<String> genes = result.genesByAccession.get(entry.assemblyName() + "|" + entry.ensdarg());
                if (genes.size() > 1) {
                    result.ambiguous.add(new Ambiguous(entry.lineNumber(), entry.zdbId(), entry.ensdarg(), entry.assemblyName(), genes));
                } else {
                    result.entries.add(entry);
                }
            }
        }
        return result;
    }

    private void readRow(CSVRecord record, boolean hasConflictColumn, boolean includeConflicts, Map<String, Entry> unique) {
        long line = record.getRecordNumber() + 1;
        String zdbId = value(record, "zdb_id");
        String conflict = hasConflictColumn ? value(record, "conflict") : "";
        for (String column : assemblyColumnsPresent) {
            String ensdarg = value(record, column);
            if (ensdarg.isEmpty()) {
                continue;
            }
            String assemblyName = ASSEMBLY_COLUMNS.get(column);
            if (zdbId.isEmpty()) {
                rejected.add(new Rejected(line, zdbId, column, ensdarg, "no zdb_id for this row"));
                continue;
            }
            if (!GENE_ZDB_ID.matcher(zdbId).matches()) {
                rejected.add(new Rejected(line, zdbId, "zdb_id", zdbId, "not a ZDB ID"));
                continue;
            }
            if (!ENSDARG.matcher(ensdarg).matches()) {
                rejected.add(new Rejected(line, zdbId, column, ensdarg, "not an ENSDARG ID"));
                continue;
            }
            String prefix = ID_PREFIX.get(column);
            if (prefix != null && !ensdarg.startsWith(prefix)) {
                rejected.add(new Rejected(line, zdbId, column, ensdarg,
                        "ID is not in the " + assemblyName + " range (" + prefix + "...)"));
                continue;
            }
            // counted before the conflict check: a flagged row still claims the ENSDARG for its gene
            genesByAccession.computeIfAbsent(assemblyName + "|" + ensdarg, k -> new TreeSet<>()).add(zdbId);
            if (!conflict.isEmpty() && !includeConflicts) {
                conflicts.add(new Conflict(line, zdbId, ensdarg, assemblyName, conflict));
                continue;
            }
            Entry entry = new Entry(zdbId, ensdarg, assemblyName, line);
            if (unique.putIfAbsent(zdbId + "|" + ensdarg + "|" + assemblyName, entry) != null) {
                duplicateCount++;
            }
        }
    }

    private static String value(CSVRecord record, String column) {
        return record.isSet(column) ? record.get(column).trim() : "";
    }

    public List<Entry> entries() {
        return entries;
    }

    public List<Rejected> rejected() {
        return rejected;
    }

    public List<Conflict> conflicts() {
        return conflicts;
    }

    public List<Ambiguous> ambiguous() {
        return ambiguous;
    }

    public Set<String> assemblyColumnsPresent() {
        return assemblyColumnsPresent;
    }

    public long rowCount() {
        return rowCount;
    }

    /** Repeats of an association already seen earlier in the file (a z12 ID shared by several z11 rows). */
    public long duplicateCount() {
        return duplicateCount;
    }
}
