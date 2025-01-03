package org.zfin.sequence.load;

import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.sequence.DBLink;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

public class EnsemblTranscriptFastaReader extends EnsemblTranscriptBase {

    public static final String ENSEMBL_ZF_FA = "ensembl_zf.fa";
    public static final String ENSEMBL_ZF_ONLY_FA = "ensembl_zf_only.fa";

    private static String fastaDirectory;

    public static void main(String[] args) throws IOException {
        fastaDirectory = args[0];
        System.out.println("Arguments: " + fastaDirectory);
        AbstractScriptWrapper wrapper = new AbstractScriptWrapper();
        wrapper.initAll();
        EnsemblTranscriptFastaReader loader = new EnsemblTranscriptFastaReader();
        File combinedFastaFile = getCombinedFastaFile();
        loader.init(combinedFastaFile);
        // move the file into server_apps/data_transfer/Ensembl directory
        if (!combinedFastaFile.renameTo(new File(fastaDirectory, combinedFastaFile.getName()))) {
            throw new RuntimeException("Could not move file " + combinedFastaFile.getAbsolutePath());
        }
    }

    public void init(File file) throws IOException {
        List<String> allTranscriptIDsInZfin = getAllTranscriptIdsInZFIN();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        Map<String, List<String>> blastEntryMap = new LinkedHashMap<>();
        String protId = null;
        while ((line = br.readLine()) != null) {
            if (line.startsWith(">")) {
                String[] defline = line.substring(1).trim().split(" +");
                // new order:
                String geneId = defline[3].replace("gene", "").replace(":", "");
                String tscriptId = defline[4].replace("transcript:", "");
                protId = defline[0];
                String newDefline = ">tpe|";
                newDefline += protId + "|";
                newDefline += tscriptId + "|";
                newDefline += geneId;
                newDefline += " " + defline[5] + " ";
                if (defline.length == 7) {
                    newDefline += defline[6] + " ";
                }
                newDefline += defline[1] + " " + defline[2];
                newDefline += "\n";
                List<String> blastLines = new ArrayList<>();
                blastLines.add(newDefline);
                blastEntryMap.put(protId, blastLines);
            } else {
                List<String> blastLines = blastEntryMap.get(protId);
                blastLines.add(line);
            }
        }

        writeFastaFile(blastEntryMap, new File(fastaDirectory, ENSEMBL_ZF_FA));
        Map<String, List<String>> zfinOnlyBlastMap = new HashMap<>();
        blastEntryMap.forEach((ensdart, blastLines) -> {
            if (allTranscriptIDsInZfin.contains(getUnversionedAccession(ensdart))) {
                zfinOnlyBlastMap.put(ensdart, blastLines);
            }
        });
        writeFastaFile(zfinOnlyBlastMap, new File(fastaDirectory, ENSEMBL_ZF_ONLY_FA));
        System.exit(0);
    }

    private static void writeFastaFile(Map<String, List<String>> blastEntryMap, File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        blastEntryMap.forEach((ensdart, blastLines) -> {
            AtomicInteger index = new AtomicInteger(0);
            blastLines.forEach(blastLine -> {
                try {
                    writer.write(blastLine);
                    if (index.incrementAndGet() < blastLines.size() - 1) {
                        writer.newLine();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        writer.close();
    }

    private List<String> getAllTranscriptIdsInZFIN() {
        List<DBLink> transcripts = getSequenceRepository().getAllEnsemblTranscripts();
        return transcripts.stream().map(DBLink::getAccessionNumber).toList();
    }

}

