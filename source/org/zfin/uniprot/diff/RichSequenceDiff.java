package org.zfin.uniprot.diff;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.biojavax.CrossRef;

import java.util.*;
import java.util.stream.Collectors;

import org.biojavax.Note;
import org.biojavax.RankedCrossRef;
import org.biojavax.bio.seq.RichSequence;
import org.zfin.uniprot.adapter.RichSequenceAdapter;


@Getter
@Setter
public class RichSequenceDiff {
    private List<CrossRef> addedCrossRefs;
    private List<CrossRef> removedCrossRefs;

    private List<Note> addedKeywords;
    private List<Note> removedKeywords;

    private RichSequenceAdapter oldSequence;
    private RichSequenceAdapter newSequence;

    private String accession;

    public RichSequenceDiff(RichSequenceAdapter oldSequence, RichSequenceAdapter newSequence) {
        if (oldSequence == null || newSequence == null) {
            throw new IllegalArgumentException("Both sequences must be non-null");
        }
        if (!oldSequence.getAccession().equals(newSequence.getAccession())) {
            throw new IllegalArgumentException("Sequences must have same accession");
        }
        this.setAccession(oldSequence.getAccession());
        this.setOldSequence(oldSequence);
        this.setNewSequence(newSequence);
        this.populateDiffs();
    }

    public boolean hasChanges() {
        return !addedCrossRefs.isEmpty() || !removedCrossRefs.isEmpty() ||
                !addedKeywords.isEmpty() || !removedKeywords.isEmpty();
    }

    public static RichSequenceDiff create(RichSequenceAdapter oldSequence, RichSequenceAdapter newSequence) {
        RichSequenceDiff diff = new RichSequenceDiff(oldSequence, newSequence);
        return diff;
    }

    private void populateDiffs() {
        this.setRemovedCrossRefs(crossRefsInFirstSeqOnly(oldSequence, newSequence));
        this.setAddedCrossRefs(crossRefsInFirstSeqOnly(newSequence, oldSequence));

        this.setRemovedKeywords(keywordsInFirstSeqOnly(oldSequence, newSequence));
        this.setAddedKeywords(keywordsInFirstSeqOnly(newSequence, oldSequence));
    }

    private List<CrossRef> crossRefsInFirstSeqOnly(RichSequenceAdapter seq1, RichSequenceAdapter seq2) {

        //convert the ranked crossrefs to a list of crossrefs
        Set<CrossRef> xrefs1 = (Set<CrossRef>)seq1.getRankedCrossRefs().stream().map(xref -> ((RankedCrossRef)xref).getCrossRef()).collect(Collectors.toSet());
        Set<CrossRef> xrefs2 = (Set<CrossRef>)seq2.getRankedCrossRefs().stream().map(xref -> ((RankedCrossRef)xref).getCrossRef()).collect(Collectors.toSet());

        //create a map, indexed by xref.dbname and xref.accession
        //if the same xref is in both lists, then remove it from the map
        //at the end, the map will contain only xrefs that are in seq1 but not in seq2
        //return the values of the map
        Map<String, CrossRef> mappedXrefs1 = xrefs1.stream()
                .collect(Collectors.toMap(xref -> xref.getDbname() + xref.getAccession(), xref -> xref));
        List<String> keysToRemove = xrefs2.stream().map(xref -> xref.getDbname() + xref.getAccession()).toList();

        mappedXrefs1.keySet().removeAll(keysToRemove);

        return new ArrayList<>(mappedXrefs1.values());
    }

    private List<Note> keywordsInFirstSeqOnly(RichSequenceAdapter seq1, RichSequenceAdapter seq2) {
        List<Note> keywords1 = seq1.getKeywordNotes();
        List<Note> keywords2 = seq2.getKeywordNotes();
        List<Note> keywordsInFirstSeqOnly = new ArrayList<>();
        for (Note keyword : keywords1) {
            if (!keywords2.contains(keyword)) {
                keywordsInFirstSeqOnly.add(keyword);
            }
        }
        return keywordsInFirstSeqOnly;
    }

    public boolean hasChangesInDB(String refSeq) {
        for(CrossRef xref : addedCrossRefs) {
            if(xref.getDbname().equals(refSeq)) {
                return true;
            }
        }
        for(CrossRef xref : removedCrossRefs) {
            if(xref.getDbname().equals(refSeq)) {
                return true;
            }
        }
        return false;
    }
}
