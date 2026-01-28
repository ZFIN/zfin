package org.zfin.mapping;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DiscriminatorFormula;
import org.zfin.feature.Feature;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.ZdbID;

/**
 * Entity for linkage members. They can contain any combination of marker abd feature,
 * i.e. there are 4 combinations (until we combine feature and marker in a single table).
 * <p/>
 * Note: The comparable interface is meant for the case where for a given marker/feature
 * a list of linkageMembers are retrieved. The first entity then is the marker/feature
 * in question and the second entity contains the linked (paired) members (marker or feature).
 */
@Entity
@Table(name = "linkage_membership_search")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula(
        "CASE get_obj_type(lms_member_1_zdb_id) " +
        "WHEN 'ALT' THEN " +
        "  CASE get_obj_type(lms_member_2_zdb_id) " +
        "  WHEN 'ALT' THEN 'FeatFeat' " +
        "  ELSE 'FeatMark' " +
        "  END " +
        "ELSE " +
        "  CASE get_obj_type(lms_member_2_zdb_id) " +
        "  WHEN 'ALT' THEN 'MarkFeat' " +
        "  ELSE 'MarkMark' " +
        "  END " +
        "END"
)
@Getter
@Setter
public abstract class LinkageMember implements Cloneable, Comparable<LinkageMember> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lms_pk_id")
    protected long id;

    @Column(name = "lms_lod")
    protected Double lod;

    @Column(name = "lms_distance")
    protected Double distance;

    @Column(name = "lms_units")
    protected String metric;

    @Column(name = "lms_member_1_zdb_id", insertable = false, updatable = false)
    protected String markerOneZdbId;

    @Column(name = "lms_member_2_zdb_id", insertable = false, updatable = false)
    protected String markerTwoZdbId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lms_lnkg_zdb_id")
    protected Linkage linkage;

    // Ensure to set these variables by the sub classes
    // attributes of the second entity
    @Transient
    protected EntityZdbID entityOne;
    @Transient
    protected EntityZdbID entityTwo;

    public EntityZdbID getEntityOne() {
        return entityOne;
    }

    public EntityZdbID getEntityTwo() {
        return entityTwo;
    }

    /**
     * Returns the second marker / feature, named as linked member
     *
     * @return
     */
    abstract public ZdbID getLinkedMember();

    public abstract LinkageMember getInverseMember();

    @Override
    public int compareTo(LinkageMember o) {
        int publicationComparison = linkage.getPublication().getAuthors().compareTo(o.getLinkage().getPublication().getAuthors());
        if (publicationComparison != 0)
            return publicationComparison;
        int chromosomeComparison = linkage.getChromosome().compareTo(o.getLinkage().getChromosome());
        if (chromosomeComparison != 0)
            return chromosomeComparison;

        if (entityOne == null && o.entityOne != null) {
            return 1;
        } else if (entityOne != null && o.entityOne == null) {
            return -1;
        }

        if (entityOne == null && o.entityOne == null) {
            // if both are null, then defer to the comparison below
        } else if (!entityOne.getEntityType().equals(o.entityOne.getEntityType())) {
            if (entityOne.getEntityType().equals(Feature.MUTANT))
                return 1;
            else if (o.entityOne.getEntityType().equals(Feature.MUTANT))
                return -1;
            else
                return entityOne.getEntityType().compareTo(o.entityOne.getEntityType());
        }
        return entityOne.getAbbreviationOrder().compareToIgnoreCase(o.entityOne.getAbbreviationOrder());
    }
}
