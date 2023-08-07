package org.zfin.uniprot;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojavax.Note;
import org.biojavax.SimpleRichAnnotation;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.io.RichSequenceBuilderFactory;
import org.biojavax.bio.seq.io.RichSequenceFormat;
import org.biojavax.bio.seq.io.RichStreamReader;
import org.biojavax.bio.seq.io.RichStreamWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UniProtTools {
    private static final int MAX_LINE_WIDTH = 500;

    public static RichStreamReader getRichStreamReaderForUniprotDatFile(String inputFileName, boolean elideSymbols) throws FileNotFoundException, BioException {
        BufferedReader br = new BufferedReader(new FileReader(inputFileName));
        RichSequenceFormat inFormat = new UniProtFormatZFIN();

        //skips parsing of certain sections that otherwise would throw exceptions
        inFormat.setElideFeatures(true);
        inFormat.setElideReferences(true);
        inFormat.setElideSymbols(elideSymbols);

        FiniteAlphabet alpha = (FiniteAlphabet) AlphabetManager.alphabetForName("PROTEIN");
        SymbolTokenization tokenization = alpha.getTokenization("default");

        return new RichStreamReader(
                br, inFormat, tokenization,
                RichSequenceBuilderFactory.THRESHOLD,
                null);
    }

    public static RichStreamWriter getRichStreamWriterForUniprotDatFile(String outfile) throws FileNotFoundException {
        FileOutputStream fos = new FileOutputStream(outfile);
        RichSequenceFormat format = getUniProtFormatForWriting();
        return new RichStreamWriter(fos, format);
    }

    public static UniProtFormatZFIN getUniProtFormatForWriting() {
        UniProtFormatZFIN format = new UniProtFormatZFIN();

        FiniteAlphabet alphabet = (FiniteAlphabet) AlphabetManager.alphabetForName("PROTEIN");
        format.setOverrideAlphabet(alphabet);
        format.setLineWidth(MAX_LINE_WIDTH);
        return format;
    }


    public static List<Note> getKeywordNotes(RichSequence seq1) {
        SimpleRichAnnotation seq1NoteSet = new SimpleRichAnnotation();
        seq1NoteSet.setNoteSet(seq1.getNoteSet());
        Note[] keywords = seq1NoteSet.getProperties(RichSequence.Terms.getKeywordTerm());
        return List.of(keywords);
    }


    public static String sequenceToString(Sequence seq1) {
        UniProtFormatZFIN format = getUniProtFormatForWriting();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            PrintStream printStream = new PrintStream(outputStream);
            format.beginWriting();
            format.writeSequence(seq1, printStream);
            format.finishWriting();
        } catch (IOException se) {
            se.printStackTrace();
            throw new RuntimeException(se);
        }
        return outputStream.toString();
    }
}
