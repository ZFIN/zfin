package org.zfin.fish.presentation;

import org.zfin.audit.AuditLogItem;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.ExpressionResultTermComparator;
import org.zfin.expression.Figure;
import org.zfin.expression.presentation.ExpressionDisplay;
import org.zfin.marker.Marker;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.FishStatistics;
import org.zfin.mutant.presentation.PhenotypeDisplay;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

// TODO: looks like a lot of copied code in GenotypeBean, AbstractFishViewBean, and GenotypeExperimentBean. Can some be refactored out?

public class GenotypeExperimentBean {
    private FishExperiment fishExperiment;
    private Genotype genotype;
    private FishStatistics fishStatistics;
    private List<GenotypeFeature> genotypeFeatures;
    private List<GenotypeFigure> genotypeFigures;
    private List<PhenotypeStatementWarehouse> phenoStatements;
    private List<PhenotypeDisplay> phenoDisplays;
    private List<ExpressionDisplay> expressionDisplays;
    private List<ExpressionResult> expressionResults;

    private int totalNumberOfPublications;

    public GenotypeExperimentBean() {
    }

    public FishExperiment getFishExperiment() {
        return fishExperiment;
    }

    public void setFishExperiment(FishExperiment fishExperiment) {
        this.fishExperiment = fishExperiment;
    }

    public Genotype getGenotype() {
        if (genotype == null) {
            genotype = new Genotype();
        }
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }


    public FishStatistics getFishStatistics() {
        if (fishStatistics == null) {
            if (fishExperiment == null || fishExperiment.getFish() == null) {
                return null;
            }
            return new FishStatistics(fishExperiment.getFish());
        }
        return fishStatistics;
    }

    public void setFishStatistics(FishStatistics fishStatistics) {
        this.fishStatistics = fishStatistics;
    }

    public List<GenotypeFeature> getGenotypeFeatures() {
        return genotypeFeatures;
    }

    public void setGenotypeFeatures(List<GenotypeFeature> genotypeFeatures) {
        this.genotypeFeatures = genotypeFeatures;
    }

    public List<GenotypeFigure> getGenotypeFigures() {
        return genotypeFigures;
    }

    public void setGenotypeFigures(List<GenotypeFigure> genotypeFigures) {
        this.genotypeFigures = genotypeFigures;
    }

    public List<PhenotypeStatementWarehouse> getPhenoStatements() {
        return phenoStatements;
    }

    public void setPhenoStatements(List<PhenotypeStatementWarehouse> phenoStatements) {
        this.phenoStatements = phenoStatements;
    }

    public List<ExpressionDisplay> getExpressionDisplays() {
        if (expressionResults == null || expressionResults.size() == 0) {
            return null;
        }

        if (expressionDisplays == null) {
            createExpressionDisplays();
        }

        return expressionDisplays;
    }

    public int getNumberOfExpressionDisplays() {
        if (expressionResults == null || expressionResults.size() == 0) {
            return 0;
        } else {
            if (expressionDisplays == null) {
                createExpressionDisplays();
            }

            if (expressionDisplays == null) {
                return 0;
            } else {
                return expressionDisplays.size();
            }
        }
    }

    public void setExpressionDisplays(List<ExpressionDisplay> expressionDisplays) {
        this.expressionDisplays = expressionDisplays;
    }

    /**
     * Create a list of expressionDisplay objects organized by expressed gene first,
     * then by the experiment (note that in case of 1) standard or generic control
     * 2) chemical condition(s), there could be one more experiments associated.
     */
    private void createExpressionDisplays() {
        if (expressionResults == null || expressionResults.size() == 0) {
            return;
        }

        // a map of zdbIDs of expressed genes etc as keys and display obejects as values
        Map<String, ExpressionDisplay> map = new HashMap<>();

        String keyGeno = genotype.getZdbID();

        for (ExpressionResult xpResult : expressionResults) {
            Marker expressedGene = xpResult.getExpressionExperiment().getGene();
            if (expressedGene != null) {
                FishExperiment fishExp = xpResult.getExpressionExperiment().getFishExperiment();
                Experiment exp = fishExp.getExperiment();

                String key = keyGeno + expressedGene.getZdbID();

                if (fishExp.isStandardOrGenericControl()) {
                    key = key + "standard";
                } else if (exp.isChemical()) {
                    key = key + "chemical";
                } else {
                    key = key + exp.getZdbID();
                }

                Set<Figure> figs = xpResult.getFigures();
                GenericTerm term = xpResult.getSuperTerm();
                Publication pub = xpResult.getExpressionExperiment().getPublication();

                ExpressionDisplay xpDisplay;
                // if the key not in the map, instantiate a display object and add it to the map
                // otherwise, get the display object from the map
                if (!map.containsKey(key)) {
                    xpDisplay = new ExpressionDisplay(expressedGene);
                    xpDisplay.setExpressionResults(new ArrayList<ExpressionResult>());
                    xpDisplay.setExperiment(exp);
                    xpDisplay.setExpressionTerms(new HashSet<GenericTerm>());

                    xpDisplay.getExpressionResults().add(xpResult);
                    xpDisplay.getExpressionTerms().add(term);

                    xpDisplay.setExpressedGene(expressedGene);

                    xpDisplay.setFigures(new HashSet<Figure>());
                    xpDisplay.getFigures().addAll(figs);

                    xpDisplay.setPublications(new HashSet<Publication>());
                    xpDisplay.getPublications().add(pub);

                    if (!xpDisplay.noFigureOrFigureWithNoLabel()) {
                        map.put(key, xpDisplay);
                    }
                } else {
                    xpDisplay = map.get(key);

                    if (!xpDisplay.getExpressionTerms().contains(term)) {
                      xpDisplay.getExpressionResults().add(xpResult);
                      xpDisplay.getExpressionTerms().add(term);
                    }

                    Collections.sort(xpDisplay.getExpressionResults(), new ExpressionResultTermComparator());

                    xpDisplay.getFigures().addAll(figs);
                    xpDisplay.getPublications().add(pub);
                }

            }
        }

        expressionDisplays = new ArrayList<>(map.size());

        if (map.values().size() > 0) {
            expressionDisplays.addAll(map.values());
            Collections.sort(expressionDisplays);
        }

    }

    public int getTotalNumberOfExpressedGenes() {
        if (expressionResults == null || expressionResults.size() == 0) {
            return 0;
        } else {
            if (expressionDisplays == null) {
                createExpressionDisplays();
            }

            if (expressionDisplays != null) {
                Set<Marker> allExpressedGenes = new HashSet<>();
                for (ExpressionDisplay xpDisp : expressionDisplays) {
                    allExpressedGenes.add(xpDisp.getExpressedGene());
                }
                return allExpressedGenes.size();
            } else {
                return 0;
            }
        }
    }

    public List<PhenotypeDisplay> getPhenoDisplays() {
        if (phenoStatements == null) {
            return null;
        }

        if (phenoDisplays == null || phenoDisplays.size() == 0) {
            createPhenoDisplays();
        }

        return phenoDisplays;
    }

    /**
     * Create a list of phenotypeDisplay objects organized by phenotype statement first,
     * then by the associated experiment.
     */
    private void createPhenoDisplays() {
        if (phenoStatements != null && phenoStatements.size() > 0) {

            // a map of phenotypeStatement-experiment-publication-concatenated-Ids as keys and display objects as values
            Map<String, PhenotypeDisplay> phenoMap = new HashMap<>();

            for (PhenotypeStatementWarehouse pheno : phenoStatements) {

                Figure fig = pheno.getPhenotypeExperiment().getFigure();
                Publication pub = fig.getPublication();

                FishExperiment fishExp = pheno.getPhenotypeExperiment().getFishExperiment();
                Experiment exp = fishExp.getExperiment();

                String keyPheno = pheno.getPhenoStatementString();
                String key;
                if (fishExp.isStandardOrGenericControl()) {
                    key = keyPheno + "standard";
                } else if (exp.isChemical()) {
                    key = keyPheno + "chemical";
                } else {
                    key = keyPheno + exp.getZdbID();
                }

                PhenotypeDisplay phenoDisplay;

                // if the key not in the map, instantiate a display object and add it to the map
                // otherwise, get the display object from the map
                if (!phenoMap.containsKey(key)) {
                    phenoDisplay = new PhenotypeDisplay(pheno);
                    phenoDisplay.setPhenoStatement(pheno);

                    SortedMap<Publication, SortedSet<Figure>> figuresPerPub = new TreeMap<>();
                    SortedSet<Figure> figures = new TreeSet<>();
                    figures.add(fig);
                    figuresPerPub.put(pub, figures);

                    phenoDisplay.setFiguresPerPub(figuresPerPub);

                    phenoMap.put(key, phenoDisplay);
                } else {
                    phenoDisplay = phenoMap.get(key);

                    if (phenoDisplay.getFiguresPerPub().containsKey(pub)) {
                        phenoDisplay.getFiguresPerPub().get(pub).add(fig);
                    } else {
                        SortedSet<Figure> figures = new TreeSet<>();
                        figures.add(fig);
                        phenoDisplay.getFiguresPerPub().put(pub, figures);
                    }
                }
            }

            phenoDisplays = new ArrayList<>(phenoMap.size());

            if (phenoMap.values().size() > 0) {
                phenoDisplays.addAll(phenoMap.values());
                Collections.sort(phenoDisplays);
            }

        }
    }

    public void setPhenoDisplays(List<PhenotypeDisplay> phenoDisplays) {
        this.phenoDisplays = phenoDisplays;
    }

    public int getNumberOfPhenoDisplays() {
        if (phenoStatements == null || phenoStatements.size() == 0) {
            return 0;
        } else {
            if (phenoDisplays == null) {
                createPhenoDisplays();
            }

            if (phenoDisplays == null) {
                return 0;
            } else {
                return phenoDisplays.size();
            }
        }
    }

    public int getNumberOfExpressionResults() {
        if (expressionResults == null) {
            return 0;
        } else {
            return expressionResults.size();
        }
    }


    public List<ExpressionResult> getExpressionResults() {
        return expressionResults;
    }

    public void setExpressionResults(List<ExpressionResult> expressionResults) {
        this.expressionResults = expressionResults;
    }

    public AuditLogItem getLatestUpdate() {
        AuditLogRepository alr = RepositoryFactory.getAuditLogRepository();
        return alr.getLatestAuditLogItem(genotype.getZdbID());
    }

    public int getTotalNumberOfPublications() {
        return totalNumberOfPublications;
    }

    public void setTotalNumberOfPublications(int totalNumberOfPublications) {
        this.totalNumberOfPublications = totalNumberOfPublications;
    }

}
