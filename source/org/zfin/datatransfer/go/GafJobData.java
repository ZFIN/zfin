package org.zfin.datatransfer.go;

import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.util.FileUtil;

import java.util.*;

/**
 * Data associated with a GafJob.
 */
public class GafJobData {
    private Set<MarkerGoTermEvidence> newEntries = new LinkedHashSet<MarkerGoTermEvidence>();
    private List<GafJobEntry> existingEntries = new ArrayList<GafJobEntry>();
    private List<GafJobEntry> removedEntries = new ArrayList<GafJobEntry>();
    private List<GafValidationError> errors = new ArrayList<GafValidationError>();
    private int gafEntryCount = 0;
    private long startTime;
    private long stopTime;


    public void setGafEntryCount(int gafEntryCount) {
        this.gafEntryCount = gafEntryCount;
    }

    public void addNewEntry(MarkerGoTermEvidence entry) {
        newEntries.add(entry);
    }

    public void addExistingEntry(MarkerGoTermEvidence entry) {
        existingEntries.add(new GafJobEntry(entry));
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

    public List<GafJobEntry> getRemovedEntries() {
        return removedEntries;
    }

    @Override
    public String toString() {
        return "Gaf Entries " + FileUtil.LINE_SEPARATOR +
                "processed: " + gafEntryCount + " gaf entries " + FileUtil.LINE_SEPARATOR +
                "added: " + newEntries.size() + "" + FileUtil.LINE_SEPARATOR +
                "removed: " + removedEntries.size() + "" + FileUtil.LINE_SEPARATOR +
                "errors: " + errors.size() + "" + FileUtil.LINE_SEPARATOR +
                "existing: " + existingEntries.size() + "" + FileUtil.LINE_SEPARATOR +
                "time: " + (stopTime - startTime) / (1000f) + " seconds" + FileUtil.LINE_SEPARATOR
                ;
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
}
