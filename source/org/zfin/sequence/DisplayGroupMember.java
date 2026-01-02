package org.zfin.sequence;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "foreign_db_contains_display_group_member")
public class DisplayGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fdbcdgm_pk_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fdbcdgm_fdbcont_zdb_id", nullable = false)
    private ReferenceDatabase referenceDatabase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fdbcdgm_group_id", nullable = false)
    @JsonView(View.SequenceDetailAPI.class)
    private DisplayGroup displayGroup;

    @Column(name = "fdbcdgm_can_view")
    private boolean canView;

    @Column(name = "fdbcdgm_can_add")
    private boolean canAdd;

    @Column(name = "fdbcdgm_can_delete")
    private boolean canDelete;

    @Column(name = "fdbcdgm_can_edit")
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
