package org.zfin.uniprot.diff;

import lombok.Getter;
import lombok.Setter;
import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.*;

@Getter
@Setter
public class UniProtDiffSet {

    private List<RichSequenceAdapter> addedSequences = new ArrayList<>();

    private List<RichSequenceAdapter> removedSequences = new ArrayList<>();

    private List<RichSequenceDiff> changedSequences = new ArrayList<>();


    private Date latestUpdateFromSequence1 = new Date(0);
    private Date latestUpdateFromSequence2 = new Date(0);

    public UniProtDiffSet() {
    }

    public void addNewSequence(RichSequenceAdapter sequence) {
        addedSequences.add(sequence);
    }

    public void addRemovedSequence(RichSequenceAdapter sequence) {
        removedSequences.add(sequence);
    }

    public void addChangedSequence(RichSequenceDiff sequence) {
        changedSequences.add(sequence);
    }

    public UniProtDiffSetSummary getSummary() {
        return new UniProtDiffSetSummary(
                addedSequences.size(),
                removedSequences.size(),
                changedSequences.size(),
                addedSequences.size() + removedSequences.size() + changedSequences.size(),
                (int) changedSequences.stream().filter(s -> s.hasChangesInDB("RefSeq")).count(),
                (int) changedSequences.stream().filter(s -> s.hasChangesInDB("ZFIN")).count(),
                (int) changedSequences.stream().filter(s -> s.hasChangesInDB("GeneID")).count(),
                new java.text.SimpleDateFormat("yyyy-MM-dd").format(latestUpdateFromSequence1),
                new java.text.SimpleDateFormat("yyyy-MM-dd").format(latestUpdateFromSequence2)
        );
    }

    public void updateLatestDate1(RichSequenceAdapter seq) {
        updateMostRecentDate(latestUpdateFromSequence1, seq);
    }

    public void updateLatestDate2(RichSequenceAdapter seq) {
        updateMostRecentDate(latestUpdateFromSequence2, seq);
    }

    private void updateMostRecentDate(Date latestDateFoundSoFar, RichSequenceAdapter seq) {
        Date dateUpdated = seq.getDateUpdated();
        updateMostRecentDate(latestDateFoundSoFar, dateUpdated);
    }

    private void updateMostRecentDate(Date latestDateFoundSoFar, Date dateUpdated) {
        if (dateUpdated == null) {
            return;
        }
        if (dateUpdated.after(latestDateFoundSoFar)) {
            latestDateFoundSoFar.setTime(dateUpdated.getTime());
        }
    }
}
