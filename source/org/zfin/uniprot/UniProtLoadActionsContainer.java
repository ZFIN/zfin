package org.zfin.uniprot;

import lombok.Builder;
import lombok.Getter;
import org.zfin.uniprot.dto.UniProtLoadSummaryDTO;

import java.util.Set;

@Builder
@Getter
public class UniProtLoadActionsContainer {

    private final UniProtLoadSummaryDTO summary;
    private final Set<UniProtLoadAction> actions;

}
