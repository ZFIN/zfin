package org.zfin.marker;

import org.zfin.expression.Figure;
import org.zfin.framework.presentation.EntityStatistics;
import org.zfin.framework.presentation.FigureStatistics;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.Set;

/**
 * This class is a container for marker-related info by anatomy structure:
 * Publications and Figures.
 */
public class MarkerStatistic extends EntityStatistics {

    private long zdbID;
    private int numberOfFigures;
    private FigureStatistics figureStatistics;
    private Marker gene;
    private Term anatomyTerm;
    private Set<Figure> figures;

    // Do not use!
    // Only used for Hibernate.

    public MarkerStatistic() {
    }

    public MarkerStatistic(Term anatomyTerm, Marker gene) {
        this.anatomyTerm = anatomyTerm;
        this.gene = gene;
    }

    public long getZdbID() {
        return zdbID;
    }

    public void setZdbID(long zdbID) {
        this.zdbID = zdbID;
    }

    protected PaginationResult<Publication> getPublicationPaginationResult() {
        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        return pr.getPublicationsWithFigures(gene, anatomyTerm);
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

    public Term getAnatomyTerm() {
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
