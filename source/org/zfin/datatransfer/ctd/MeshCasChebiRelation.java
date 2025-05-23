package org.zfin.datatransfer.ctd;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeshCasChebiRelation {
    private String mesh;
    private String meshName;
    private String cas;
    private String chebi;
    private String chebiName;
}

