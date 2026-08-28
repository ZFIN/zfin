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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mgtaeg_mrkrgoev_zdb_id")
    private MarkerGoTermEvidence mgtaegMarkerGoEvidence;

    @OneToMany(mappedBy = "annotExtnGroupID")
    private Set<MarkerGoTermAnnotationExtn> mgtAnnoExtns;

    public void addMgtAnnoExtns(MarkerGoTermAnnotationExtn mgtAnnoExtn) {
        if (this.mgtAnnoExtns == null)
            this.mgtAnnoExtns = new HashSet<>();
        this.mgtAnnoExtns.add(mgtAnnoExtn);
    }

    /**
     * Identity is the database id; a group that has not been persisted is equal only to itself.
     *
     * This previously returned an unconditional {@code true} with no {@code hashCode()} override,
     * which made every group equal to every other one. The missing hashCode is what kept it from
     * doing visible damage most of the time: instances fell back on identity hash codes, so they
     * usually landed in different buckets of the {@code Set<MarkerGoTermAnnotationExtnGroup>} on
     * MarkerGoTermEvidence and never got compared. Whenever two groups *did* collide, though, the
     * set silently discarded the second one -- and collisions stop being unlikely once an
     * annotation carries a lot of groups.
     *
     * Deliberately NOT comparing contents, even though contents are what make two groups
     * semantically the same (that is how migration 0040 deduped them). {@code mgtAnnoExtns} is a
     * lazy {@code @OneToMany}, so a content-based equals/hashCode would force initialization on
     * every set operation and throw LazyInitializationException on a detached instance. It is also
     * mutable via addMgtAnnoExtns(), so a group's hash could change after it was already in a set.
     * Id equality is the safe JPA-entity contract here; deduplication belongs in the load, not in
     * equals().
     *
     * hashCode is constant so it survives the transient -> persistent transition, where the id is
     * assigned by the database. These sets hold a handful of groups, so the lost bucket spread
     * costs nothing.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MarkerGoTermAnnotationExtnGroup that = (MarkerGoTermAnnotationExtnGroup) o;

        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
