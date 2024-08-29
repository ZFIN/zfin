package org.zfin.sequence.load;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data()
public class EnsemblLoadSummaryItemDTO {
    String description;
    Map<String, Long> counts = new HashMap<>();

}
