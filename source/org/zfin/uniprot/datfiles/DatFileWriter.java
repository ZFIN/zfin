package org.zfin.uniprot.datfiles;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojavax.bio.seq.io.RichSequenceFormat;
import org.biojavax.bio.seq.io.RichStreamWriter;
import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.io.*;

public class DatFileWriter {
    public static final int MAX_LINE_WIDTH = 500;

    public static String sequenceToString(RichSequenceAdapter seq1) {
        UniProtFormatZFIN format = getUniProtFormatForWriting();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            PrintStream printStream = new PrintStream(outputStream);
            format.beginWriting();
            format.writeSequence(seq1.unwrap(), printStream);
            format.finishWriting();
        } catch (IOException se) {
            se.printStackTrace();
            throw new RuntimeException(se);
        }
        return outputStream.toString();
    }


    public static UniProtFormatZFIN getUniProtFormatForWriting() {
        UniProtFormatZFIN format = new UniProtFormatZFIN();

        FiniteAlphabet alphabet = (FiniteAlphabet) AlphabetManager.alphabetForName("PROTEIN");
        format.setOverrideAlphabet(alphabet);
        format.setLineWidth(MAX_LINE_WIDTH);
        return format;
    }

    public static RichStreamWriter getRichStreamWriterForUniprotDatFile(OutputStream out) throws FileNotFoundException {
        RichSequenceFormat format = getUniProtFormatForWriting();
        return new RichStreamWriter(out, format);
    }


    public static RichStreamWriter getRichStreamWriterForUniprotDatFile(String outfile) throws FileNotFoundException {
        return getRichStreamWriterForUniprotDatFile(new FileOutputStream(outfile));
    }


}
