package org.zfin.sequence.reno.presentation;

import org.zfin.sequence.reno.*;

import java.util.List;
import java.util.Collections;

public class RunBean {

    public static final int MAX_NUM_OF_RECORDS = 40;
    public static final String SORT_BY_OCCURRENCE_ASC = "occurrenceAsc";
    public static final String SORT_BY_OCCURRENCE_DSC = "occurrenceDsc";
    public static final String SORT_BY_LASTDONE_ASC = "lastDoneAsc";
    public static final String SORT_BY_LASTDONE_DSC = "lastDoneDsc";
    public static final String ORTHOLOGY_PUBLICATION_ZDB_ID = "orthologyPublicationZdbID";
    public static final String NOMENCLATURE_PUBLICATION_ZDB_ID = "nomenclaturePublicationZdbID";
    public static final String RELATION_PUBLICATION_ZDB_ID = "relationPublicationZdbID";
    public static final String FINISH_REMAINDER = "finish-remainder";

    private List<RedundancyRun> redundancyRuns;
    private List<NomenclatureRun> nomenclatureRuns;

    private Run run;
    private RunCandidate runCandidate;
    private List<RunCandidate> runCandidates;
    private String zdbID;
    private String nomenclaturePublicationZdbID;   // called nomenclature in nomenclature and link in
    private String orthologyPublicationZdbID;  // used by nomenclature only
    private String relationPublicationZdbID; // attribute used by redundancy only
    private String comparator;
    private String action ;

    public List<RedundancyRun> getRedundancyRuns() {
        return redundancyRuns;
    }

    public void setRedundancyRuns(List<RedundancyRun> redundancyRuns) {
        this.redundancyRuns = redundancyRuns;
    }

    public List<NomenclatureRun> getNomenclatureRuns() {
        return nomenclatureRuns;
    }

    public void setNomenclatureRuns(List<NomenclatureRun> nomenclatureRuns) {
        this.nomenclatureRuns = nomenclatureRuns;
    }

    public List<RunCandidate> getRunCandidates() {
        //sortRunCandidates(comparator);
        return runCandidates;
    }

    public void setRunCandidates(List<RunCandidate> runCandidates) {
        this.runCandidates = runCandidates;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Run getRun() {
        return run;
    }

    public void setRun(Run run) {
        this.run = run;
    }

    public RunCandidate getRunCandidate() {
        return runCandidate;
    }

    public void setRunCandidate(RunCandidate runCandidate) {
        this.runCandidate = runCandidate;
    }


    public String getRelationPublicationZdbID() {
        return relationPublicationZdbID;
    }

    public void setRelationPublicationZdbID(String relationPublicationZdbID) {
        this.relationPublicationZdbID = relationPublicationZdbID;
    }

    public String getOrthologyPublicationZdbID() {
        return orthologyPublicationZdbID;
    }

    public void setOrthologyPublicationZdbID(String orthologyPublicationZdbID) {
        this.orthologyPublicationZdbID = orthologyPublicationZdbID;
    }

    public String getNomenclaturePublicationZdbID() {
        return nomenclaturePublicationZdbID;
    }

    public void setNomenclaturePublicationZdbID(String nomenclaturePublicationZdbID) {
        this.nomenclaturePublicationZdbID = nomenclaturePublicationZdbID;
    }

  /**
   * This method sorts the results in the
   * <code>runCandidates</code> property of this
   * <code>RunBean</code> object.  If the
   * <code>runCandidates</code> property is null, this
   * method returns false.
   *
   * @param propertyName Property to sort by
   * @return <code>true</code> if and only if the
   *         <code>runCandidates</code> property is
   *         successfully sorted.
   */
  public boolean sortRunCandidates( String propertyName ) {
    if ( runCandidates == null ) {
      return false;
    } else if ( propertyName.equals( "name" )) {
       RunCandidateComparatorByName c = new RunCandidateComparatorByName();
       Collections.sort ( runCandidates, c);
       return true;
    } else {
      return false;
    }
  }

    public String getComparator() {
        return comparator;
    }

    public void setComparator(String comparator) {
        this.comparator = comparator;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
