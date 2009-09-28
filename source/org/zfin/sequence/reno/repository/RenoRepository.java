/**
 *  Interface RenoRepository.
 */
package org.zfin.sequence.reno.repository ;

import org.zfin.sequence.reno.Run;
import org.zfin.sequence.reno.RunCandidate;
import org.zfin.sequence.reno.RedundancyRun;
import org.zfin.sequence.reno.NomenclatureRun;
import org.zfin.people.Person;

import java.util.List;

public interface RenoRepository {

    List<RedundancyRun> getRedundancyRuns();
    List<NomenclatureRun> getNomenclatureRuns();
    int getQueueCandidateCount(Run run);
    int getPendingCandidateCount(Run run);
    int getFinishedCandidateCount(Run run);
    Run castRun(Run run) ;


    boolean lock(Person p, RunCandidate rc);
    boolean unlock(Person p, RunCandidate rc);

    Run getRunByID(String runID);
    RunCandidate getRunCandidateByID(String runCandidateID);
    List<RunCandidate> getRunCandidates(String runZdbId);
    List<RunCandidate> getSortedRunCandidates(String runZdbId, String comparator, int maxNumOfRecords);
    List<RunCandidate> getSortedNonZFRunCandidates(String runZdbId, String comparator, int maxNumOfRecords);
    List<RunCandidate> getPendingCandidates(Run run);

}
