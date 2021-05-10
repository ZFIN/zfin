package org.zfin.sequence.blast.results.view;

import org.zfin.sequence.blast.results.Hit;
import org.zfin.sequence.blast.results.Hsp;

import java.util.Comparator;
import java.util.List;

/**
 * Comparator sorts by the highest scoring hsp.
 */
public class BlastOutputHitComparator implements Comparator<Hit> {

    public int compare(Hit o1, Hit o2) {
        return getHighScore(o2)- getHighScore(o1) ;
    }

    public int getHighScore(Hit hit){
        int highScore = 0 ;
        List<Hsp> hsps1 = hit.getHitHsps().getHsp() ;
        for(Hsp hsp : hsps1){
            int score = Integer.parseInt(hsp.getHspScore()) ;
            if(score >highScore){
                highScore = score ;
            }
        }
        return highScore ;
    }

}
