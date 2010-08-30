package org.zfin.mutant.presentation;

import org.zfin.expression.Figure;
import org.zfin.mutant.Genotype;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;
import org.zfin.ontology.Term;
import org.zfin.expression.Image;

import java.util.Set;
import java.util.HashSet;

/**
 */
public class PhenotypeDisplay implements Comparable<PhenotypeDisplay> {
    private Genotype genotype;
    private Marker MO;
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

    public Marker getMO() {
        return MO;
    }

    public void setMO(Marker MO) {
        this.MO = MO;
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
        Marker MO2 = anotherPhenotypeDisplay.getMO();

        if (MO == null && MO2 != null) {
			return -1;
		} else if (MO != null && MO2 == null) {
			return 1;
	    } else if (MO == null && MO2 == null) {
		    if (entityTermSuper.compareTo(anotherPhenotypeDisplay.getEntityTermSuper()) == 0)
			    return qualityTerm.compareTo(anotherPhenotypeDisplay.getQualityTerm());
			else
				return entityTermSuper.compareTo(anotherPhenotypeDisplay.getEntityTermSuper());
	    } else {
            int indexOfHyphen1 = MO.getName().indexOf("-");
            String moNameCmp1 = MO.getName().substring(indexOfHyphen1);

            int indexOfHyphen2 = MO2.getName().indexOf("-");
            String moNameCmp2 = MO2.getName().substring(indexOfHyphen2);

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