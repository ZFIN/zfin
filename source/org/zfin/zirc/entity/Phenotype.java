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

import java.io.Serializable;

/**
 * Per-mutation phenotype description. {@code p_segregation} and {@code p_type} are stored
 * as native PostgreSQL {@code text[]} arrays (per ZFIN-10162 simplification — replaced
 * the original phenotype_segregation / phenotype_type child tables).
 */
@Entity(name = "ZircPhenotype")
@Table(schema = "zirc", name = "phenotype")
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

    @Column(name = "p_hours_post_fertilization")
    private Integer hoursPostFertilization;

    @Column(name = "p_stage")
    private String stage;

    @Column(name = "p_zfin_image_permission")
    private Boolean zfinImagePermission;

    @Column(name = "p_non_mendelian_percentage")
    private Double nonMendelianPercentage;

    @Column(name = "p_segregation", columnDefinition = "text[]")
    private String[] segregation;

    @Column(name = "p_type", columnDefinition = "text[]")
    private String[] type;

}
