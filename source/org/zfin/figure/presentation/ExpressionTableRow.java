package org.zfin.figure.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.expression.*;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.ZdbID;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.publication.Publication;

import jakarta.persistence.*;

/**
 * Stores a collection of entities used to display one row of the figureView expression table
 */
@Setter
@Getter
@Entity
@Table(name = "ui.publication_expression_display")
public class ExpressionTableRow implements ZdbID {

    @Id
    @JsonView(View.ExpressionPublicationUI.class)
    @SequenceGenerator(name = "seq-gen-expression", sequenceName = "publication_expression_display_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq-gen-expression")
    @Column(name = "ped_id", nullable = false)
    private long id;

    @JsonView(View.ExpressionPublicationUI.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ped_pub_zdb_id")
    private Publication publication;

    @JsonView(View.ExpressionPublicationUI.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ped_subterm_zdb_id")
    private GenericTerm subterm;

    @JsonView(View.ExpressionPublicationUI.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ped_superterm_zdb_id")
    private GenericTerm superterm;

    @JsonView(View.ExpressionPublicationUI.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ped_gene_zdb_id")
    private Marker gene;

    @JsonView(View.ExpressionPublicationUI.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ped_antibody_zdb_id")
    private Antibody antibody;

    @JsonView(View.ExpressionPublicationUI.class)
    @Transient
    private FishExperiment fishExperiment;
    @JsonView(View.ExpressionPublicationUI.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ped_fish_zdb_id")
    private Fish fish;
    @JsonView(View.ExpressionPublicationUI.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ped_exp_zdb_id")
    private Experiment experiment;
    @JsonView(View.ExpressionPublicationUI.class)
    @Column(name = "ped_qualifier")
    private String qualifier;

    @Transient
    private Boolean isExpressionFound;
    @JsonView(View.ExpressionPublicationUI.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ped_start_zdb_id")
    private DevelopmentStage start;

    @JsonView(View.ExpressionPublicationUI.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ped_end_zdb_id")
    private DevelopmentStage end;

    @JsonView(View.ExpressionPublicationUI.class)
    @Transient
    private PostComposedEntity entity;

    @JsonView(View.ExpressionPublicationUI.class)
    @Column(name = "ped_anatomy_display")
    private String anatomyDisplay;

    @JsonView(View.ExpressionPublicationUI.class)
    @Column(name = "ped_experiment_display")
    private String experimentDisplay;

    @JsonView(View.ExpressionPublicationUI.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ped_assay_id")
    private ExpressionAssay assay;

    @Transient
    private String fishNameOrder;

    @JsonView(View.ExpressionPublicationUI.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ped_fig_zdb_id")
    private Figure figure;


    //this is a key used for deciding whether to repeat the genotype in the display tag
    // (it's a "new" genotype if it's a new gene and the same genotype...)
    @Transient
    private String geneGenoxZdbIDs;

    public ExpressionTableRow() {

    }

    public ExpressionTableRow(ExpressionFigureStage figureStage, ExpressionResult2 expressionResult) {

        ExpressionExperiment2 expressionExperiment = figureStage.getExpressionExperiment();
        setGene(expressionExperiment.getGene());
        setAntibody(expressionExperiment.getAntibody());
        setFishExperiment(expressionExperiment.getFishExperiment());
        setFish(expressionExperiment.getFishExperiment().getFish());
        setExperiment(expressionExperiment.getFishExperiment().getExperiment());
        setStart(figureStage.getStartStage());
        setEnd(figureStage.getEndStage());
        setIsExpressionFound(expressionResult.isExpressionFound());
        setSuperterm(expressionResult.getSuperTerm());
        if (expressionResult.getSubTerm() != null) {
            setSubterm(expressionResult.getSubTerm());
            setAnatomyDisplay(expressionResult.getSubTerm().getTermName() + " " + expressionResult.getSuperTerm().getTermName());
        } else {
            setAnatomyDisplay(expressionResult.getSuperTerm().getTermName());
        }
        setExperimentDisplay(expressionExperiment.getFishExperiment().getExperiment().getDisplayAllConditions());
        if (!expressionResult.isExpressionFound()) {
            setQualifier("Not Detected");
        }
        setEntity(expressionResult.getEntity());
        setAssay(figureStage.getExpressionExperiment().getAssay());

        setGeneGenoxZdbIDs(gene.getZdbID() + fishExperiment.getZdbID());

        //todo: might want this to be getNameOrder later...
        setFishNameOrder(fishExperiment.getFish().getAbbreviationOrder());
    }


    public String getGeneGenoxZdbIDs() {
        return geneGenoxZdbIDs;
    }

    public void setGeneGenoxZdbIDs(String geneGenoxZdbIDs) {
        this.geneGenoxZdbIDs = geneGenoxZdbIDs;
    }

    public FishExperiment getFishExperiment() {
        return fishExperiment;
    }

    public void setFishExperiment(FishExperiment fishExperiment) {
        this.fishExperiment = fishExperiment;
    }


    @JsonView(View.ExpressionPublicationUI.class)
    public PostComposedEntity getEntity() {
        PostComposedEntity entity = new PostComposedEntity();
        entity.setSuperterm(superterm);
        entity.setSubterm(subterm);
        return entity;
    }

    @JsonView(View.ExpressionPublicationUI.class)
    @JsonProperty("id")
    public String getUniqueKey() {
        StringBuilder builder = new StringBuilder();
        builder.append(gene.getZdbID());
        if (antibody != null)
            builder.append(antibody.getZdbID());
        if (fish != null)
            builder.append(fish.getZdbID());
        if (start != null)
            builder.append(start.getZdbID());
        if (end != null)
            builder.append(end.getZdbID());
        if (experiment != null)
            builder.append(experiment.getZdbID());
        if (assay != null)
            builder.append(assay.getAbbreviation());
        if (figure != null)
            builder.append(figure.getZdbID());
        if (entity != null && entity.getSuperterm() != null)
            builder.append(entity.getSuperterm().getZdbID());
        if (entity != null && entity.getSubterm() != null)
            builder.append(entity.getSubterm().getZdbID());

        return builder.toString();
    }

    @Override
    public String getZdbID() {
        return String.valueOf(id);
    }

    @Override
    public void setZdbID(String zdbID) {

    }
}
