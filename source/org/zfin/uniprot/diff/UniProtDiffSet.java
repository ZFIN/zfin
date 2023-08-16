package org.zfin.uniprot.diff;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.biojavax.bio.seq.RichSequence;
import org.zfin.uniprot.serialize.UniProtDiffSetSerializer;

import java.util.*;

import static org.zfin.uniprot.UniProtTools.getDateUpdatedFromNotes;

@Getter
@Setter
@JsonSerialize(using = UniProtDiffSetSerializer.class)
public class UniProtDiffSet {

    private List<RichSequence> addedSequences = new ArrayList<>();

    private List<RichSequence> removedSequences = new ArrayList<>();

    private List<RichSequenceDiff> changedSequences = new ArrayList<>();


    private Date latestUpdateFromSequence1 = new Date(0);
    private Date latestUpdateFromSequence2 = new Date(0);

    public UniProtDiffSet(List<RichSequence> addedSequences, List<RichSequence> removedSequences, List<RichSequenceDiff> changedSequences) {
        this.addedSequences = addedSequences;
        this.removedSequences = removedSequences;
        this.changedSequences = changedSequences;
    }

    public UniProtDiffSet() {
    }

    public boolean hasChanges() {
        return !addedSequences.isEmpty() || !removedSequences.isEmpty() || !changedSequences.isEmpty();
    }

    public void addNewSequence(RichSequence sequence) {
        addedSequences.add(sequence);
    }

    public void addRemovedSequence(RichSequence sequence) {
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

    public void updateLatestDate1(RichSequence seq) {
        updateMostRecentDate(latestUpdateFromSequence1, seq);
    }

    public void updateLatestDate2(RichSequence seq) {
        updateMostRecentDate(latestUpdateFromSequence2, seq);
    }

    private void updateMostRecentDate(Date latestDateFoundSoFar, RichSequence seq) {
        Date dateUpdated = getDateUpdatedFromNotes(seq);
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
