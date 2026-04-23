package org.zfin.mutant.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.ontology.GenericTerm;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Disease model which groups by Publications
 */
@Setter
@Getter
@Entity
@Table(schema = "ui", name = "zebrafish_models_chebi_association")
public class ChebiFishModelDisplay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "omca_id", nullable = false)
    private int id;

    @JsonView(View.API.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "omca_term_zdb_id")
    GenericTerm chebi;

    @JsonView(View.API.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "omca_zebfrafish_model_id")
    FishModelDisplay fishModelDisplay;

    @Column(name = "omca_ancestor_term_ids", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] ancestorTermIds;

}
