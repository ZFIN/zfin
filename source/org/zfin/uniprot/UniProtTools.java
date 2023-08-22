package org.zfin.uniprot;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojavax.CrossRef;
import org.biojavax.Note;
import org.biojavax.SimpleRichAnnotation;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.io.RichSequenceBuilderFactory;
import org.biojavax.bio.seq.io.RichSequenceFormat;
import org.biojavax.bio.seq.io.RichStreamReader;
import org.biojavax.bio.seq.io.RichStreamWriter;
import org.biojavax.ontology.ComparableTerm;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.sequence.DBLink;
import org.zfin.uniprot.dto.UniProtContextSequenceDTO;

import java.io.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

public class UniProtTools {
    private static final int MAX_LINE_WIDTH = 500;
    public static final String MANUAL_CURATION_OF_PROTEIN_IDS = "ZDB-PUB-170131-9";
    public static final String UNIPROT_ID_LOAD_FROM_ENSEMBL = "ZDB-PUB-170502-16";
    public static final String MANUAL_CURATION_OF_UNIPROT_IDS = "ZDB-PUB-220705-2";

    public static final String[] NON_LOAD_PUBS = new String[]{MANUAL_CURATION_OF_PROTEIN_IDS, UNIPROT_ID_LOAD_FROM_ENSEMBL, MANUAL_CURATION_OF_UNIPROT_IDS};

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

    public static RichStreamReader getRichStreamReaderForUniprotDatString(String inputString, boolean elideSymbols) throws FileNotFoundException, BioException {
        BufferedReader br = new BufferedReader(new StringReader(inputString));
        return getRichStreamReaderForUniprotDatFile(br, elideSymbols);
    }


        public static RichStreamWriter getRichStreamWriterForUniprotDatFile(String outfile) throws FileNotFoundException {
        return getRichStreamWriterForUniprotDatFile(new FileOutputStream(outfile));
    }

    public static RichStreamWriter getRichStreamWriterForUniprotDatFile(OutputStream out) throws FileNotFoundException {
        RichSequenceFormat format = getUniProtFormatForWriting();
        return new RichStreamWriter(out, format);
    }

    public static Map<String, RichSequence> getMapOfAccessionsToSequencesFromStreamReader(RichStreamReader richStreamReader) throws BioException {
        Map<String, RichSequence> sequences = new TreeMap<String, RichSequence>();
        populateSequenceMap(richStreamReader, sequences);
        return sequences;
    }

    public static void populateSequenceMap(RichStreamReader richStreamReader, Map<String, RichSequence> sequences) throws BioException {
        while (richStreamReader.hasNext()) {
            RichSequence seq = richStreamReader.nextRichSequence();
            sequences.put(seq.getAccession(), seq);
        }
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

    public static List<Note> getAdditionalAccessionTermNotes(CrossRef crossRef) {
        ComparableTerm term = (ComparableTerm) UniProtFormatZFIN.Terms.getAdditionalAccessionTerm();
        SimpleRichAnnotation sequenceNoteSet = new SimpleRichAnnotation();
        sequenceNoteSet.setNoteSet(crossRef.getNoteSet());
        Note[] notes = sequenceNoteSet.getProperties(term);
        return List.of(notes);
    }

    //use passed in lambda expression to transform the notes
    public static void transformCrossRefNoteSetByTerm(CrossRef crossRef, ComparableTerm term, Function<String, String> transformer) {
        Set<Note> notes = crossRef.getNoteSet();
        for (Note note : notes) {
            if (note.getTerm().equals(term)) {
                note.setValue(transformer.apply(note.getValue()));
            }
        }
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

    public static void setAccession(CrossRef xref, String accession) {
        //CrossRef doesn't allow setting accession, so we have to use reflection
        try {
            Method method = null;
            method = xref.getClass().getDeclaredMethod("setAccession", String.class);
            method.setAccessible(true);
            method.invoke(xref, accession);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getAttributionsSupportingGeneAccessionRelationship(String geneID, String accession) {
        DBLink dblink = getSequenceRepository().getDBLink(geneID, accession);
        if (dblink != null) {
            List<RecordAttribution> attributions = getInfrastructureRepository().getRecordAttributions(dblink.getZdbID());
            List<String> attributionPubIDs = attributions.stream().map(attribution -> attribution.getSourceZdbID()).toList();
            return attributionPubIDs;
        } else {
            return null;
        }
    }

    public static boolean isGeneAccessionRelationshipSupportedByNonLoadPublication(String geneID, String accession) {
        List<String> attributionPubIDs = getAttributionsSupportingGeneAccessionRelationship(geneID, accession);
        if (attributionPubIDs == null) {
            return false;
        }
        return attributionPubIDs.stream().anyMatch(pubID -> isNonLoadPublication(pubID));
    }

    public static boolean isGeneAccessionRelationshipSupportedByNonLoadPublication(UniProtContextSequenceDTO sequence) {
        return isGeneAccessionRelationshipSupportedByNonLoadPublication(sequence.getDataZdbID(), sequence.getAccession());
    }

    public static boolean isAnyGeneAccessionRelationshipSupportedByNonLoadPublication(String accession, List<String> geneIDs) {
        return geneIDs.stream().anyMatch(geneID -> isGeneAccessionRelationshipSupportedByNonLoadPublication(geneID, accession));
    }


    private static boolean isNonLoadPublication(String pubID) {
        return List.of(NON_LOAD_PUBS).contains(pubID);
    }
}
