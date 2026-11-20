package org.zfin.feature;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SortNatural;
import org.zfin.gwt.root.dto.FeatureTypeEnum;

import java.util.*;

/**
 * One variant of a feature (ZFIN-10622). A feature has one variant, except a Complex feature,
 * which has several (Variant 1, 2, ...). The genomic, DNA, transcript and protein mutation
 * details belong to a variant; they reach their feature through it.
 */
@Getter
@Setter
@Entity
@Table(name = "variant")
public class Variant implements Comparable<Variant> {

    public static final int PRIMARY_VARIANT_NUMBER = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Variant")
    @GenericGenerator(name = "Variant",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "VAR"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "var_zdb_id")
    private String zdbID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "var_feature_zdb_id", nullable = false)
    private Feature feature;

    /** Display order within the feature: Variant 1, 2, 3 ... */
    @Column(name = "var_variant_number", nullable = false)
    private int variantNumber = PRIMARY_VARIANT_NUMBER;

    /** Single-variant type (Point Mutation, Small Deletion, ...); null when not yet curated. */
    @Column(name = "var_variant_type")
    @org.hibernate.annotations.Type(value = org.zfin.framework.StringEnumValueUserType.class,
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.gwt.root.dto.FeatureTypeEnum")})
    private FeatureTypeEnum variantType;

    /** Set by the database default on insert. */
    @Column(name = "var_modified_at", insertable = false, updatable = false)
    private Date modifiedAt;

    @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FeatureGenomicMutationDetail> featureGenomicMutationDetailSet = new HashSet<>();

    @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FeatureDnaMutationDetail> featureDnaMutationDetailSet = new HashSet<>();

    @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @SortNatural
    private SortedSet<FeatureTranscriptMutationDetail> featureTranscriptMutationDetailSet = new TreeSet<>();

    @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FeatureProteinMutationDetail> featureProteinMutationDetailSet = new HashSet<>();

    public Variant() {
    }

    public Variant(Feature feature, int variantNumber, FeatureTypeEnum variantType) {
        this.feature = feature;
        this.variantNumber = variantNumber;
        this.variantType = variantType;
    }

    // A variant holds at most one genomic, DNA and protein detail (unique per variant in the DB).

    public FeatureGenomicMutationDetail getFeatureGenomicMutationDetail() {
        return first(featureGenomicMutationDetailSet);
    }

    public void setFeatureGenomicMutationDetail(FeatureGenomicMutationDetail detail) {
        featureGenomicMutationDetailSet.clear();
        if (detail != null) {
            detail.setVariant(this);
            featureGenomicMutationDetailSet.add(detail);
        }
    }

    public FeatureDnaMutationDetail getFeatureDnaMutationDetail() {
        return first(featureDnaMutationDetailSet);
    }

    public void setFeatureDnaMutationDetail(FeatureDnaMutationDetail detail) {
        featureDnaMutationDetailSet.clear();
        if (detail != null) {
            detail.setVariant(this);
            featureDnaMutationDetailSet.add(detail);
        }
    }

    public FeatureProteinMutationDetail getFeatureProteinMutationDetail() {
        return first(featureProteinMutationDetailSet);
    }

    public void setFeatureProteinMutationDetail(FeatureProteinMutationDetail detail) {
        featureProteinMutationDetailSet.clear();
        if (detail != null) {
            detail.setVariant(this);
            featureProteinMutationDetailSet.add(detail);
        }
    }

    public void addTranscriptMutationDetail(FeatureTranscriptMutationDetail detail) {
        detail.setVariant(this);
        featureTranscriptMutationDetailSet.add(detail);
    }

    private static <T> T first(Set<T> set) {
        return set == null || set.isEmpty() ? null : set.iterator().next();
    }

    @Override
    public int compareTo(Variant o) {
        return Integer.compare(variantNumber, o.variantNumber);
    }
}
