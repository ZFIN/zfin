package org.zfin.sequence.blast;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.zfin.framework.exec.ExecProcess;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.sequence.BioJavaDefline;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.Sequence;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

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

    public static void main(String[] args) {
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
        printFirstTerms(sortedGeneTranscriptMapCleaned, 10);

        List<MarkerDBLink> ensdargList = getSequenceRepository().getAllEnsemblGenes();
        // <ensdargID, DBLink>
        Map<String, MarkerDBLink> ensdargMap = ensdargList.stream().collect(
                Collectors.toMap(DBLink::getAccessionNumber, Function.identity(), (existing, replacement) -> existing));
        System.out.println("Total Number of Ensembl Genes In ZFIN: " + ensdargMap.size());

        List<DBLink> ensdartList = getSequenceRepository().getAllEnsemblTranscripts();
        System.out.println("Total Number of Ensembl Transcript In ZFIN: " + ensdartList.size());
        List<String> ensdartListString = ensdartList.stream().map(DBLink::getAccessionNumber).collect(Collectors.toList());

        sortedGeneTranscriptMapCleaned.entrySet().removeIf(entry -> !ensdargMap.containsKey(entry.getKey()));
        System.out.println("Total Number of Ensembl Genes with transcripts in FASTA file matching a gene record in ZFIN: " + sortedGeneTranscriptMapCleaned.size());

        // remove transcripts that are being found in ZFIN
        sortedGeneTranscriptMapCleaned.entrySet().forEach(entry -> {
            entry.getValue().removeIf(richSequence -> ensdartListString.contains(getGeneIdFromVersionedAccession(richSequence.getAccession())));
        });
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
        String f = "";
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

