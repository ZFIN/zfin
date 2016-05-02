package org.zfin.framework.presentation;

import org.zfin.expression.ExpressionResult2;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Class that maps to a statistics table for antibodies
 */
@Entity
@Table(name = "FEATURE_STATS")
@DiscriminatorColumn(
        name = "fstat_type",
        discriminatorType = DiscriminatorType.STRING
)
public class AnatomyFact implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fstat_pk_id")
    private long zdbID;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fstat_xpatres_zdb_id")
    ExpressionResult2 expressionResult;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fstat_superterm_zdb_id")
    GenericTerm superterm;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fstat_subterm_zdb_id")
    GenericTerm subterm;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fstat_gene_zdb_id")
    Marker gene;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fstat_fig_zdb_id")
    Figure figure;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fstat_img_zdb_id")
    Image image;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fstat_pub_zdb_id")
    Publication publication;
    @Column(name = "fstat_type", insertable = false, updatable = false)
    String type;


    public long getZdbID() {
        return zdbID;
    }

    public void setZdbID(long zdbID) {
        this.zdbID = zdbID;
    }

    public GenericTerm getSuperterm() {
        return superterm;
    }

    public void setSuperterm(GenericTerm superterm) {
        this.superterm = superterm;
    }

    public GenericTerm getSubterm() {
        return subterm;
    }

    public void setSubterm(GenericTerm subterm) {
        this.subterm = subterm;
    }

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ExpressionResult2 getExpressionResult() {
        return expressionResult;
    }

    public void setExpressionResult(ExpressionResult2 expressionResult) {
        this.expressionResult = expressionResult;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}