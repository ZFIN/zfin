package org.zfin.publication;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.SortNatural;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "mesh_heading")
public class MeshHeading implements Comparable<MeshHeading> {

    @Id
    @Column(name = "mh_pk_id")
    private Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "majorTopic", column = @Column(name = "mh_descriptor_is_major_topic"))
    })
    @AssociationOverrides({
            @AssociationOverride(name = "term", joinColumns = @JoinColumn(name = "mh_mesht_mesh_descriptor_id"))
    })
    private MeshHeadingTerm descriptor;

    @ElementCollection
    @CollectionTable(name = "mesh_heading_qualifier", joinColumns = @JoinColumn(name = "mhq_mesh_heading_id"))
    @AttributeOverrides({
            @AttributeOverride(name = "majorTopic", column = @Column(name = "mhq_is_major_topic"))
    })
    @AssociationOverrides({
            @AssociationOverride(name = "term", joinColumns = @JoinColumn(name = "mhq_mesht_mesh_qualifier_id"))
    })
    @SortNatural
    private SortedSet<MeshHeadingTerm> qualifiers;

    public List<String> getDisplayList() {
        String base = descriptor.toString();
        List<String> displayList = new ArrayList<>();
        if (CollectionUtils.isEmpty(qualifiers)) {
            displayList.add(base);
        } else {
            for (MeshHeadingTerm qualifier : qualifiers) {
                displayList.add(base + "/" + qualifier.toString());
            }
        }
        return displayList;
    }

    @Override
    public int compareTo(MeshHeading o) {
        return this.descriptor.compareTo(o.getDescriptor());
    }

}
