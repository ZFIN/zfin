package org.zfin.mutant.presentation;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.expression.Figure;
import org.zfin.mutant.Morpholino;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 * ToDo: Please add documentation for this class.
 */
public class MorpholinoStatistics {

    private Morpholino morpholino;
    private AnatomyItem anatomyItem;
    private List<Figure> figures;
    private List<Publication> publications;

    public MorpholinoStatistics(Morpholino morpholino, AnatomyItem anatomyItem) {
        this.morpholino = morpholino;
        this.anatomyItem = anatomyItem;
    }

    public Morpholino getMorpholino() {
        return morpholino;
    }

    public int getNumberOfFigures() {
        if (figures == null) {
            PublicationRepository publicationRep = RepositoryFactory.getPublicationRepository();
            figures = publicationRep.getFiguresByMorpholinoAndAnatomy(morpholino, anatomyItem);
        }
        return figures.size();
    }

    public Figure getFigure(){
        if(figures == null || figures.size() != 1)
        throw new RuntimeException("Can call this method only when there is exactly one figure");
        return figures.get(0);
    }

    public int getNumberOfPublications() {
        if (publications == null) {
            PublicationRepository publicationRep = RepositoryFactory.getPublicationRepository();
            publications = publicationRep.getPublicationsWithFiguresPerMorpholinoAndAnatomy(morpholino, anatomyItem);
        }
        return publications.size();
    }
}
