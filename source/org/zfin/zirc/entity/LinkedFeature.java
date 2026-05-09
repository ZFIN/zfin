package org.zfin.zirc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * One linked-feature entry on a {@link LineSubmission}. Composite PK:
 * (lineSubmission, feature).
 *
 * <p>The {@code feature} string is plain text — there's deliberately no FK
 * to {@code public.feature}, since linked features can include lines being
 * established as part of this submission that don't exist as ZFIN features
 * yet. Distance fields are independent: {@code distanceKnown} gates the
 * UI's display of {@code distanceCentimorgans} / {@code distanceMegabases},
 * but at the storage level all three are nullable.
 */
@Entity(name = "ZircLinkedFeature")
@Table(schema = "zirc", name = "line_submission_linked_feature")
@IdClass(LinkedFeatureId.class)
@Getter
@Setter
public class LinkedFeature implements Serializable {

    @JsonIgnore
    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lslf_line_submission_id", referencedColumnName = "ls_zdb_id", nullable = false)
    private LineSubmission lineSubmission;

    @Id
    @Column(name = "lslf_feature", nullable = false)
    private String feature;

    @Column(name = "lslf_distance_known")
    private Boolean distanceKnown;

    @Column(name = "lslf_distance_centimorgans")
    private Double distanceCentimorgans;

    @Column(name = "lslf_distance_megabases")
    private Double distanceMegabases;

    @Column(name = "lslf_additional_info")
    private String additionalInfo;
}
