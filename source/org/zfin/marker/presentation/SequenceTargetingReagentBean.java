package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.ExpressionResultTermComparator;
import org.zfin.expression.Figure;
import org.zfin.expression.presentation.ExpressionDisplay;
import org.zfin.gbrowse.presentation.GBrowseImage;
import org.zfin.gbrowse.presentation.GBrowseImageSimilarComparator;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.presentation.GenotypeInformation;
import org.zfin.mutant.presentation.PhenotypeDisplay;
import org.zfin.ontology.GenericTerm;
import org.zfin.profile.MarkerSupplier;
import org.zfin.publication.Publication;
import org.zfin.sequence.STRMarkerSequence;
import org.zfin.sequence.blast.Database;

import java.util.*;

/**
 */
public class SequenceTargetingReagentBean extends MarkerBean{

    private Logger logger = Logger.getLogger(SequenceTargetingReagentBean.class);

    private Set<Marker> targetGenes ;
    private List<STRMarkerSequence> sequences;
    private List<MarkerSupplier> suppliers;
    private String ncbiBlastUrl;
    private String sequenceAttribution;
    private List<Database> databases;
    private List<Genotype> genotypes;
    private List<GenotypeInformation> genotypeData;
    private List<PhenotypeDisplay> phenotypeDisplays;
    private List<ExpressionResult> expressionResults;
    private Set<GBrowseImage> gBrowseImages;
    private List<String> expressionFigureIDs;
    private List<String> expressionPublicationIDs;
    private List<ExpressionDisplay> expressionDisplays;

    public Set<Marker> getTargetGenes() {
        return targetGenes;
    }

    public void setTargetGenes(Set<Marker> targetGenes) {
        this.targetGenes = targetGenes;
    }
    /**
     * Most of the time there will only be a single sequence.
     * @return
     */
    public STRMarkerSequence getSequence(){
        if(sequences!=null && sequences.size()>0){
            if(sequences.size()>1){
                logger.error("more than 1 sequence for marker: " + marker);
            }
            return sequences.get(0);
        }
        else{
            return null ;
        }
    }

    public String getNcbiBlastUrl() {
        return ncbiBlastUrl;
    }

    public void setNcbiBlastUrl(String ncbiBlastUrl) {
        this.ncbiBlastUrl = ncbiBlastUrl;
    }

    public List<Database> getDatabases() {
        return databases;
    }

    public void setDatabases(List<Database> databases) {
        this.databases = databases;
    }

    public String getSequenceAttribution() {
        return sequenceAttribution;
    }

    public void setSequenceAttribution(String sequenceAttribution) {
        this.sequenceAttribution = sequenceAttribution;
    }


    public boolean isTALEN() {
        if (this.marker.getType().isMarkerType("TALEN")) {
            return true;
        }

        return false;
    }

    public boolean isCRISPR() {
        if (this.marker.getType().isMarkerType("CRISPR")) {
            return true;
        }

        return false;
    }

    public List<MarkerSupplier> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<MarkerSupplier> suppliers) {
        this.suppliers = suppliers;
    }

    public List<Genotype> getGenotypes() {
        return genotypes;
    }

    public void setGenotypes(List<Genotype> genotypes) {
        this.genotypes = genotypes;
    }

    public List<GenotypeInformation> getGenotypeData() {
        return genotypeData;
    }

    public void setGenotypeData(List<GenotypeInformation> genotypeData) {
        this.genotypeData = genotypeData;
    }

    public List<PhenotypeDisplay> getPhenotypeDisplays() {
        return phenotypeDisplays;
    }

    public void setPhenotypeDisplays(List<PhenotypeDisplay> phenotypeDisplays) {
        this.phenotypeDisplays = phenotypeDisplays;
    }

    public List<ExpressionResult> getExpressionResults() {
        return expressionResults;
    }

    public void setExpressionResults(List<ExpressionResult> expressionResults) {
        this.expressionResults = expressionResults;
    }

    public List<String> getExpressionFigureIDs() {
        return expressionFigureIDs;
    }

    public void setExpressionFigureIDs(List<String> expressionFigureIDs) {
        this.expressionFigureIDs = expressionFigureIDs;
    }

    public List<String> getExpressionPublicationIDs() {
        return expressionPublicationIDs;
    }

    public void setExpressionPublicationIDs(List<String> expressionPublicationIDs) {
        this.expressionPublicationIDs = expressionPublicationIDs;
    }

    public List<ExpressionDisplay> getExpressionDisplays() {
        if (expressionResults == null || expressionResults.size() == 0)
            return null;

        if (expressionDisplays == null)
            createExpressionDisplays();

        return expressionDisplays;
    }

    public void setExpressionDisplays(List<ExpressionDisplay> expressionDisplays) {
        this.expressionDisplays = expressionDisplays;
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

    /**
     * Create a list of expressionDisplay objects organized by expressed gene.
     */
    private void createExpressionDisplays() {
        if (expressionResults == null || expressionResults.size() == 0 || expressionFigureIDs == null || expressionFigureIDs.size() == 0 || expressionPublicationIDs == null || expressionPublicationIDs.size() == 0)
            return;

        // a map of zdbIDs of expressed genes as keys and display objects as values
        Map<String, ExpressionDisplay> map = new HashMap<String, ExpressionDisplay>();

        String keySTR = marker.getZdbID();

        for (ExpressionResult xpResult : expressionResults) {
            Marker expressedGene = xpResult.getExpressionExperiment().getGene();
            if (expressedGene != null) {
                Experiment exp = xpResult.getExpressionExperiment().getFishExperiment().getExperiment();

                String key = keySTR + expressedGene.getZdbID();

                Set<Figure> figs = xpResult.getFigures();
                Set<Figure> qualifiedFigures = new HashSet<Figure>();

                for (Figure fig : figs)  {
                    if (expressionFigureIDs.contains(fig.getZdbID())) {
                        qualifiedFigures.add(fig);
                    }
                }

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
                    xpDisplay.getFigures().addAll(qualifiedFigures);

                    xpDisplay.setPublications(new HashSet<Publication>());
                    if (expressionPublicationIDs.contains(pub.getZdbID())) {
                      xpDisplay.getPublications().add(pub);

                      if (!xpDisplay.noFigureOrFigureWithNoLabel()) {
                          map.put(key, xpDisplay);
                      }
                    }
                } else {
                    xpDisplay = map.get(key);

                    if (!xpDisplay.getExpressionTerms().contains(term)) {
                        xpDisplay.getExpressionResults().add(xpResult);
                        xpDisplay.getExpressionTerms().add(term);
                    }

                    Collections.sort(xpDisplay.getExpressionResults(), new ExpressionResultTermComparator());

                    xpDisplay.getFigures().addAll(qualifiedFigures);
                    if (expressionPublicationIDs.contains(pub.getZdbID())) {
                        xpDisplay.getPublications().add(pub);
                    }
                }

            }
        }

        expressionDisplays = new ArrayList<ExpressionDisplay>(map.size());

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

    // the b in browse is lowercase in the method name to make JSP happy for some reason
    public Set<GBrowseImage> getGbrowseImages() {
        return gBrowseImages;
    }

    public void addGBrowseImage(GBrowseImage image) {
        if (gBrowseImages == null) {
            gBrowseImages = new TreeSet<>(new GBrowseImageSimilarComparator());
        }
        gBrowseImages.add(image);
    }
}

