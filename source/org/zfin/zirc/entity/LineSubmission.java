package org.zfin.zirc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Root entity for a ZIRC fish-line submission. UUID PK; not tied into ZFIN's
 * zdb_active_data infrastructure — the submission graph is a self-contained tree.
 *
 * <p>Note: this {@code Mutation} class and its peers are scoped to {@code org.zfin.zirc.entity}
 * on purpose. The bare names match the curator-facing form mockup; fully qualified imports
 * disambiguate from the existing ZFIN concepts in {@code org.zfin.feature} / {@code org.zfin.mutant}.
 */
@Entity(name = "ZircLineSubmission")
@Table(schema = "zirc", name = "line_submission")
@Getter
@Setter
public class LineSubmission implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ls_id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "ls_name", unique = true)
    private String name;

    @Column(name = "ls_abbreviation")
    private String abbreviation;

    @Column(name = "ls_previous_names")
    private String previousNames;

    @Column(name = "ls_features_linked")
    private Boolean featuresLinked;

    @Column(name = "ls_maternal_background")
    private String maternalBackground;

    @Column(name = "ls_paternal_background")
    private String paternalBackground;

    @Column(name = "ls_background_changeable")
    private Boolean backgroundChangeable;

    @Column(name = "ls_background_change_concerns")
    private String backgroundChangeConcerns;

    @Column(name = "ls_unreported_features_details")
    private String unreportedFeaturesDetails;

    @Column(name = "ls_additional_info")
    private String additionalInfo;

    @Column(name = "ls_created_at", insertable = false, updatable = false)
    private Date createdAt;

    @Column(name = "ls_updated_at", insertable = false, updatable = false)
    private Date updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "lineSubmission",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("sortOrder")
    private Set<Mutation> mutations = new HashSet<>();

}
