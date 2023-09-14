package org.zfin.sequence;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisplayGroupMember {
    private Long id;
    private ReferenceDatabase referenceDatabase;
    private DisplayGroup displayGroup;

    private boolean canRead;
    private boolean canAdd;
    private boolean canDelete;
    private boolean canEdit;

    public ReferenceDatabase getForeignDBContains() {
        return referenceDatabase;
    }

    public void setForeignDBContains(ReferenceDatabase foreignDBContains) {
        this.referenceDatabase = foreignDBContains;
    }

    public DisplayGroup getDisplayGroupName() {
        return displayGroup;
    }

    public void setDisplayGroupName(DisplayGroup displayGroupName) {
        this.displayGroup = displayGroupName;
    }


    public Long getFdbcDisplayGroupMemberID() {
        return id;
    }

    public void setFdbcDisplayGroupMemberID(Long fdbcDisplayGroupMemberID) {
        this.id = fdbcDisplayGroupMemberID;
    }





}
