package org.zfin.marker;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.expression.Figure;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.framework.presentation.FigureStatistics;

import java.util.Set;
import java.util.List;

/**
 * This class is a container for marker-related info by anatomy structure:
 * Publications and Figures.
 */
public class MarkerStatistic {

    private long zdbID;
    private int numberOfPublications;
    private int numberOfFigures;
    private FigureStatistics figureStatistics;
    private Marker gene;
    private AnatomyItem anatomyTerm;
    private Set<Figure> figures;

    // Do not use!
    // Only used for Hibernate.
    public MarkerStatistic() {
    }

    public MarkerStatistic(AnatomyItem anatomyTerm, Marker gene) {
        this.anatomyTerm = anatomyTerm;
        this.gene = gene;
    }

    public long getZdbID() {
        return zdbID;
    }

    public void setZdbID(long zdbID) {
        this.zdbID = zdbID;
    }

    public int getNumberOfPublications() {
        return numberOfPublications;
    }

    public void setNumberOfPublications(int numberOfPublications) {
        this.numberOfPublications = numberOfPublications;
    }

    public Marker getGene() {
        return gene;
    }

    public int getNumberOfFigures() {
        return numberOfFigures;
    }

    public void setNumberOfFigures(int numberOfFigures) {
        this.numberOfFigures = numberOfFigures;
    }

    public AnatomyItem getAnatomyTerm() {
        return anatomyTerm;
    }

    public Set<Figure> getFigures() {
        return figures;
    }

    public void setFigures(Set<Figure> figures) {
        this.figures = figures;
    }


    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public void setAnatomyTerm(AnatomyItem anatomyTerm) {
        this.anatomyTerm = anatomyTerm;
    }

    public Figure getFigure() {
        PublicationRepository publicationRep = RepositoryFactory.getPublicationRepository();
        List<Figure> figs = publicationRep.getFiguresByGeneAndAnatomy(gene, anatomyTerm);
        if (figs == null || figs.size() != 1)
            throw new RuntimeException("Can call this method only when there is exactly one figure");
        return figs.get(0);
    }

    public FigureStatistics getFigureStatistics() {
        return figureStatistics;
    }

    public void setFigureStatistics(FigureStatistics figureStatistics) {
        this.figureStatistics = figureStatistics;
    }
}
