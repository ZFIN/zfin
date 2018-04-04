package org.zfin.datatransfer.go;

import org.apache.commons.lang3.StringUtils;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.util.FileUtil;

import java.util.*;

/**
 * Data associated with a GafJob.
 */
public class GafJobData {
    private Set<MarkerGoTermEvidence> newEntries = new LinkedHashSet<>();
    private Set<MarkerGoTermEvidence> updateEntries = new LinkedHashSet<>();
    private List<GafJobEntry> existingEntries = new ArrayList<>();
    private List<GafJobEntry> removedEntries = new ArrayList<>();
    private List<GafEntry> cellEntries = new ArrayList<>();
    private List<GafEntry> subsetFailureEntries = new ArrayList<>();
    private List<GafValidationError> errors = new ArrayList<>();
     private int gafEntryCount = 0;
    private int infPipeCount=0;
    private int infCommaCount=0;
    private int infBothCount=0;
    private long startTime;
    private long stopTime;


    public void setGafEntryCount(int gafEntryCount) {
        this.gafEntryCount = gafEntryCount;
    }

    public void addNewEntry(MarkerGoTermEvidence entry) {
        newEntries.add(entry);
    }

    public void addUpdateEntry(MarkerGoTermEvidence entry) {
        updateEntries.add(entry);
    }

    public void addExistingEntry(MarkerGoTermEvidence entry) {
        existingEntries.add(new GafJobEntry(entry));
    }

    public void addCellEntry(GafEntry entry) {
        cellEntries.add(entry);
    }


    public void addSubsetFailureEntry(GafEntry entry) {
        subsetFailureEntries.add(entry);
    }

    public void addRemoved(MarkerGoTermEvidence markerGoTermEvidence) {
        removedEntries.add(new GafJobEntry(markerGoTermEvidence));
    }

    public void addError(GafValidationError gafValidationError) {
        errors.add(gafValidationError);
    }

    public List<GafValidationError> getErrors() {
        Collections.sort(errors);
        return errors;
    }

    public Set<MarkerGoTermEvidence> getNewEntries() {
        return newEntries;
    }

    public Set<MarkerGoTermEvidence> getUpdateEntries() {
        return updateEntries;
    }

    public List<GafJobEntry> getRemovedEntries() {
        return removedEntries;
    }
    public int getInfPipeCount() {
        return infPipeCount;
    }

    public void setInfPipeCount(int infPipeCount) {
        this.infPipeCount = infPipeCount;
    }

    public int getInfCommaCount() {
        return infCommaCount;
    }

    public int getInfBothCount() {
        return infBothCount;
    }

    public void setInfBothCount(int infBothCount) {
        this.infBothCount = infBothCount;
    }

    public void setInfCommaCount(int infCommaCount) {
        this.infCommaCount = infCommaCount;
    }


    @Override
    public String toString() {
        return StringUtils.join(
                new String[] {
                        "Gaf Entries",
                        "processed: " + gafEntryCount + " gaf entries ",

                        "added: " + newEntries.size(),
                        "updated: " + updateEntries.size(),
                        "removed: " + removedEntries.size(),

                        "errors: " + errors.size(),
                        "existing: " + existingEntries.size(),
                        "cell Terms: " + cellEntries.size(),
                        "subset Failures: " + subsetFailureEntries.size(),
                        "col 8 Inferences with Pipes: " + infPipeCount,
                        "col 8 Inferences with Commas: " + infCommaCount,
                        "col 8 Inferences with Both: " + infBothCount,
                        "time: " + (stopTime - startTime) / (1000f) + " seconds"},
                FileUtil.LINE_SEPARATOR);
    }

    public void markStartTime() {
        startTime = System.currentTimeMillis();
    }

    public void markStopTime() {
        stopTime = System.currentTimeMillis();
    }

    public List<GafJobEntry> getExistingEntries() {
        return existingEntries;
    }

    public List<GafEntry> getCellEntries() {
        return cellEntries;
    }
}
