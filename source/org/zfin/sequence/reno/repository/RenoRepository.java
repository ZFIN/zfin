/**
 *  Interface RenoRepository.
 */
package org.zfin.sequence.reno.repository;

import org.zfin.people.Person;
import org.zfin.sequence.reno.NomenclatureRun;
import org.zfin.sequence.reno.RedundancyRun;
import org.zfin.sequence.reno.Run;
import org.zfin.sequence.reno.RunCandidate;

import java.util.List;

public interface RenoRepository {

    List<RedundancyRun> getRedundancyRuns();

    List<NomenclatureRun> getNomenclatureRuns();

    int getQueueCandidateCount(Run run);

    int getPendingCandidateCount(Run run);

    int getFinishedCandidateCount(Run run);

    Run castRun(Run run);


    boolean lock(Person p, RunCandidate rc);

    boolean unlock(Person p, RunCandidate rc);

    Run getRunByID(String runID);

    RunCandidate getRunCandidateByID(String runCandidateID);

    List<RunCandidate> getSangerRunCandidatesInQueue(Run run);

    List<RunCandidate> getSortedRunCandidates(Run run, String comparator, int maxNumOfRecords);

    List<RunCandidate> getSortedNonZFRunCandidates(Run run, String comparator, int maxNumOfRecords);

    List<RunCandidate> getPendingCandidates(Run run);

}
