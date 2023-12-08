package org.zfin.uniprot.secondary;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SecondaryTermLoadActionsContainer {

    private Long releaseID;
    private Date creationDate;

    private List<SecondaryTermLoadAction> actions = new ArrayList<>();

    public SecondaryTermLoadActionsContainer(Long releaseID, Date creationDate, List<SecondaryTermLoadAction> actions) {
        this.releaseID = releaseID;
        this.creationDate = creationDate;
        this.actions = actions;
    }
}
