package org.zfin.sequence.blast.results.view;

import org.apache.log4j.Logger;
import org.zfin.sequence.blast.BlastQueryJob;
import org.zfin.sequence.blast.results.BlastOutput;
import org.zfin.sequence.blast.results.Hit;
import org.zfin.sequence.blast.results.HitNum;
import org.zfin.sequence.blast.results.Iteration;
import org.zfin.sequence.blast.results.impl.HitNumImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This class merges blast output into a single result.
 * For use with distributed queries.
 */
public class BlastOutputMerger {

    private final static Logger logger = Logger.getLogger(BlastOutputMerger.class);

    public static BlastOutput mergeBlastOutput(Collection<BlastQueryJob> blastSlices) {

        BlastOutput blastOutput = null;

        for (BlastQueryJob blastSliceThread : blastSlices) {
            if (blastOutput == null) {
                blastOutput = blastSliceThread.getXmlBlastBean().getBlastOutput();
            } else {
                blastOutput = mergeBlastOutput(blastOutput, blastSliceThread.getXmlBlastBean().getBlastOutput());
            }
        }

        blastOutput = sortHits(blastOutput);

        return blastOutput;

    }

    private static BlastOutput mergeBlastOutput(BlastOutput blastOutputA, BlastOutput blastOutputB) {
        blastOutputA = mergeBlastHits(blastOutputA, blastOutputB);
        blastOutputA = mergeBlastErrors(blastOutputA, blastOutputB);
        return blastOutputA;
    }

    private static BlastOutput mergeBlastErrors(BlastOutput blastOutputA, BlastOutput blastOutputB) {
        String errorA = null;
        String errorB = null;
        try {
            errorA = blastOutputA.getZFINParameters().getErrorData().getContent();
        } catch (NullPointerException e) {
            logger.debug("no error for blastA output:" + blastOutputA);
        }

        try {
            errorB = blastOutputB.getZFINParameters().getErrorData().getContent();
        } catch (NullPointerException e) {
            logger.debug("no error for blastB output:" + blastOutputB);
        }

        // if neither is null,then set as normal
        if (errorA != null && errorB != null) {
            errorA += " " + errorB;
        } else
            // if we have no error on A, but we do have some on B, then we just replace them
            if (errorA == null && errorB != null) {
                errorA = errorB;
            }
        // if no error on B, then there is nothing to do


        return blastOutputA;
    }

    // this method only merges hits
    //

    /**
     * This method merges hits.  There will only be one iteration, because we are splitting queries.
     *
     * @param blastOutputA First blast output.
     * @param blastOutputB Second blast ouput.
     * @return Merged blastoutput file.
     */
    public static BlastOutput mergeBlastHits(BlastOutput blastOutputA, BlastOutput blastOutputB) {
        List<Hit> hitsA = null;
        List<Hit> hitsB = null;
        try {
            hitsA = ((List<Iteration>) blastOutputA.getBlastOutputIterations().getIteration()).get(0).getIterationHits().getHit();
        } catch (NullPointerException e) {
            logger.warn("no hits for blastA output:" + blastOutputA);
        }

        try {
            hitsB = ((List<Iteration>) blastOutputB.getBlastOutputIterations().getIteration()).get(0).getIterationHits().getHit();
        } catch (NullPointerException e) {
            logger.warn("no hits for blastB output:" + blastOutputB);
        }

        // if neither is null,then set as normal
        if (hitsA != null && hitsB != null) {
            hitsA.addAll(hitsB);
        } else
            // if we have no hits on A, but we do have some on B, then we just replace them
            if (hitsA == null && hitsB != null) {
                hitsA = hitsB;
            }
        // if no hits on B, then there is nothing to do


        return blastOutputA;
    }

    public static BlastOutput sortHits(BlastOutput blastOutput) {
        if (blastOutput == null) return null;

        try {
            List<Hit> hits = ((List<Iteration>) blastOutput.getBlastOutputIterations().getIteration()).get(0).getIterationHits().getHit();
            Collections.sort(hits, new BlastOutputHitComparator());
            for (int hitNumber = 0; hitNumber < hits.size(); hitNumber++) {
                HitNum hitNum = new HitNumImpl();
                hitNum.setContent(String.valueOf(hitNumber + 1));
                hits.get(hitNumber).setHitNum(hitNum);
            }
        } catch (Exception e) {
            logger.error("Failed to sort hits", e);
        }
        return blastOutput;
    }

}
