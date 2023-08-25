package org.zfin.uniprot.datfiles;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojavax.bio.seq.io.RichSequenceBuilderFactory;
import org.biojavax.bio.seq.io.RichSequenceFormat;
import org.biojavax.bio.seq.io.RichStreamReader;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.adapter.RichStreamReaderAdapter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.Map;
import java.util.TreeMap;

public class DatFileReader {

    public static RichStreamReaderAdapter getRichStreamReaderForUniprotDatFile(String inputFileName, boolean elideSymbols) throws FileNotFoundException, BioException {
        BufferedReader br = new BufferedReader(new FileReader(inputFileName));
        return getRichStreamReaderForUniprotDatFile(br, elideSymbols);
    }

    public static RichStreamReaderAdapter getRichStreamReaderForUniprotDatFile(BufferedReader inputFileReader, boolean elideSymbols) throws BioException {
        RichSequenceFormat inFormat = new UniProtFormatZFIN();

        //skips parsing of certain sections that otherwise would throw exceptions
        inFormat.setElideFeatures(true);
        inFormat.setElideReferences(true);
        inFormat.setElideSymbols(elideSymbols);

        FiniteAlphabet alpha = (FiniteAlphabet) AlphabetManager.alphabetForName("PROTEIN");
        SymbolTokenization tokenization = alpha.getTokenization("default");

        RichStreamReader wrappedReader = new RichStreamReader(
                inputFileReader, inFormat, tokenization,
                RichSequenceBuilderFactory.THRESHOLD,
                null);

        return new RichStreamReaderAdapter(wrappedReader);
    }

    public static RichStreamReaderAdapter getRichStreamReaderForUniprotDatString(String inputString, boolean elideSymbols) throws FileNotFoundException, BioException {
        BufferedReader br = new BufferedReader(new StringReader(inputString));
        return getRichStreamReaderForUniprotDatFile(br, elideSymbols);
    }

    public static Map<String, RichSequenceAdapter> getMapOfAccessionsToSequencesFromStreamReader(RichStreamReaderAdapter richStreamReader) throws BioException {
        Map<String, RichSequenceAdapter> sequences = new TreeMap<>();
        while (richStreamReader.hasNext()) {
            RichSequenceAdapter seq = richStreamReader.nextRichSequence();
            sequences.put(seq.getAccession(), seq);
        }
        return sequences;
    }

}
