package org.zfin.marker;

import jakarta.persistence.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import java.util.Set;

/**
 * This class maps to the marker_type_group table.
 */
@Entity
@Table(name = "marker_type_group")
@Immutable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class MarkerTypeGroup {

    @Id
    @Column(name = "mtgrp_name")
    private String name;

    @Column(name = "mtgrp_display_name")
    private String displayName;

    @Column(name = "mtgrp_comments")
    private String comment;

    @Column(name = "mtgrp_searchable")
    private Boolean searchable;

    @ElementCollection
    @CollectionTable(name = "marker_type_group_member", joinColumns = @JoinColumn(name = "mtgrpmem_mrkr_type_group"))
    @Column(name = "mtgrpmem_mrkr_type")
    private Set<String> typeStrings;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getSearchable() {
        return searchable;
    }

    public void setSearchable(Boolean searchable) {
        this.searchable = searchable;
    }

    public Set<String> getTypeStrings() {
        return typeStrings;
    }

    public void setTypeStrings(Set<String> typeStrings) {
        this.typeStrings = typeStrings;
    }

    public boolean hasType(Marker.Type type) {
        if (type == null)
            return false;
        return typeStrings.stream()
                .anyMatch(name -> name.equals(type.name()));
    }
}
