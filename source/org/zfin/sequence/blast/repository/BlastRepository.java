/**
 *  Class BlastRepository.
 */
package org.zfin.sequence.blast.repository ;

import org.zfin.sequence.blast.Hit;
import org.zfin.sequence.reno.RunCandidate;

public interface BlastRepository {
    
     public Hit getBestHit (RunCandidate rc);
} 


