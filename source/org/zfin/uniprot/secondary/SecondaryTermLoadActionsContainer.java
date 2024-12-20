package org.zfin.uniprot.secondary;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.zfin.uniprot.dto.UniProtLoadSummaryListDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class SecondaryTermLoadActionsContainer {

    private Long releaseID;
    private Date creationDate;
    private UniProtLoadSummaryListDTO summary;
    private Map<String, String> uniprotDatFile;

    private List<SecondaryTermLoadAction> actions = new ArrayList<>();

}
