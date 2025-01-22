package org.zfin.uniprot.adapter;

import org.biojavax.Comment;
import org.biojavax.Note;
import org.biojavax.RankedCrossRef;
import org.biojavax.SimpleRichAnnotation;
import org.biojavax.bio.seq.RichSequence;
import org.zfin.uniprot.datfiles.DatFileWriter;
import org.zfin.uniprot.datfiles.UniProtFormatZFIN;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.zfin.sequence.ForeignDB.AvailableName.EC;

public class RichSequenceAdapter {
    public enum DatabaseSource {
        ZFIN("ZFIN"),
        GENEID("GeneID"),
        REFSEQ("RefSeq"),
        EMBL("EMBL"),
        GO("GO"),
        INTERPRO("InterPro"),
        PFAM("Pfam"),
        PROSITE("PROSITE"),
        PDB("PDB"),
        ENSEMBL("Ensembl"),
        EC("EC");
        private final String value;
        DatabaseSource(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }

    private final RichSequence originalRichSequence;

    public RichSequenceAdapter(RichSequence wrappedObject) {
        this.originalRichSequence = wrappedObject;
    }

    public RichSequence unwrap() {
        //I would prefer to not expose the inner object, but File Writing needs it
        return originalRichSequence;
    }

    private RichSequence wo() {
        return originalRichSequence;
    }

    public String getAccession() {
        return originalRichSequence.getAccession();
    }

    public boolean isDanioRerio() {
        return originalRichSequence.getTaxon().getNCBITaxID() == 7955;
    }

    public boolean isDanioRerioOrRelated() {
        return isDanioRerio() || this.wo().getTaxon().getDisplayName().toLowerCase().contains("danio rerio");
    }

    //TODO: wrap cross references
    public Set<RankedCrossRef> getRankedCrossRefs() {
        return originalRichSequence.getRankedCrossRefs();
    }

    public void setRankedCrossRefs(Set<RankedCrossRef> rankedCrossRefs) {
        this.wo().setRankedCrossRefs(rankedCrossRefs);
    }

    public String getName() {
        return this.wo().getName();
    }

    public Set<String> getRefSeqsWithoutVersion() {
        return getRefSeqs().stream().map(r -> {
            if (r.contains(".")) {
                return r.substring(0, r.indexOf("."));
            }
            return r;
        }).collect(Collectors.toSet());
    }

    public Set<String> getRefSeqs() {
        RichSequence richSequence = wo();

        //get all cross references with dbname = RefSeq
        List<String> refseqs = richSequence.getRankedCrossRefs().stream()
                .filter(rc -> ((RankedCrossRef)rc).getCrossRef().getDbname().equals("RefSeq"))
                .map(rc -> ((RankedCrossRef)rc).getCrossRef().getAccession())
                .toList();

        //Lines like 'DR   RefSeq; NP_001107539.1; NM_001114067.2.' store the NM_... part as a note
        List<String> additionalRefseqs = richSequence.getRankedCrossRefs().stream()
                .filter(rc -> ((RankedCrossRef) rc).getCrossRef().getDbname().equals("RefSeq"))
                .flatMap(rc -> ((RankedCrossRef) rc).getCrossRef().getNoteSet().stream())
                .filter(note -> ((Note)note).getTerm().equals(UniProtFormatZFIN.Terms.getAdditionalAccessionTerm()))
                .map(note -> ((Note)note).getValue())
                .toList();

        //return union
        Set<String> combined = new TreeSet<>();
        combined.addAll(refseqs);
        combined.addAll(additionalRefseqs);
        return combined;
    }

    public Date getDateUpdated() {
        SimpleRichAnnotation seq1NoteSet = new SimpleRichAnnotation();
        seq1NoteSet.setNoteSet(this.getNoteSet());

        String stringDate = (String) seq1NoteSet.getProperty(RichSequence.Terms.getDateUpdatedTerm());
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");

        try {
            return formatter.parse(stringDate);
        } catch (Exception e) {
            return null;
        }
    }

    public int getLength() {
        SimpleRichAnnotation annotation = new SimpleRichAnnotation();
        annotation.setNoteSet(originalRichSequence.getNoteSet());
        int length = 0;
        try {
            length = Integer.parseInt(annotation.getProperty("sequence_length").toString());
        } catch (Exception e) {
            // do nothing, length remains zero
        }
        return length;
    }


    /*
     * Return notes that correspond to keyword terms
     */
    public List<Note> getKeywordNotes() {
        SimpleRichAnnotation seq1NoteSet = new SimpleRichAnnotation();
        seq1NoteSet.setNoteSet(this.getNoteSet());
        Note[] keywords = seq1NoteSet.getProperties(RichSequence.Terms.getKeywordTerm());
        return List.of(keywords);
    }

    /**
     * Return keywords as strings
     */
    public List<String> getKeywords() {
        return getKeywordNotes().stream()
                .map(note -> note.getValue())
                .toList();
    }

    /**
     * Removes the curly braces and the text inside them
     * Example: return "Glycosyltransferase" from "Glycosyltransferase {ECO:0000256|ARBA:ARBA00022676, ECO:0000256|RuleBase:RU003718}"
     * @return
     */
    public List<String> getPlainKeywords() {
        return getKeywords().stream()
                .map(keyword -> keyword.replaceAll("\\{.*?\\}", "").trim())
                .toList();
    }

    private Set<Note> getNoteSet() {
        return this.wo().getNoteSet();
    }

    public Collection<CrossRefAdapter> getCrossRefsByDatabase(String dbName) {
        if (dbName == null) {
            return Collections.emptyList();
        }
        if (dbName.equals(EC.toString())) {
            return getECCrossReferences();
        }
        Set<RankedCrossRef> matches = getRankedCrossRefs().stream().filter(rc -> rc.getCrossRef().getDbname().equals(dbName)).collect(Collectors.toSet());
        return CrossRefAdapter.fromRankedCrossRefs(matches);
    }
    public Collection<CrossRefAdapter> getCrossRefsByDatabase(DatabaseSource source) {
        return getCrossRefsByDatabase(source.getValue());
    }

    public List<String> getCrossRefIDsByDatabase(String dbName) {
        return getCrossRefsByDatabase(dbName).stream().map(crossRef -> crossRef.getAccession()).toList();
    }

    public List<String> getCrossRefIDsByDatabase(DatabaseSource source) {
        return getCrossRefIDsByDatabase(source.getValue());
    }

    /**
     * @return a list of cross references that are EC numbers
     * This is a special case since the uniprot file puts the EC number in the description
     */
    public Collection<CrossRefAdapter> getECCrossReferences() {
        String description = originalRichSequence.getDescription();

        Pattern pattern = Pattern.compile("^\\s+EC=(.*);", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(description);

        List<CrossRefAdapter> results = new ArrayList<>();

        while (matcher.find()) {
            String ecnumberLine = matcher.group(1);
            String ecnumber = "";

            if (ecnumberLine.contains("{")) {  // EC=2.7.4.6 {ECO:0000256|RuleBase:RU004013};
                String[] ecLineSplit = ecnumberLine.split("\\{");
                String ecWithBracket = ecLineSplit[0].trim();  // trim() will remove trailing and leading spaces

                if (ecWithBracket.matches("[\\d\\.\\-]*")) {
                    ecnumber = ecWithBracket;
                }
            } else {  // EC=2.7.8.2;
                if (ecnumberLine.matches("[\\d\\.\\-]*")) {
                    ecnumber = ecnumberLine;
                }
            }

            if (!ecnumber.isEmpty()) {
                results.add(CrossRefAdapter.create("EC", ecnumber));
            }
        }
        return results;
    }

    public List<String> getComments() {
        return originalRichSequence
                .getComments()
                .stream()
                .map(comment -> ((org.biojavax.Comment)comment).getComment().replaceFirst("\\-\\!\\- ", ""))
                .toList();
    }

    public String toUniProtFormat() {
        return DatFileWriter.sequenceToString(this);
    }
}
