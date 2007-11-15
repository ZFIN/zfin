package org.zfin.sequence.reno;

/**
 * Created by IntelliJ IDEA.
 * User: Peiran Song
 * Date: Jul 18, 2007
 * Time: 5:18:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class QueueCandidate {
    private RunCandidate runCandidate;
    private Integer bestScore;
    private Double expectValue;

    public QueueCandidate(RunCandidate rc, Integer bestScore, Double expectValue) {
        this.runCandidate = rc;
        this.bestScore = bestScore;
        this.expectValue = expectValue;
    }

    public RunCandidate getRunCandidate() {
        return runCandidate;
    }

    public void setRunCandidate(RunCandidate runCandidate) {
        this.runCandidate = runCandidate;
    }

    public Integer getBestScore() {
        return bestScore;
    }

    public void setBestScore(Integer bestScore) {
        this.bestScore = bestScore;
    }

    public Double getExpectValue() {
        return expectValue;
    }

    public void setExpectValue(Double expectValue) {
        this.expectValue = expectValue;
    }
}
