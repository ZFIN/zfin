package org.zfin.sequence.load;

import lombok.Data;

@Data
public class LoadSummaryItemDTO {
    String description;
    Long beforeLoadCount;
    Long afterLoadCount;
}
