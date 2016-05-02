package org.zfin.feature;

import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.Set;


@Entity
@Table(name = "feature_type_group")
@Immutable
public class FeatureTypeGroup {

    @Id
    @Column(name = "ftrgrp_name")
    private String name;
    @Column(name = "ftrgrp_comments")
    private String comment;
    @ElementCollection
    @CollectionTable(name="feature_type_group_member", joinColumns=@JoinColumn(name="ftrgrpmem_ftr_type"))
    @Column(name="ftrgrpmem_ftr_type_group")
    private Set<String> typeStrings;

    public Set<String> getTypeStrings() {
        return typeStrings;
    }

    public void setTypeStrings(Set<String> typeStrings) {
        this.typeStrings = typeStrings;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


}
