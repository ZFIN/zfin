package org.zfin.sequence;


public class DisplayGroupMember {
    private Long id;
    private ReferenceDatabase referenceDatabase;
    private DisplayGroup displayGroup;

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
