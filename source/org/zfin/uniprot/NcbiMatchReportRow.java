package org.zfin.uniprot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NcbiMatchReportRow {
    private String ncbiId;
    private String zdbId;
    private String ensemblId;
    private String symbol;
    private String dblinks;
    private String publications;
    private String rnaAccessions;

    public List<String> toList() {
        return List.of(
                ncbiId == null ? "" : ncbiId,
                zdbId == null ? "" : zdbId,
                ensemblId == null ? "" : ensemblId,
                symbol == null ? "" : symbol,
                dblinks == null ? "" : dblinks,
                publications == null ? "" : publications,
                rnaAccessions == null ? "" : rnaAccessions
        );
    }
}
