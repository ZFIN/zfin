package org.zfin.feature.presentation;

import org.zfin.audit.AuditLogItem;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.expression.ExperimentCondition;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.ExpressionResultTermComparator;
import org.zfin.expression.Figure;
import org.zfin.expression.presentation.ExpressionDisplay;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeFeature;
import org.zfin.mutant.GenotypeFigure;
import org.zfin.mutant.presentation.GenotypeStatistics;
import org.zfin.mutant.presentation.PhenotypeDisplay;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

public class GenotypeBean {
    private Genotype genotype;
    private GenotypeStatistics genotypeStatistics;
    private List<GenotypeFeature> genotypeFeatures;
    private List<GenotypeFigure> genotypeFigures;
    private List<PhenotypeDisplay> phenoDisplays;
    // a map of geno-MO-entity-quality-tag as keys and display obejects as values
    private Map<String, PhenotypeDisplay> phenoMap;
    private List<ExpressionDisplay> expressionDisplays;
    private List<ExpressionResult> expressionResults;

    private int totalNumberOfPublications;

    public GenotypeBean() {
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


    public GenotypeStatistics getGenotypeStatistics() {
        if (genotypeStatistics == null) {
            if (genotype == null)
                return null;
            return new GenotypeStatistics(genotype);
        }
        return genotypeStatistics;
    }

    public void setGenotypeStatistics(GenotypeStatistics genotypeStatistics) {
        this.genotypeStatistics = genotypeStatistics;
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

    public List<ExpressionDisplay> getExpressionDisplays() {
        if (expressionResults == null || expressionResults.size() == 0)
            return null;

        if (expressionDisplays == null)
            createExpressionDisplays();

        return expressionDisplays;
    }

    public int getNumberOfExpressionDisplays() {
        if (expressionResults == null || expressionResults.size() == 0) {
            return 0;
        } else {
            if (expressionDisplays == null)
                createExpressionDisplays();

            if (expressionDisplays == null)
                return 0;
            else
                return expressionDisplays.size();
        }
    }

    public void setExpressionDisplays(List<ExpressionDisplay> expressionDisplays) {
        this.expressionDisplays = expressionDisplays;
    }

    private void createExpressionDisplays() {
        if (expressionResults == null || expressionResults.size() == 0)
            return;

        // a map of zdbIDs of expressed genes as keys and display obejects as values
        Map<String, ExpressionDisplay> map = new HashMap<String, ExpressionDisplay>();

        String keyGeno = genotype.getZdbID();

        for (ExpressionResult xpResult : expressionResults) {
            Marker expressedGene = xpResult.getExpressionExperiment().getGene();
            if (expressedGene != null) {
                String key = keyGeno + expressedGene.getZdbID();

                Term superterm = xpResult.getSuperterm();
                Term subterm = xpResult.getSubterm();

                Set<Figure> figs = xpResult.getFigures();
                Publication pub = xpResult.getExpressionExperiment().getPublication();

                ExpressionDisplay xpDisplay;
                // if the key not in the map, instantiate a display object and add it to the map
                // otherwise, get the display object from the map
                if (!map.containsKey(key)) {
                    xpDisplay = new ExpressionDisplay();
                    xpDisplay.setExpressionResults(new ArrayList<ExpressionResult>());
                    xpDisplay.setNonDuplicatedTerms(new HashSet<Term>());
                    xpDisplay.getNonDuplicatedTerms().add(superterm);

                    if (subterm != null)
						xpDisplay.getNonDuplicatedTerms().add(subterm);

                    xpDisplay.getExpressionResults().add(xpResult);
                    xpDisplay.setExpressedGene(expressedGene);
                    xpDisplay.setPublications(new HashSet<Publication>());
                    xpDisplay.getPublications().add(pub);
                    xpDisplay.setFigures(figs);
                    Set<ExperimentCondition> expConditions = xpResult.getExpressionExperiment().getGenotypeExperiment().getExperiment().getExperimentConditions();
                    if (expConditions != null) {
                        for (ExperimentCondition cond : expConditions) {
                            if (cond.getMorpholino() != null) {
                                xpDisplay.setMoInExperiment(true);
                                break;
                            }
                        }
                    } else {
                        xpDisplay.setMoInExperiment(false);
                    }
                    map.put(key, xpDisplay);
                } else {
                    xpDisplay = map.get(key);
                    if (!xpDisplay.getNonDuplicatedTerms().contains(superterm) && !xpDisplay.getNonDuplicatedTerms().contains(subterm)) {
                        xpDisplay.getExpressionResults().add(xpResult);
                        xpDisplay.getNonDuplicatedTerms().add(superterm);

						if (subterm != null)
						  xpDisplay.getNonDuplicatedTerms().add(subterm);

                        Collections.sort(xpDisplay.getExpressionResults(), new ExpressionResultTermComparator());
                    }
                    xpDisplay.getPublications().add(pub);
                    xpDisplay.getFigures().addAll(figs);
                }

            }
        }


        expressionDisplays = new ArrayList<ExpressionDisplay>();

        if (map.values().size() > 0) {
            expressionDisplays.addAll(map.values());
            Collections.sort(expressionDisplays);
        }

    }

    public int getTotalNumberOfExpressedGenes() {
        if (expressionResults == null || expressionResults.size() == 0) {
            return 0;
        } else {
            if (expressionDisplays == null)
                createExpressionDisplays();

            if (expressionDisplays != null) {
                Set<Marker> allExpressedGenes = new HashSet<Marker>();
                for (ExpressionDisplay xpDisp : expressionDisplays) {
                    allExpressedGenes.add(xpDisp.getExpressedGene());
                }
                return allExpressedGenes.size();
            } else {
                return 0;
            }
        }
    }

    public int getTotalNumberOfExpressionFigures() {
        if (expressionResults == null || expressionResults.size() == 0) {
            return 0;
        } else {
            if (expressionDisplays == null)
                createExpressionDisplays();

            if (expressionDisplays != null) {
                Set<Figure> allExpressionFigs = new HashSet<Figure>();
                for (ExpressionDisplay xpDisp : expressionDisplays) {
                    allExpressionFigs.addAll(xpDisp.getFigures());
                }
                return allExpressionFigs.size();
            } else {
                return 0;
            }
        }
    }

    public List<PhenotypeDisplay> getPhenoDisplays() {
        if (genotypeFigures == null)
            return null;

        if (phenoDisplays == null || phenoDisplays.size() == 0)
            createPhenoDisplays();

        return phenoDisplays;
    }

    private void createPhenoDisplays() {
        if (genotypeFigures != null && genotypeFigures.size() > 0) {

            // a map of geno-MO-entity-quality-tag as keys and display objects as values
            phenoMap = new HashMap<String, PhenotypeDisplay>();

            MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

            for (GenotypeFigure genoFig : genotypeFigures) {

                Marker mo = genoFig.getMorpholino();
                String keyMO;
                if (mo == null) {
                    keyMO = "";
                } else {
                    keyMO = mo.getZdbID();
                }

                Figure fig = genoFig.getFigure();
                Publication pub = fig.getPublication();

                String keyGeno = genotype.getZdbID();

                Term superTerm = genoFig.getSuperTerm();
                String superTermID = superTerm.getID();

                Term subTerm = genoFig.getSubTerm();
                if (subTerm != null) {
                    superTermID += subTerm.getID();
                }

                Term qualityTerm = genoFig.getQualityTerm();
                String tag = genoFig.getTag();

                String key = keyGeno + keyMO + superTermID + qualityTerm.getID() + tag;

                PhenotypeDisplay phenoDisplay;

                // if the key not in the map, instantiate a display object and add it to the map
                // otherwise, get the display object from the map
                if (!phenoMap.containsKey(key)) {
                    phenoDisplay = new PhenotypeDisplay();
                    phenoDisplay.setGenotype(genotype);
                    if (mo != null) {
                        phenoDisplay.setMorpholino(mo);
                        phenoDisplay.setMoInExperiment(true);
                    } else {
                        phenoDisplay.setMoInExperiment(false);
                    }

                    phenoDisplay.setEntityTermSuper(superTerm);

                    if (subTerm != null)
                        phenoDisplay.setEntityTermSub(subTerm);

                    phenoDisplay.setQualityTerm(qualityTerm);
                    phenoDisplay.setTag(tag);

                    if (phenoDisplay.getSingleFig() == null)
                        phenoDisplay.setSingleFig(fig);

                    phenoDisplay.setFigures(new HashSet<Figure>());
                    phenoDisplay.getFigures().add(fig);

                    if (phenoDisplay.getSinglePub() == null)
                        phenoDisplay.setSinglePub(pub);

                    phenoDisplay.setPublications(new HashSet<Publication>());
                    phenoDisplay.getPublications().add(pub);

                    phenoMap.put(key, phenoDisplay);
                } else {
                    phenoDisplay = phenoMap.get(key);
                    phenoDisplay.getFigures().add(fig);
                    phenoDisplay.getPublications().add(pub);
                }
            }

            phenoDisplays = new ArrayList<PhenotypeDisplay>();

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
        if (genotypeFigures == null || genotypeFigures.size() == 0) {
            return 0;
        } else {
            if (phenoDisplays == null)
                createPhenoDisplays();

            if (phenoDisplays == null)
                return 0;
            else
                return phenoDisplays.size();
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