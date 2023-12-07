package org.zfin.uniprot;

import lombok.Builder;
import org.zfin.uniprot.dto.UniProtLoadSummaryDTO;

import java.util.Set;

@Builder
public class UniProtLoadActionsContainer {

    private final UniProtLoadSummaryDTO summary;
    private final Set<UniProtLoadAction> actions;

}
