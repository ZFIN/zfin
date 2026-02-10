package org.zfin.uniprot;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NcbiGeneSymbolMatchRow {
    private String ncbiId;
    private String symbol;
    private String mrkrZdbId;
    private String ncbiPredictedZdbId;
    private String zdbIdsMatch;

    public List<String> toList() {
        return List.of(
                ncbiId == null ? "" : ncbiId,
                symbol == null ? "" : symbol,
                mrkrZdbId == null ? "" : mrkrZdbId,
                ncbiPredictedZdbId == null ? "" : ncbiPredictedZdbId,
                zdbIdsMatch == null ? "" : zdbIdsMatch
        );
    }
}
