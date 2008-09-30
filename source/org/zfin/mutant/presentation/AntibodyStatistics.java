package org.zfin.mutant.presentation;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.expression.Figure;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 * This class is a statistics class about Morpholinos on the Anatomy Detail page.
 */
public class AntibodyStatistics {

    private Antibody antibody;
    private AnatomyItem anatomyItem;
    private List<Figure> figures;
    private int numberOfFigures = -1;
    private int numberOfPublications = -1;
    private List<Publication> publications;

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
            numberOfFigures = abRepository.getNumberOfFiguresPerAoTerm(antibody, anatomyItem);
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

    public int getNumberOfPublications() {
        if (numberOfPublications == -1) {
            AntibodyRepository abRepository = RepositoryFactory.getAntibodyRepository();
            numberOfPublications = abRepository.getNumberOfPublicationsWithFiguresPerAoTerm(antibody, anatomyItem);
        }
        return numberOfPublications;
    }

    public Publication getPublication() {
        if (publications == null) {
            AntibodyRepository abRepository = RepositoryFactory.getAntibodyRepository();
            publications = abRepository.getPublicationsWithFiguresPerAoTerm(antibody, anatomyItem);
        }
        if (publications == null || publications.size() != 1)
            throw new RuntimeException("Can call this method only when there is exactly one publication");
        return publications.iterator().next();
    }


}