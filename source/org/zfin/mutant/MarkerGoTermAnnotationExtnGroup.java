package org.zfin.mutant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "marker_go_term_annotation_extension_group")
public class MarkerGoTermAnnotationExtnGroup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mgtaeg_annotation_extension_group_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mgtaeg_mrkrgoev_zdb_id")
    private MarkerGoTermEvidence mgtaegMarkerGoEvidence;

    @OneToMany(mappedBy = "annotExtnGroupID")
    private Set<MarkerGoTermAnnotationExtn> mgtAnnoExtns;

    public void addMgtAnnoExtns(MarkerGoTermAnnotationExtn mgtAnnoExtn) {
        if (this.mgtAnnoExtns == null)
            this.mgtAnnoExtns = new HashSet<>();
        this.mgtAnnoExtns.add(mgtAnnoExtn);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MarkerGoTermAnnotationExtnGroup that = (MarkerGoTermAnnotationExtnGroup) o;

        return true;
    }

}
