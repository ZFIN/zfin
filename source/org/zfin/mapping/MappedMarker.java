package org.zfin.mapping;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DiscriminatorFormula;
import org.zfin.infrastructure.ZdbID;
import org.zfin.profile.Lab;
import org.zfin.profile.Person;

@Entity
@Table(name = "mapped_marker")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula(
        "CASE get_obj_type(marker_id) " +
        "WHEN 'ALT' THEN 'Feat' " +
        "ELSE 'Mark' " +
        "END"
)
@Getter
@Setter
public abstract class MappedMarker implements Comparable, ZdbID {

    @Id
    @Column(name = "zdb_id")
    protected String zdbID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refcross_id", insertable = false, updatable = false)
    protected Panel panel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitter", insertable = false, updatable = false)
    protected Person submitter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab", insertable = false, updatable = false)
    protected Lab lab;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner", insertable = false, updatable = false)
    protected Person owner;

    @Column(name = "comments")
    protected String comments;

    @Column(name = "mm_chromosome", nullable = false)
    protected String lg;

    @Column(name = "marker_id")
    protected String entityID;

    @Column(name = "map_name")
    protected String mappedName;

    @Column(name = "scoring_data")
    protected String scoringData;

    @Column(name = "metric")
    protected String metric;

    @Column(name = "mm_chrom_location")
    protected Float lgLocation;

    public abstract String getEntityID();

    public abstract String getEntityAbbreviation();
}
