package org.zfin.zirc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * Per-mutation phenotype description. {@code p_segregation} and {@code p_type} are
 * single-valued scalar {@code text} columns — one inheritance pattern and one
 * phenotype type per phenotype, edited via single-select dropdowns.
 */
@Entity(name = "ZircPhenotype")
@Table(schema = "zirc", name = "phenotype")
@DynamicUpdate
@Getter
@Setter
public class Phenotype implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "p_id", updatable = false, nullable = false)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "p_mutation_id", referencedColumnName = "m_id", nullable = false)
    private Mutation mutation;

    @Column(name = "p_sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "p_description")
    private String description;

    @Column(name = "p_hpf_start")
    private Integer hpfStart;

    @Column(name = "p_hpf_end")
    private Integer hpfEnd;

    // Server-managed cache: derived from hpfStart via the STAGE lookup
    // on each save. Clients render it read-only.
    @Column(name = "p_stage")
    private String stage;

    @Column(name = "p_zfin_image_permission")
    private Boolean zfinImagePermission;

    @Column(name = "p_zirc_image_permission")
    private Boolean zircImagePermission;

    @Column(name = "p_non_mendelian_percentage")
    private Double nonMendelianPercentage;

    @Column(name = "p_non_mendelian_comment")
    private String nonMendelianComment;

    @Column(name = "p_segregation")
    private String segregation;

    @Column(name = "p_type")
    private String type;

}
