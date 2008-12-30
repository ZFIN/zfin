/**
 *  Interface RenoRepository.
 */
package org.zfin.sequence.reno.repository ;

import org.zfin.sequence.reno.Run;
import org.zfin.sequence.reno.RunCandidate;
import org.zfin.sequence.reno.RedundancyRun;
import org.zfin.sequence.reno.NomenclatureRun;
import org.zfin.people.Person;
import org.zfin.people.User;

import java.util.List;

public interface RenoRepository {

    List<RedundancyRun> getRedundancyRuns();
    List<NomenclatureRun> getNomenclatureRuns();
    Integer getQueueCandidateCount(Run run);
    Integer getPendingCandidateCount(Run run);
    Integer getFinishedCandidateCount(Run run);
    Run castRun(Run run) ;


    boolean lock(User user, RunCandidate rc);
    boolean unlock(Person p, RunCandidate rc);

    Run getRunByID(String runID);
    RunCandidate getRunCandidateByID(String runCandidateID);
    List<RunCandidate> getRunCandidates(String runZdbId);
    List<RunCandidate> getSortedRunCandidates(String runZdbId, String comparator, int maxNumOfRecords);
    List<RunCandidate> getSortedNonZFRunCandidates(String runZdbId, String comparator, int maxNumOfRecords);
    List<RunCandidate> getPendingCandidates(Run run);

}
