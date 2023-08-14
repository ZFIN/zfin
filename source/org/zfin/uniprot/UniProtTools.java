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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UniProtTools {
    private static final int MAX_LINE_WIDTH = 500;

    public static RichStreamReader getRichStreamReaderForUniprotDatFile(String inputFileName, boolean elideSymbols) throws FileNotFoundException, BioException {
        BufferedReader br = new BufferedReader(new FileReader(inputFileName));
        return getRichStreamReaderForUniprotDatFile(br, elideSymbols);
    }

    public static RichStreamReader getRichStreamReaderForUniprotDatFile(BufferedReader inputFileReader, boolean elideSymbols) throws FileNotFoundException, BioException {
        RichSequenceFormat inFormat = new UniProtFormatZFIN();

        //skips parsing of certain sections that otherwise would throw exceptions
        inFormat.setElideFeatures(true);
        inFormat.setElideReferences(true);
        inFormat.setElideSymbols(elideSymbols);

        FiniteAlphabet alpha = (FiniteAlphabet) AlphabetManager.alphabetForName("PROTEIN");
        SymbolTokenization tokenization = alpha.getTokenization("default");

        return new RichStreamReader(
                inputFileReader, inFormat, tokenization,
                RichSequenceBuilderFactory.THRESHOLD,
                null);
    }

    public static RichStreamWriter getRichStreamWriterForUniprotDatFile(String outfile) throws FileNotFoundException {
        return getRichStreamWriterForUniprotDatFile(new FileOutputStream(outfile));
    }

    public static RichStreamWriter getRichStreamWriterForUniprotDatFile(OutputStream out) throws FileNotFoundException {
        RichSequenceFormat format = getUniProtFormatForWriting();
        return new RichStreamWriter(out, format);
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

    public static Date getDateUpdatedFromNotes(RichSequence seq1) {
        SimpleRichAnnotation seq1NoteSet = new SimpleRichAnnotation();
        seq1NoteSet.setNoteSet(seq1.getNoteSet());

        String stringDate = (String) seq1NoteSet.getProperty(RichSequence.Terms.getDateUpdatedTerm());
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");

        try {
            return formatter.parse(stringDate);
        } catch (Exception e) {
            return null;
        }
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


    public static String getArgOrEnvironmentVar(String[] args, int index, String envVar) {
        if (args.length > index && args[index] != null) {
            return args[index];
        }

        String result = System.getenv(envVar);

        if (result == null) {
            System.err.println("Missing required argument: " + envVar + ". Please provide it as an environment variable or as argument: " + (index + 1) + ". ");
            System.exit(1);
        }

        return result;
    }
}
