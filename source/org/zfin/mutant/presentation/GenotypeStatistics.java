package org.zfin.mutant.presentation;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.expression.Figure;
import org.zfin.mutant.Genotype;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 * ToDo: Please add documentation for this class.
 */
public class GenotypeStatistics {

    private Genotype genotype;
    private AnatomyItem anatomyItem;
    private List<Figure> figures;
    private List<Publication> publications;

    public GenotypeStatistics(Genotype genotype, AnatomyItem anatomyItem) {
        this.genotype = genotype;
        this.anatomyItem = anatomyItem;
    }

    public Genotype getGenotype() {
        return genotype;
    }

    public int getNumberOfFigures() {
        if (figures == null) {
            PublicationRepository publicationRep = RepositoryFactory.getPublicationRepository();
            figures = publicationRep.getFiguresByGenoAndAnatomy(genotype, anatomyItem);
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
            publications = publicationRep.getPublicationsWithFiguresPerGenotypeAndAnatomy(genotype, anatomyItem);
        }
        return publications.size();
    }
}
