package org.zfin.uniprot;

import lombok.Builder;
import lombok.Getter;
import org.zfin.uniprot.dto.UniProtLoadSummaryItemDTO;

import java.util.Set;

@Builder
@Getter
public class UniProtLoadActionsContainer {

    private final UniProtLoadSummaryItemDTO summary;
    private final Set<UniProtLoadAction> actions;

}
