package org.zfin.expression.presentation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.zfin.expression.ExpressionResult2;
import org.zfin.ontology.GenericTerm;

import java.io.Serializable;
import java.util.Date;

/**
 * Convenience class to hold expression result data with term data
 * super- and subterms flattened out.
 */
@Entity
@Table(name = "expression_term_fast_search")
@Getter
@Setter
public class ExpressionTermFastSearch implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "etfs_pk_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etfs_xpatres_pk_id", nullable = false)
    private ExpressionResult2 expressionResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etfs_term_zdb_id", nullable = false)
    private GenericTerm term;

    @Column(name = "etfs_created_date")
    private Date dateCreated;

    @Column(name = "etfs_is_xpatres_term")
    private boolean originalAnnotation;
}
