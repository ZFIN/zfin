package org.zfin.mutant.presentation;

import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;

import java.util.Set;

/**
 */
public class PhenotypeDisplay implements Comparable<PhenotypeDisplay> {
    private Genotype genotype;
    private Marker morpholino;
    private Term entityTermSuper;
    private Term entityTermSub;
    private Term qualityTerm;
    private Set<Figure> figures;
    private Set<Publication> publications;
    private String tag;
    private boolean moInExperiment;

    private Figure singleFig;
    private Publication singlePub;

    public int getNumberOfFigures() {
        if (figures == null) {
            return 0;
        }
        return figures.size();
    }

    public int getNumberOfPubs() {
        if (publications == null) {
            return 0;
        }
        return publications.size();
    }

    public Genotype getGenotype() {
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }

    public Marker getMorpholino() {
        return morpholino;
    }

    public void setMorpholino(Marker morpholino) {
        this.morpholino = morpholino;
    }

    public Term getEntityTermSuper() {
        return entityTermSuper;
    }

    public void setEntityTermSuper(Term entityTermSuper) {
        this.entityTermSuper = entityTermSuper;
    }

    public Term getEntityTermSub() {
        return entityTermSub;
    }

    public void setEntityTermSub(Term entityTermSub) {
        this.entityTermSub = entityTermSub;
    }

    public Term getQualityTerm() {
        return qualityTerm;
    }

    public void setQualityTerm(Term qualityTerm) {
        this.qualityTerm = qualityTerm;
    }

    public Set<Publication> getPublications() {
        return publications;
    }

    public void setPublications(Set<Publication> publications) {
        this.publications = publications;
    }

    public Set<Figure> getFigures() {
        return figures;
    }

    public void setFigures(Set<Figure> figures) {
        this.figures = figures;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Publication getSinglePub() {
        return singlePub;
    }

    public void setSinglePub(Publication singlePub) {
        this.singlePub = singlePub;
    }

    public Figure getSingleFig() {
        return singleFig;
    }

    public void setSingleFig(Figure singleFig) {
        this.singleFig = singleFig;
    }

    public int compareTo(PhenotypeDisplay anotherPhenotypeDisplay) {
        Marker morpholinoTwo = anotherPhenotypeDisplay.getMorpholino();

        if (morpholino == null && morpholinoTwo != null) {
			return -1;
		} else if (morpholino != null && morpholinoTwo == null) {
			return 1;
	    } else if (morpholino == null && morpholinoTwo == null) {
		    if (entityTermSuper.compareTo(anotherPhenotypeDisplay.getEntityTermSuper()) == 0)
			    return qualityTerm.compareTo(anotherPhenotypeDisplay.getQualityTerm());
			else
				return entityTermSuper.compareTo(anotherPhenotypeDisplay.getEntityTermSuper());
	    } else {
            int indexOfHyphen1 = morpholino.getName().indexOf("-");
            String moNameCmp1 = morpholino.getName().substring(indexOfHyphen1);

            int indexOfHyphen2 = morpholinoTwo.getName().indexOf("-");
            String moNameCmp2 = morpholinoTwo.getName().substring(indexOfHyphen2);

            return moNameCmp1.compareToIgnoreCase(moNameCmp2);
		}
    }

    public boolean isMoInExperiment() {
        return moInExperiment;
    }

    public void setMoInExperiment(boolean moInExperiment) {
        this.moInExperiment = moInExperiment;
    }

    public boolean isImgInFigure() {
        if (figures == null || figures.size() == 0)
            return false;
        for (Figure fig : figures) {
           if (fig.getImages() != null && fig.getImages().size() > 0)
			   return true;
        }
        return false;
    }
}