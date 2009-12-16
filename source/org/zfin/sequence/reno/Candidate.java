/**
 *  Class Candidate.
 */
package org.zfin.sequence.reno;

import java.util.Date;

/**
 */
public class Candidate {
    private String zdbID;
    /**
     * private Accession accession;
     */
    private String note;
    private boolean problem;
    private Integer runCount;
    private Date lastFinishedDate;
    private String markerType;
//    private Marker identifiedMarker;
    private String suggestedName;

    /**
     * Get the "name" of the Candidate.
     * If the suggestedName is not null, return it as the candidate name;
     * Otherwise, will need to go to its RunCandiate (not mapped yet), and
     * get the best hit from all of its queries and use that Marker's name.
     * Currently it is believed that suggestedName is always populated.
     *
     * @return a <code>String</code> object.
     */
    public String getName() {
        if (suggestedName != null) {
            return suggestedName;
        } else {
            return "no suggested name see FB:2070";
        }
    }


    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isProblem() {
        return problem;
    }

    public void setProblem(boolean problem) {
        this.problem = problem;
    }

    public Integer getRunCount() {
        return runCount;
    }

    public void setRunCount(Integer runCount) {
        this.runCount = runCount;
    }

    public Date getLastFinishedDate() {
        return lastFinishedDate;
    }

    public void setLastFinishedDate(Date lastFinishedDate) {
        this.lastFinishedDate = lastFinishedDate;
    }

//    public Marker getIdentifiedMarker() {
//        return identifiedMarker;
//    }
//
//    public void setIdentifiedMarker(Marker identifiedMarker) {
//        this.identifiedMarker = identifiedMarker;
//    }

    /**
     * Get markerType.
     *
     * @return markerType as String.
     */
    public String getMarkerType() {
        return markerType;
    }

    /**
     * Set markerType.
     *
     * @param markerType the value to set.
     */
    public void setMarkerType(String markerType) {
        this.markerType = markerType;
    }

    public String getSuggestedName() {
        return suggestedName;
    }

    public void setSuggestedName(String suggestedName) {
        this.suggestedName = suggestedName;
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();
        sb.append("zdbID: ").append(zdbID);
        sb.append(newline);
        sb.append("Suggested name: ").append(suggestedName);
        sb.append(newline);
        sb.append("Marker type: ").append(markerType);
        sb.append(newline);
        sb.append("Date last finished: ").append(lastFinishedDate);
        sb.append(newline);
        sb.append("Problem: ").append(problem);
        sb.append(newline);
        sb.append("Note: ").append(note);
        return sb.toString();
    }
}


