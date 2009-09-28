package org.zfin.sequence.blast.results.view;

/**
 */
public class AlignmentLine {

    public final static int PADDING = 5 ;

    private String queryStrand ;
    private String midlineStrand;
    private String hitStrand ;
    private int startQuery ;
    private int stopQuery ;
    private int startHit ;
    private int stopHit ;

    public String getQueryStrand() {
        return queryStrand;
    }

    public void setQueryStrand(String queryStrand) {
        this.queryStrand = queryStrand;
    }

    public String getMidlineStrand() {
        return midlineStrand;
    }

    public void setMidlineStrand(String midlineStrand) {
        this.midlineStrand = midlineStrand;
    }

    public String getHitStrand() {
        return hitStrand;
    }

    public void setHitStrand(String hitStrand) {
        this.hitStrand = hitStrand;
    }

    public int getStartQuery() {
        return startQuery;
    }

    public void setStartQuery(int startQuery) {
        this.startQuery = startQuery;
    }

    public int getStopQuery() {
        return stopQuery;
    }

    public void setStopQuery(int stopQuery) {
        this.stopQuery = stopQuery;
    }

    public int getStartHit() {
        return startHit;
    }

    public void setStartHit(int startHit) {
        this.startHit = startHit;
    }

    public int getStopHit() {
        return stopHit;
    }

    public void setStopHit(int stopHit) {
        this.stopHit = stopHit;
    }

    public String getStartQueryString() {
        return padTo(String.valueOf(startQuery),PADDING) ;
    }

    public String getStopQueryString() {
        return padTo(String.valueOf(stopQuery),PADDING) ;
    }

    public String getStartHitString() {
        return padTo(String.valueOf(startHit),PADDING) ;
    }

    public String getStopHitString() {
        return padTo(String.valueOf(stopHit),PADDING) ;
    }
        /**
     * Ensures the given string is the correct length.
     *
     */
    private String padTo( String poString, int numberOfChars) {
        int toPad = numberOfChars - poString.length() ;
        for(int i = 0 ; i < toPad ; i++){
            poString += " " ;
        }
        return poString ;
    }

}
