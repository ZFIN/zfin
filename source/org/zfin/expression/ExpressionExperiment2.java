package org.zfin.expression;

import org.hibernate.annotations.GenericGenerator;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.mutant.FishExperiment;
import org.zfin.publication.Publication;
import org.zfin.sequence.MarkerDBLink;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Main Experiment object that contains expression annotations.
 */
@Entity
@Table(name = "expression_experiment2")
public class ExpressionExperiment2 {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfin")
    @GenericGenerator(name = "zfin",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "XPAT"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "xpatex_zdb_id")
    private String zdbID;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xpatex_source_zdb_id")
    private Publication publication;
    @Transient
    private Set<ExpressionResult> expressionResults;
    @ManyToOne()
    @JoinColumn(name = "xpatex_genox_zdb_id")
    private FishExperiment fishExperiment;
    @ManyToOne()
    @JoinColumn(name = "xpatex_gene_zdb_id")
    private Marker gene;
    @ManyToOne()
    @JoinColumn(name = "xpatex_probe_feature_zdb_id")
    private Clone probe;
    @ManyToOne()
    @JoinColumn(name = "xpatex_assay_name")
    private ExpressionAssay assay;
    @ManyToOne()
    @JoinColumn(name = "xpatex_atb_zdb_id")
    private Antibody antibody;
    // this markerdblink refers to either the probe or the gene as far as I can tell.  Mostly the gene, though.
    @ManyToOne()
    @JoinColumn(name = "xpatex_dblink_zdb_id")
    private MarkerDBLink markerDBLink;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "expressionExperiment")
    private Set<ExpressionFigureStage> figureStageSet;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }


    public int getAlternateKey() {
        int hash = 1;
        hash = hash * 31 + publication.hashCode();  // uses zdbID
        hash = hash * 31 + fishExperiment.getZdbID().hashCode();
        hash = hash * 31 + assay.getName().hashCode();
        hash = hash * 31 + (probe == null ? 0 : probe.hashCode()); // uses zdbID
        hash = hash * 31 + (gene == null ? 0 : gene.hashCode());// uses zdbID
        // dblink
        hash = hash * 31 + (markerDBLink == null ? 0 : markerDBLink.hashCode());// uses zdbID
        // atb
        hash = hash * 31 + (antibody == null ? 0 : antibody.hashCode());// uses zdbID

        return hash;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public Set<ExpressionResult> getExpressionResults() {
        return expressionResults;
    }

    public void setExpressionResults(Set<ExpressionResult> expressionResults) {
        this.expressionResults = expressionResults;
    }

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public Clone getProbe() {
        return probe;
    }

    public void setProbe(Clone probe) {
        this.probe = probe;
    }

    public FishExperiment getFishExperiment() {
        return fishExperiment;
    }

    public void setFishExperiment(FishExperiment fishExperiment) {
        this.fishExperiment = fishExperiment;
    }

    public Antibody getAntibody() {
        return antibody;
    }

    public void setAntibody(Antibody antibody) {
        this.antibody = antibody;
    }

    public ExpressionAssay getAssay() {
        return assay;
    }

    public void setAssay(ExpressionAssay assay) {
        this.assay = assay;
    }

    public MarkerDBLink getMarkerDBLink() {
        return markerDBLink;
    }

    public void setMarkerDBLink(MarkerDBLink markerDBLink) {
        this.markerDBLink = markerDBLink;
    }

    /**
     * Distinct expressions are combinations of
     * 1) Figure
     * 2) Stage Range
     * You can add multiple structures to such a combination.
     *
     * @return number of distinct expressions
     */
    public int getDistinctExpressions() {
        HashSet<String> distinctSet = new HashSet<String>();
        if (expressionResults != null) {
            for (ExpressionResult expression : expressionResults) {
                DevelopmentStage startStage = expression.getStartStage();
                DevelopmentStage endStage = expression.getEndStage();
                Set<Figure> figures = expression.getFigures();
                for (Figure figure : figures) {
                    StringBuilder sb = new StringBuilder(figure.getZdbID());
                    sb.append(startStage.getZdbID());
                    sb.append(endStage.getZdbID());
                    distinctSet.add(sb.toString());
                }
            }
        }
        return distinctSet.size();
    }

    public void addExpressionResult(ExpressionResult newResult) {
        if (expressionResults == null)
            expressionResults = new HashSet<ExpressionResult>();
        expressionResults.add(newResult);
    }

    public ExpressionResult getMatchingExpressionResult(ExpressionResult expressionResult) {
        for (ExpressionResult aExpressionResult : getExpressionResults()) {
            if (false == canMergeExpressionResult(aExpressionResult, expressionResult)) {
                return aExpressionResult;
            }
        }
        return null;
    }

    /**
     * Uses alternate key:
     * experiment, anatomy item, start stage, end stage, expression found, and term
     * Only term can be null.  Expression found is a boolean.
     * Since experiment is going ot be moved, we don't really care about that.
     *
     * @param era First expresion result.
     * @param erb Second expression result.
     * @return Indicates if these records are too similar (false) or not (true).
     */
    private boolean canMergeExpressionResult(ExpressionResult era, ExpressionResult erb) {
        if (!era.getSuperTerm().equals(erb.getSuperTerm())) return true;
        if (!era.getStartStage().equals(erb.getStartStage())) return true;
        if (!era.getEndStage().equals(erb.getEndStage())) return true;
        if (!era.isExpressionFound() == erb.isExpressionFound()) return true;
        if (era.getSubTerm() == null && erb.getSubTerm() != null) return true;
        if (era.getSubTerm() != null && erb.getSubTerm() == null) return true;
        if (era.getSubTerm() != null && erb.getSubTerm() != null &&
                false == era.getSubTerm().equals(erb.getSubTerm())) return true;

        return false;
    }

    /**
     * Retrieve all distinct figures for this expression experiment.
     *
     * @return set of figures to which expression results are linked
     */
    public Set<Figure> getAllFigures() {
        if (expressionResults == null)
            return null;
        // at maximum as many figures as result records
        Set<Figure> figures = new HashSet<Figure>(expressionResults.size());
        for (ExpressionResult result : expressionResults) {
            figures.addAll(result.getFigures());
        }
        return figures;
    }

    public Set<ExpressionFigureStage> getFigureStageSet() {
        return figureStageSet;
    }

    public void setFigureStageSet(Set<ExpressionFigureStage> figureStageSet) {
        this.figureStageSet = figureStageSet;
    }

    /**
     * Check if the fish uses a wildtype geno without STRs and the environment is standard or generic control.
     * @return
     */
    public boolean isWildtype() {
        return fishExperiment.isStandardOrGenericControl() && fishExperiment.getFish().isWildtypeWithoutReagents();
    }
}
