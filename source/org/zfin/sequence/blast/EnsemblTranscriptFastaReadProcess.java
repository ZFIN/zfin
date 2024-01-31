package org.zfin.sequence.blast;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.zfin.framework.exec.ExecProcess;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.LinkDisplay;
import org.zfin.marker.presentation.RelatedMarker;
import org.zfin.marker.presentation.RelatedTranscriptDisplay;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.sequence.*;
import org.zfin.sequence.service.TranscriptService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;
import static org.zfin.repository.RepositoryFactory.getSequenceRepository;
import static org.zfin.sequence.service.TranscriptService.getRelatedTranscripts;

/**
 * This class runs system exec calls robustly.
 */
public class EnsemblTranscriptFastaReadProcess extends ExecProcess {

    private DBLink dbLink;
    private List<Sequence> sequences = new ArrayList<>();


    public EnsemblTranscriptFastaReadProcess(List<String> commandList, DBLink dbLink) {
        super(commandList);
        this.dbLink = dbLink;
    }

    public List<Sequence> getSequences() {

        try {
            sequences.clear();

            StringReader stringReader = new StringReader(getStandardOutput());
            BufferedReader br = new BufferedReader(stringReader);
            RichSequenceIterator iterator;
            SymbolTokenization symbolTokenization;
            if (dbLink.getReferenceDatabase().getPrimaryBlastDatabase().getType() == Database.Type.NUCLEOTIDE) {
                symbolTokenization = RichSequence.IOTools.getNucleotideParser();
            } else {
                symbolTokenization = RichSequence.IOTools.getProteinParser();
            }
            iterator = RichSequence.IOTools.readFasta(br, symbolTokenization, new SimpleNamespace("fasta-in"));
            while (iterator.hasNext()) {
                RichSequence richSequence = iterator.nextRichSequence();
                Sequence sequence = new Sequence();
                sequence.setDbLink(dbLink);
                sequence.setData(richSequence.getInternalSymbolList().seqString().toUpperCase());
                sequence.setDefLine(new BioJavaDefline(richSequence));
                sequences.add(sequence);
            }
        } catch (Exception ex) {
            System.out.println("Problem reading stream" + ex);
            ex.printStackTrace();
        }
        return sequences;
    }

    public static void main(String[] args) throws IOException {
        AbstractScriptWrapper wrapper = new AbstractScriptWrapper();
        wrapper.initAll();
        String fileName = "Danio_rerio.GRCz11.cdna.all.fa";
        // <ensdargID, List<RichSequence>>
        Map<String, List<RichSequence>> geneTranscriptMap = getGeneTranscriptMap(fileName);
        Map<String, List<RichSequence>> sortedGeneTranscriptMap = geneTranscriptMap.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        // remove version number from accession ID
        Map<String, List<RichSequence>> sortedGeneTranscriptMapCleaned = new LinkedHashMap<>();
        sortedGeneTranscriptMap.forEach((s, richSequences) -> {
            String cleanedID = s.split("\\.")[0];
            sortedGeneTranscriptMapCleaned.put(cleanedID, richSequences);
        });
        System.out.println("Total Number of Ensembl Genes with transcripts in FASTA file: " + sortedGeneTranscriptMap.size());
        //printFirstTerms(sortedGeneTranscriptMapCleaned, 10);

        List<MarkerDBLink> ensdargList = getSequenceRepository().getAllEnsemblGenes();
        // <ensdargID, DBLink>
        Map<String, MarkerDBLink> ensdargMap = ensdargList.stream().collect(
            Collectors.toMap(DBLink::getAccessionNumber, Function.identity(), (existing, replacement) -> existing));
        System.out.println("Total Number of Ensembl Genes In ZFIN: " + ensdargMap.size());

        Map<Marker, List<TranscriptDBLink>> geneEnsdartMap = getSequenceRepository().getAllRelevantEnsemblTranscripts();
        System.out.println("Total Number of Genes with Ensembl Transcripts In ZFIN: " + geneEnsdartMap.size());
        //List<String> ensdartListString = ensdartList.stream().map(DBLink::getAccessionNumber).toList();
        List<ReferenceDatabase> referenceDatabases = getSequenceRepository().getReferenceDatabasesByForeignDBName(ForeignDB.AvailableName.ENSEMBL);
        ReferenceDatabase referenceDatabase = getSequenceRepository().getZebrafishSequenceReferenceDatabase(ForeignDB.AvailableName.ENSEMBL, ForeignDBDataType.DataType.RNA);
        List<String> listOfGeneIdsWithNoDifference = new ArrayList<>();
        List<String> listOfGeneIdsWithDifferencesEnsemblLongest = new ArrayList<>();
        List<String> listOfGeneIdsWithDifferences = new ArrayList<>();
        List<String> listOfGeneIdsNotFoundInFile = new ArrayList<>();

        BufferedWriter writer = Files.newBufferedWriter(Paths.get("report-transcript-ensembl-important.txt"));
        BufferedWriter ensemblWriter = Files.newBufferedWriter(Paths.get("report-transcript-ensembl.txt"));

        List<String> headerNames = List.of(
            "GeneID",
            "Number of Transcripts ZFIN",
            "Lengths of transcripts ZFIN",
            "Length of Transcripts not found in ensembl",
            "Ensembl ID",
            "Number of Transcripts Ensembl",
            "Lengths of transcripts Ensembl",
            "Length of Transcripts not found in ZFIN"
        );
        CSVPrinter csvPrinterImportant = new CSVPrinter(writer, CSVFormat.DEFAULT
            .withHeader(headerNames.toArray(String[]::new)));
        CSVPrinter csvPrinter = new CSVPrinter(ensemblWriter, CSVFormat.DEFAULT
            .withHeader(headerNames.toArray(String[]::new)));

        geneEnsdartMap.forEach((gene, markerDBLink) -> {
            List<String> values = new ArrayList<>(headerNames.size());

            List<LinkDisplay> list = getMarkerRepository().getMarkerDBLinksFast(gene, DisplayGroup.GroupName.SUMMARY_PAGE);
            List<LinkDisplay> ensdarg = list.stream().filter(linkDisplay -> linkDisplay.getAccession().startsWith("ENSDARG")).toList();
            if (CollectionUtils.isEmpty(ensdarg))
                return;
            String ensdargAccession = ensdarg.get(0).getAccession();
            List<RichSequence> transcriptsEnsembl = sortedGeneTranscriptMapCleaned.get(ensdargAccession);
            if (transcriptsEnsembl == null) {
                listOfGeneIdsNotFoundInFile.add(ensdargAccession);
                return;
            }
            Set<String> zfinAccessions = markerDBLink.stream().map(TranscriptDBLink::getAccessionNumber).collect(Collectors.toSet());
            Set<String> ensemblAccessions = transcriptsEnsembl.stream().map(richSequence -> richSequence.getAccession().split("\\.")[0]).collect(Collectors.toSet());
            if (zfinAccessions.equals(ensemblAccessions)) {
                listOfGeneIdsWithNoDifference.add(gene.getZdbID());
            } else {
                RelatedTranscriptDisplay disp = TranscriptService.getRelatedTranscriptsForGene(gene);
                List<Integer> lengthsZfin = disp.getTranscripts().stream().map(relatedMarker -> relatedMarker.getDisplayedSequenceDBLinks().stream().map(DBLink::getLength).collect(Collectors.toList())).flatMap(Collection::stream).sorted().toList();
                List<Integer> lengthsEnsembl = transcriptsEnsembl.stream().map(s -> s.getInternalSymbolList().length()).sorted().toList();
                if (lengthsEnsembl.get(lengthsEnsembl.size() - 1) > lengthsZfin.get(lengthsZfin.size() - 1)) {
                    listOfGeneIdsWithDifferencesEnsemblLongest.add(gene.getZdbID());
                } else {
                    listOfGeneIdsWithDifferences.add(gene.getZdbID());
                }

                values.add(gene.getZdbID());
                values.add(String.valueOf(zfinAccessions.size()));
                values.add(disp.getTranscripts().stream().map(relatedMarker -> relatedMarker.getDisplayedSequenceDBLinks().stream().map(link -> String.valueOf(link.getLength())).collect(joining(",")))
                    .collect(joining("|")));
                values.add(CollectionUtils.subtract(lengthsZfin, lengthsEnsembl) + "");
                values.add(ensdargAccession);
                values.add(ensemblAccessions.size() + "");
                values.add(transcriptsEnsembl.stream().map(s -> String.valueOf(s.getInternalSymbolList().length())).collect(Collectors.joining("|")));
                values.add(CollectionUtils.subtract(lengthsEnsembl, lengthsZfin) + "");

                try {
                    if (lengthsEnsembl.get(lengthsEnsembl.size() - 1) > lengthsZfin.get(lengthsZfin.size() - 1)) {
                        csvPrinterImportant.printRecord(values.toArray(String[]::new));
                    } else {
                        csvPrinter.printRecord(values.toArray(String[]::new));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        try {
            writer.close();
            ensemblWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        sortedGeneTranscriptMapCleaned.entrySet().removeIf(entry -> !ensdargMap.containsKey(entry.getKey()));
        System.out.println("Total Number of Ensembl Genes with transcripts in FASTA file matching a gene record in ZFIN: " + sortedGeneTranscriptMapCleaned.size());

        // remove transcripts that are being found in ZFIN
/*
        sortedGeneTranscriptMapCleaned.entrySet().forEach(entry -> {
            entry.getValue().removeIf(richSequence -> ensdartListString.contains(getGeneIdFromVersionedAccession(richSequence.getAccession())));
        });
*/
        // remove accession without any new transcripts left
        sortedGeneTranscriptMapCleaned.entrySet().removeIf(entry -> entry.getValue().size() == 0);
        System.out.println("Total Number of Ensembl Genes with new transcripts in FASTA file matching a gene record in ZFIN: " + sortedGeneTranscriptMapCleaned.size());
        System.out.println("Total Number of Ensembl Transcripts missing in ZFIN: " + sortedGeneTranscriptMapCleaned.values().stream().map(List::size).reduce(0, (integer, richSequences) -> integer + richSequences));

        sortedGeneTranscriptMapCleaned.forEach((s, richSequences) -> {
                richSequences.forEach(richSequence -> {
                    System.out.println(ensdargMap.get(s).getMarker().getZdbID() + "," + getGeneIdFromVersionedAccession(richSequence.getAccession()));
                });
            }
        );
    }

    private static void printFirstTerms(Map<String, List<RichSequence>> sortedMap, int limit) {
        sortedMap.entrySet().stream()
            .skip(0)
            .limit(limit)
            .forEach(entry -> {
                System.out.println(entry.getKey() + "\t" + entry.getValue().size());
            });
    }

    private static Map<String, List<RichSequence>> getGeneTranscriptMap(String fileName) {
        try {
            List<RichSequence> transcriptList = getFastaIterator(fileName);
            return transcriptList.stream().collect(Collectors.groupingBy(EnsemblTranscriptFastaReadProcess::getGeneId));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getGeneId(RichSequence sequence) {

        String line = sequence.getDescription();
        String pattern = "(.*)(gene:)(ENSDARG.*)( gene_biotype)(.*)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(line);

        if (m.find()) {
            return m.group(3);
        }
        return null;
    }

    private static String getGeneIdFromVersionedAccession(String accession) {
        return accession.split("\\.")[0];
    }

    private static List<RichSequence> getFastaIterator(String fileName) throws FileNotFoundException {
        FileReader fileReader = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fileReader);
        RichSequenceIterator iterator;
        SymbolTokenization symbolTokenization = RichSequence.IOTools.getNucleotideParser();
        iterator = RichSequence.IOTools.readFasta(br, symbolTokenization, new SimpleNamespace(""));

        List<RichSequence> sequenceList = new ArrayList<>();
        while (iterator.hasNext()) {
            try {
                sequenceList.add(iterator.nextRichSequence());
            } catch (BioException e) {
                e.printStackTrace();
            }
        }
        return sequenceList;
    }

}

