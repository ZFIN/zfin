package org.zfin.datatransfer.ensembl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

/**
 * Reads the {@code xref.tsv(.gz)} Ensembl publishes beside each assembly's cDNA FASTA, for its ZFIN cross
 * references: the gene symbol and ZDB ID Ensembl has for an ENSDARG. A plain or gzipped file is accepted.
 *
 * <p>The columns used are {@code gene_stable_id}, {@code xref_id}, {@code xref_label} and {@code db_name}, found by
 * header name; rows whose {@code db_name} is {@code ZFIN} and whose {@code xref_id} is a ZDB ID are kept.
 */
public class EnsemblXrefFile {

    /** A ZFIN gene Ensembl gives an ENSDARG. */
    public record ZfinGene(String zdbId, String symbol) implements Comparable<ZfinGene> {
        @Override
        public int compareTo(ZfinGene o) {
            return zdbId.compareTo(o.zdbId);
        }
    }

    private final Map<String, Set<ZfinGene>> genesByEnsdarg = new HashMap<>();

    public static EnsemblXrefFile read(Path file) throws IOException {
        EnsemblXrefFile xref = new EnsemblXrefFile();
        try (InputStream raw = new BufferedInputStream(Files.newInputStream(file))) {
            raw.mark(2);
            boolean gzipped = raw.read() == 0x1f && raw.read() == 0x8b;
            raw.reset();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    gzipped ? new GZIPInputStream(raw) : raw, StandardCharsets.UTF_8))) {
                xref.parse(reader);
            }
        }
        return xref;
    }

    private void parse(BufferedReader reader) throws IOException {
        String header = reader.readLine();
        if (header == null) {
            return;
        }
        Map<String, Integer> column = new TreeMap<>();
        String[] names = header.split("\t", -1);
        for (int i = 0; i < names.length; i++) {
            column.put(names[i], i);
        }
        for (String required : new String[]{"gene_stable_id", "xref_id", "xref_label", "db_name"}) {
            if (!column.containsKey(required)) {
                throw new IllegalArgumentException("xref file has no " + required + " column; header: " + header);
            }
        }
        int gene = column.get("gene_stable_id"), id = column.get("xref_id"), label = column.get("xref_label"), db = column.get("db_name");
        int widest = Math.max(Math.max(gene, id), Math.max(label, db));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] f = line.split("\t", -1);
            if (f.length <= widest || !"ZFIN".equals(f[db]) || !f[id].startsWith("ZDB-")) {
                continue;
            }
            genesByEnsdarg.computeIfAbsent(f[gene], k -> new TreeSet<>()).add(new ZfinGene(f[id], f[label]));
        }
    }

    /** The ZFIN genes Ensembl gives this (unversioned) ENSDARG; empty if none. */
    public Set<ZfinGene> zfinGenes(String ensdarg) {
        return genesByEnsdarg.getOrDefault(ensdarg, Set.of());
    }

    public int ensdargCount() {
        return genesByEnsdarg.size();
    }
}
