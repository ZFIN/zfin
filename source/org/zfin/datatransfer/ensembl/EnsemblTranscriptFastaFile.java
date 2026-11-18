package org.zfin.datatransfer.ensembl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Reads an Ensembl cDNA FASTA, as published per assembly under
 * {@code ftp.ebi.ac.uk/pub/ensemblorganisms/GCA/.../geneset/cdna.fa.bgz}. A plain or gzipped (.gz, .bgz) file is
 * accepted. Each record's header line looks like
 * <pre>
 * &gt;ENSDART00160001365.1 cdna GRCz12tu:7:67547983:67702067:-1 gene:ENSDARG00160000915.1 gene_biotype:protein_coding transcript_biotype:protein_coding gene_symbol:ARAP1 description:"..."
 * </pre>
 * Versions are dropped from both accessions. The length is that of the sequence. The file names its own
 * assembly in every header; {@link #assembly()} is that name.
 */
public class EnsemblTranscriptFastaFile {

    private static final Pattern ENSDART = Pattern.compile("^>(ENSDART\\d{11})(?:\\.\\d+)?\\s");
    private static final Pattern ASSEMBLY = Pattern.compile("\\scdna\\s+([A-Za-z0-9_]+):");
    private static final Pattern GENE = Pattern.compile("\\sgene:(ENSDARG\\d{11})(?:\\.\\d+)?(?:\\s|$)");
    private static final Pattern BIOTYPE = Pattern.compile("\\stranscript_biotype:(\\S+)");

    /** One transcript of the file. */
    public record Entry(String ensdart, String ensdarg, String biotype, int length, long lineNumber) {
    }

    /** A header line that could not be used, with the reason. */
    public record Rejected(long lineNumber, String reason) {
    }

    private final List<Entry> entries = new ArrayList<>();
    private final List<Rejected> rejected = new ArrayList<>();
    private final Set<String> assemblies = new LinkedHashSet<>();

    public static EnsemblTranscriptFastaFile read(Path file) throws IOException {
        EnsemblTranscriptFastaFile fasta = new EnsemblTranscriptFastaFile();
        try (InputStream raw = new BufferedInputStream(Files.newInputStream(file))) {
            raw.mark(2);
            boolean gzipped = raw.read() == 0x1f && raw.read() == 0x8b;
            raw.reset();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    gzipped ? new GZIPInputStream(raw) : raw, StandardCharsets.UTF_8))) {
                fasta.parse(reader);
            }
        }
        return fasta;
    }

    private void parse(BufferedReader reader) throws IOException {
        String header = null;
        long headerLine = 0;
        int length = 0;
        long lineNumber = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (line.startsWith(">")) {
                finish(header, headerLine, length);
                header = line;
                headerLine = lineNumber;
                length = 0;
            } else {
                length += line.trim().length();
            }
        }
        finish(header, headerLine, length);
    }

    private void finish(String header, long lineNumber, int length) {
        if (header == null) {
            return;
        }
        Matcher id = ENSDART.matcher(header + " ");
        Matcher assembly = ASSEMBLY.matcher(header);
        Matcher gene = GENE.matcher(header);
        Matcher biotype = BIOTYPE.matcher(header);
        if (!id.find()) {
            rejected.add(new Rejected(lineNumber, "no ENSDART accession in header"));
        } else if (!assembly.find()) {
            rejected.add(new Rejected(lineNumber, id.group(1) + ": no assembly in header"));
        } else if (!gene.find()) {
            rejected.add(new Rejected(lineNumber, id.group(1) + ": no gene:ENSDARG in header"));
        } else if (!biotype.find()) {
            rejected.add(new Rejected(lineNumber, id.group(1) + ": no transcript_biotype in header"));
        } else if (length == 0) {
            rejected.add(new Rejected(lineNumber, id.group(1) + ": empty sequence"));
        } else {
            assemblies.add(assembly.group(1));
            entries.add(new Entry(id.group(1), gene.group(1), biotype.group(1), length, lineNumber));
        }
    }

    public List<Entry> entries() {
        return entries;
    }

    public List<Rejected> rejected() {
        return rejected;
    }

    /** The assemblies named by the headers; a usable file has exactly one. */
    public Set<String> assemblies() {
        return assemblies;
    }

    public String assembly() {
        if (assemblies.size() != 1) {
            throw new IllegalStateException("Expected the file to name exactly one assembly but found " + assemblies);
        }
        return assemblies.iterator().next();
    }
}
