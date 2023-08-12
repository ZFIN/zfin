package org.zfin.uniprot.diff;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.biojavax.bio.seq.RichSequence;

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

    public Map<String, String> getSummary() {
        Map<String, String> summary = new TreeMap<>();
        summary.put("added", String.valueOf(addedSequences.size()));
        summary.put("removed", String.valueOf(removedSequences.size()));
        summary.put("changed", String.valueOf(changedSequences.size()));


        summary.put("total", String.valueOf(addedSequences.size() + removedSequences.size() + changedSequences.size()));

        //TODO: change "RefSeq", "ZFIN", "GeneID" to constants
        summary.put("changed RefSeq", changedSequences.stream().filter(s -> s.hasChangesInDB("RefSeq")).count() + "");
        summary.put("changed ZFIN", changedSequences.stream().filter(s -> s.hasChangesInDB("ZFIN")).count() + "");
        summary.put("changed GeneID", changedSequences.stream().filter(s -> s.hasChangesInDB("GeneID")).count() + "");

        summary.put("latest update in set 1", new java.text.SimpleDateFormat("dd-MMM-yyyy").format(latestUpdateFromSequence1));
        summary.put("latest update in set 2", new java.text.SimpleDateFormat("dd-MMM-yyyy").format(latestUpdateFromSequence2));

        return summary;
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
