package org.zfin.sequence;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class DisplayGroupMember {
    private Long id;
    private ReferenceDatabase referenceDatabase;
    @JsonView(View.SequenceDetailAPI.class)
    private DisplayGroup displayGroup;

    private boolean canView;
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


    public Set<String> getPermissions() {
        HashSet<String> permissions = new HashSet<String>();
        if (canView) {
            permissions.add("view");
        }
        if (canAdd) {
            permissions.add("add");
        }
        if (canDelete) {
            permissions.add("delete");
        }
        if (canEdit) {
            permissions.add("edit");
        }
        return permissions;
    }
}
