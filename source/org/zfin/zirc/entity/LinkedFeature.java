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
 * One linkage relationship between two mutations on the same
 * {@link LineSubmission}. Composite PK: (lineSubmission, mutationA, mutationB).
 *
 * <p>The DB enforces {@code mutationA.id &lt; mutationB.id} via a CHECK
 * constraint so the pair is symmetric (i.e. {@code (A, B)} and {@code (B, A)}
 * cannot both exist). The service normalizes incoming pairs before save —
 * the swap is invisible to the user but keeps storage consistent.
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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lslf_mutation_a_id", referencedColumnName = "m_id", nullable = false)
    private Mutation mutationA;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lslf_mutation_b_id", referencedColumnName = "m_id", nullable = false)
    private Mutation mutationB;

    @Column(name = "lslf_distance_known")
    private Boolean distanceKnown;

    @Column(name = "lslf_distance_centimorgans")
    private Double distanceCentimorgans;

    @Column(name = "lslf_distance_megabases")
    private Double distanceMegabases;

    @Column(name = "lslf_additional_info")
    private String additionalInfo;
}
