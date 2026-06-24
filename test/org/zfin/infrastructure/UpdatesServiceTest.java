package org.zfin.infrastructure;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.infrastructure.service.UpdatesService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;

/**
 * Verifies that the updates/history page surfaces the update history of old IDs that were merged
 * into the marker being viewed, resolved through zdb_replaced_data (ZFIN-10337). Merge-time
 * behavior is unchanged: updates rows remain stored under their original IDs.
 */
public class UpdatesServiceTest extends AbstractDatabaseTest {

    // ZDB-GENE-001106-12 (among others) was merged into the surviving gene ZDB-GENE-001106-11
    private static final String SURVIVOR = "ZDB-GENE-001106-11";
    private static final String MERGED_AWAY = "ZDB-GENE-001106-12";

    private final UpdatesService updatesService = new UpdatesService();

    @Test
    public void includesMergedAwayHistoryForSurvivor() {
        List<UpdatesDTO> survivorOnly = UpdatesDTO.fromUpdates(
                org.zfin.repository.RepositoryFactory.getInfrastructureRepository().getUpdates(SURVIVOR));
        List<UpdatesDTO> combined = updatesService.getUpdatesDTOS(SURVIVOR, null);

        // combined view must include more rows than the survivor's own audit rows
        assertThat("combined history is larger than survivor-only history",
                combined.size(), greaterThan(survivorOnly.size()));

        Set<String> recordIDs = combined.stream()
                .map(UpdatesDTO::recordID)
                .collect(Collectors.toSet());

        // history attached to the survivor is present...
        assertTrue("survivor history present", recordIDs.contains(SURVIVOR));
        // ...alongside the history of a merged-away id, tagged under its original id
        assertTrue("merged-away history present and tagged under its original id",
                recordIDs.contains(MERGED_AWAY));
    }

    @Test
    public void mergedAwayRowsRemainUnderOriginalId() {
        // No merge-time mutation: the merged-away id still owns its own updates rows directly.
        List<UpdatesDTO> mergedAwayOwn = UpdatesDTO.fromUpdates(
                org.zfin.repository.RepositoryFactory.getInfrastructureRepository().getUpdates(MERGED_AWAY));
        assertThat("merged-away id retains its own update rows", mergedAwayOwn.size(), greaterThan(0));
        assertTrue("rows are stored under the original id",
                mergedAwayOwn.stream().allMatch(dto -> MERGED_AWAY.equals(dto.recordID())));
    }
}
