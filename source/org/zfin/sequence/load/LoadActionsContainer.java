package org.zfin.sequence.load;

import lombok.Builder;
import lombok.Getter;
import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.dto.UniProtLoadSummaryItemDTO;

import java.util.Set;

@Builder
@Getter
public class LoadActionsContainer {

    private final EnsemblLoadSummaryItemDTO summary;
    private final Set<LoadAction> actions;

}
