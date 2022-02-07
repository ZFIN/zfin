package org.zfin.sequence.blast;

import org.apache.commons.collections.CollectionUtils;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.NewSimpleAssembly;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.SymbolList;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.SimpleRichSequence;
import org.biojavax.bio.seq.io.FastaHeader;
import org.zfin.framework.exec.ExecProcess;
import org.zfin.marker.Marker;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.sequence.BioJavaDefline;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.Sequence;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

/**
 * This class runs system exec calls robustly.
 */
public class FastaReadProcess extends ExecProcess {

    private DBLink dbLink;
    private List<Sequence> sequences = new ArrayList<Sequence>();


    public FastaReadProcess(List<String> commandList, DBLink dbLink) {
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

    public static void main(String[] args) throws BioException, IOException {
        AbstractScriptWrapper wrapper = new AbstractScriptWrapper();
        wrapper.initAll();
        FileReader fileReader = new FileReader("Danio_rerio.VEGA67.cdna.all.fa");
        BufferedReader br = new BufferedReader(fileReader);
        RichSequenceIterator iterator;
        SymbolTokenization symbolTokenization = RichSequence.IOTools.getNucleotideParser();
        iterator = RichSequence.IOTools.readFasta(br, symbolTokenization, new SimpleNamespace(""));
        List<RichSequence> sequences = new ArrayList<>();
        List<String> ids = Files.readAllLines(Paths.get("ottdarts-no-sequence.csv.org"));
        List<String> foundIds = new ArrayList<>();
        SimpleNamespace ns = new SimpleNamespace("tpe");
        while (iterator.hasNext()) {
            RichSequence richSequence = iterator.nextRichSequence();
            ids.forEach(id -> {
                if (richSequence.getAccession().startsWith(id)) {
                    Marker marker = getMarkerRepository().getTranscriptByVegaID(id);
                    String description = richSequence.getDescription();
                    richSequence.setDescription(marker.getZdbID() + " " + description);
                    sequences.add(richSequence);
                    foundIds.add(id);
                }
            });

            Sequence sequence = new Sequence();
            //sequence.setDbLink(dbLink);
/*
            sequence.setData(richSequence.getInternalSymbolList().seqString().toUpperCase());
            sequence.setDefLine(new BioJavaDefline(richSequence));
            sequences.add(sequence);
*/
        }
        List list = new ArrayList<>(CollectionUtils.disjunction(foundIds, ids));
        System.out.println(list);

        FastaHeader header = new FastaHeader();
        header.setShowNamespace(true);
        header.setShowVersion(true);
        header.setShowIdentifier(false);
        OutputStream os = new FileOutputStream("output.fasta");
        sequences.forEach(richSequence -> {
            try {
                RichSequence.IOTools.writeFasta(os, richSequence, new SimpleNamespace("tpe"), header);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

