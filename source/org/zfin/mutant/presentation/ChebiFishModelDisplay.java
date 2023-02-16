package org.zfin.mutant.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.ontology.GenericTerm;

import javax.persistence.*;

/**
 * Disease model which groups by Publications
 */
@Setter
@Getter
@Entity
@Table(name = "UI.ZEBRAFISH_MODELS_CHEBI_ASSOCIATION")
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

}
