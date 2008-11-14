package org.zfin.mutant.presentation;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.expression.Figure;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.framework.presentation.FigureStatistics;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.framework.presentation.EntityStatistics;

import java.util.List;

/**
 * This class is a statistics class about Morpholinos on the Anatomy Detail page.
 */
public class AntibodyStatistics extends EntityStatistics {

    private Antibody antibody;
    private AnatomyItem anatomyItem;
    private List<Figure> figures;
    private FigureStatistics figureStats;
    private int numberOfFigures = -1;

    public AntibodyStatistics(Antibody antibody, AnatomyItem anatomyItem) {
        this.anatomyItem = anatomyItem;
        this.antibody = antibody;
    }

    public Antibody getAntibody() {
        return antibody;
    }

    public int getNumberOfFigures() {
        if (numberOfFigures == -1) {
            AntibodyRepository abRepository = RepositoryFactory.getAntibodyRepository();
            int textOnly = abRepository.getNumberOfFiguresPerAoTerm(antibody, anatomyItem, Figure.Type.TOD);
            int trueFigure = abRepository.getNumberOfFiguresPerAoTerm(antibody, anatomyItem, Figure.Type.FIGURE);
            figureStats = new FigureStatistics(trueFigure, textOnly);
            numberOfFigures = textOnly + trueFigure;
        }
        return numberOfFigures;
    }

    public Figure getFigure() {
        if (figures == null) {
            AntibodyRepository abRepository = RepositoryFactory.getAntibodyRepository();
            figures = abRepository.getFiguresPerAoTerm(antibody, anatomyItem);
        }
        if (figures == null || figures.size() != 1)
            throw new RuntimeException("Can call this method only when there is exactly one figure");
        return figures.iterator().next();
    }

    public PaginationResult<Publication> getPublicationPaginationResult() {
        AntibodyRepository abRepository = RepositoryFactory.getAntibodyRepository();
        return abRepository.getPublicationsWithFigures(antibody, anatomyItem);
    }

}