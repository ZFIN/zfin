package org.zfin.sequence.blast.results.view;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class HighScoringPair {

    // scores
    private int score ;
    private float bitScore;
    private float expectValue;
    private int identity ;
    private int positive ;

    // strands
    private int hspNumber ;
    private String queryStrand ;
    private String midlineStrand;
    private String hitStrand ;
    private int queryFrom;
    private int queryTo;
    private int hitFrom;
    private int hitTo;
    private int alignmentLength ;
    public final static int DISPLAY_LENGTH = 60 ;

    public int getHitLength(){
        return hitTo - hitFrom ;
    }

    public int getQueryLength(){
        return Math.abs(queryTo - queryFrom) ;
    }

    public float getExpectValue() {
        return expectValue;
    }

    public void setExpectValue(float expectValue) {
        this.expectValue = expectValue;
    }

    public int getHspNumber() {
        return hspNumber;
    }

    public void setHspNumber(int hspNumber) {
        this.hspNumber = hspNumber;
    }

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

    public int getQueryFrom() {
        return queryFrom;
    }

    public void setQueryFrom(int queryFrom) {
        this.queryFrom = queryFrom;
    }

    public int getQueryTo() {
        return queryTo;
    }

    public void setQueryTo(int queryTo) {
        this.queryTo = queryTo;
    }

    public int getHitFrom() {
        return hitFrom;
    }

    public void setHitFrom(int hitFrom) {
        this.hitFrom = hitFrom;
    }

    public int getHitTo() {
        return hitTo;
    }

    public void setHitTo(int hitTo) {
        this.hitTo = hitTo;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public float getBitScore() {
        return bitScore;
    }

    public void setBitScore(float bitScore) {
        this.bitScore = bitScore;
    }

    public float getEValue() {
        return expectValue;
    }

    public void setEValue(float eValue) {
        this.expectValue = eValue;
    }

    public int getIdentity() {
        return identity;
    }

    public void setIdentity(int identity) {
        this.identity = identity;
    }

    public int getPositive() {
        return positive;
    }

    public void setPositive(int positive) {
        this.positive = positive;
    }

    public int getAlignmentLength() {
        return alignmentLength;
    }

    public void setAlignmentLength(int alignmentLength) {
        this.alignmentLength = alignmentLength;
    }

    public List<AlignmentLine> getView(){
        int counter = 0 ;
        int length = queryStrand.length() ;

        List<AlignmentLine> alignmentLines  = new ArrayList<AlignmentLine>();
        boolean reverseStrand = (queryFrom>queryTo);

        if(reverseStrand==false){
            while (counter < length){
                AlignmentLine alignmentLine = new AlignmentLine() ;

                // case 1, there is plenty of length
                if(length-counter > DISPLAY_LENGTH){
                    alignmentLine.setQueryStrand(queryStrand.substring(counter, counter+DISPLAY_LENGTH));
                    alignmentLine.setMidlineStrand(midlineStrand.substring(counter, counter+DISPLAY_LENGTH));
                    alignmentLine.setHitStrand(hitStrand.substring(counter, counter+DISPLAY_LENGTH));
                    alignmentLine.setStartQuery(counter+queryFrom);
                    alignmentLine.setStopQuery(counter+queryFrom+DISPLAY_LENGTH-1);
                    alignmentLine.setStartHit(counter+hitFrom);
                    alignmentLine.setStopHit(counter+hitFrom+DISPLAY_LENGTH-1);

                    counter += DISPLAY_LENGTH;
                }
                // case 2, we are at the end
                else{
                    alignmentLine.setQueryStrand(queryStrand.substring(counter));
                    alignmentLine.setMidlineStrand(midlineStrand.substring(counter));
                    alignmentLine.setHitStrand(hitStrand.substring(counter));

                    alignmentLine.setStartQuery(counter+queryFrom);
                    alignmentLine.setStopQuery(queryTo);
                    alignmentLine.setStartHit(counter+hitFrom);
                    alignmentLine.setStopHit(hitTo);
                    // force it to end now
                    counter = length + 1 ;
                }

                alignmentLines.add(alignmentLine) ;
            }
        }
        else{
            counter = length ;
            while (counter > 0 ){
                AlignmentLine alignmentLine = new AlignmentLine() ;
                // case 1, there is plenty of length
                if(counter > DISPLAY_LENGTH){
                    alignmentLine.setQueryStrand(queryStrand.substring(length-counter, length-counter+DISPLAY_LENGTH));
                    alignmentLine.setMidlineStrand(midlineStrand.substring(length-counter, length-counter+DISPLAY_LENGTH));
                    alignmentLine.setHitStrand(hitStrand.substring(length-counter, length-counter+DISPLAY_LENGTH));
                    alignmentLine.setStartQuery(queryFrom-(length-counter));
                    alignmentLine.setStopQuery(queryFrom-(length-counter)- DISPLAY_LENGTH+1);
                    alignmentLine.setStartHit(hitFrom+(length-counter));
                    alignmentLine.setStopHit(hitFrom+(length-counter)+DISPLAY_LENGTH-1);

                    counter -= DISPLAY_LENGTH;
                }
                // case 2, we are at the end
                else{
                    alignmentLine.setQueryStrand(queryStrand.substring(length-counter));
                    alignmentLine.setMidlineStrand(midlineStrand.substring(length-counter));
                    alignmentLine.setHitStrand(hitStrand.substring(length-counter));

                    alignmentLine.setStartQuery(queryFrom-(length-counter));
                    alignmentLine.setStopQuery(queryTo);
                    alignmentLine.setStartHit(hitFrom+(length-counter));
                    alignmentLine.setStopHit(hitTo);
                    // force it to end now
                    counter = 0 ;
                }

                alignmentLines.add(alignmentLine) ;
            }

        }


        return alignmentLines ;
    }

}
