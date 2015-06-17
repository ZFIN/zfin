package org.zfin.infrastructure.delete;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.mutant.presentation.GenotypeFishResult;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

public class DeleteGenotypeRule extends AbstractDeleteEntityRule implements DeleteEntityRule {

    public DeleteGenotypeRule(String zdbID) {
        this.zdbID = zdbID;
    }

    @Override
    public List<DeleteValidationReport> validate() {
        Genotype genotype = RepositoryFactory.getMutantRepository().getGenotypeByID(zdbID);
        entity = genotype;

        // loo through all Fish using genotype


        // Can't delete a genotype if it has expression data associated

        List<String> publicationListGO = RepositoryFactory.getPublicationRepository().getPublicationIDsForGOwithField(zdbID);
        SortedSet<Publication> sortedGOpubs = new TreeSet<>();
        for (String pubId : publicationListGO) {
            Publication publication = RepositoryFactory.getPublicationRepository().getPublication(pubId);
            sortedGOpubs.add(publication);
        }
        // Can't delete if used in a GO annotation "inferred from" field
        if (CollectionUtils.isNotEmpty(sortedGOpubs)) {
            addToValidationReport(genotype.getAbbreviation() + " is used in the \"inferred from\" field of GO annotation in the following Publication", sortedGOpubs);
        }

        // can not delete if the genotype is associated with more than 1 publications
        SortedSet<Publication> genoPublications = RepositoryFactory.getPublicationRepository().getAllPublicationsForGenotype(genotype);
        if (CollectionUtils.isNotEmpty(genoPublications) && genoPublications.size() > 1) {
            addToValidationReport(genotype.getAbbreviation() + " associated with more than one publication: ", genoPublications);
        }
        List<FishExperiment> fishExperimentList = RepositoryFactory.getMutantRepository().getFishExperiment(genotype);
        SortedSet<Fish> sortedFish= new TreeSet<>();
        for (FishExperiment fishExp: fishExperimentList){
            Fish fish=fishExp.getFish();
            sortedFish.add(fish);
        }
        if (CollectionUtils.isNotEmpty(sortedFish)) {

            addToValidationReport(genotype.getAbbreviation() + " comprises following fish",sortedFish);
        }
        return validationReportList;
    }

    @Override
    public void prepareDelete() {
        entity = RepositoryFactory.getMutantRepository().getGenotypeByID(zdbID);
    }

    @Override
    public Publication getPublication() {
        Genotype genotype = RepositoryFactory.getMutantRepository().getGenotypeByID(zdbID);
        SortedSet<Publication> genoPublications = RepositoryFactory.getPublicationRepository().getAllPublicationsForGenotype(genotype);
        // FB case 11678, provide link back to pub.
        if (CollectionUtils.isNotEmpty(genoPublications) && genoPublications.size() == 1)
            return genoPublications.first();
        return null;
    }
}
