package org.zfin.uniprot.adapter;

import org.biojavax.Note;
import org.biojavax.RankedCrossRef;
import org.biojavax.SimpleRichAnnotation;
import org.biojavax.bio.seq.RichSequence;
import org.zfin.uniprot.datfiles.UniProtFormatZFIN;

import java.text.SimpleDateFormat;
import java.util.*;

public class RichSequenceAdapter {
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


    public List<Note> getKeywordNotes() {
        SimpleRichAnnotation seq1NoteSet = new SimpleRichAnnotation();
        seq1NoteSet.setNoteSet(this.getNoteSet());
        Note[] keywords = seq1NoteSet.getProperties(RichSequence.Terms.getKeywordTerm());
        return List.of(keywords);
    }

    private Set<Note> getNoteSet() {
        return this.wo().getNoteSet();
    }

}
