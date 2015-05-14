package org.zfin.infrastructure.delete;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.mutant.DiseaseModel;
import org.zfin.mutant.Fish;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getPhenotypeRepository;

public class DeleteFishRule extends AbstractDeleteEntityRule implements DeleteEntityRule {

    public DeleteFishRule(String zdbID) {
        this.zdbID = zdbID;
    }

    @Override
    public List<DeleteValidationReport> validate() {
        Fish fish = RepositoryFactory.getMutantRepository().getFish(zdbID);
        entity = fish;
        List<Publication> publicationList = RepositoryFactory.getMutantRepository().getPublicationWithFish(zdbID);
        if (CollectionUtils.isNotEmpty(publicationList) && publicationList.size() > 1) {
            addToValidationReport(fish.getAbbreviation() + " associated with more than one publication: ", publicationList);
        }
        List<DiseaseModel> diseaseModelList = getPhenotypeRepository().getHumanDiseaseModelsByFish(zdbID);
        if (CollectionUtils.isNotEmpty(diseaseModelList) && diseaseModelList.size() > 1) {
            addToValidationReport(fish.getAbbreviation() + " associated with : "+diseaseModelList.size()+" disease models", diseaseModelList);
        }
        return validationReportList;
    }

    @Override
    public void prepareDelete() {
        entity = RepositoryFactory.getMutantRepository().getFish(zdbID);
    }

    @Override
    public Publication getPublication() {
        Fish fish = RepositoryFactory.getMutantRepository().getFish(zdbID);
        SortedSet<Publication> genoPublications = RepositoryFactory.getPublicationRepository().getAllPublicationsForGenotype(fish.getGenotype());
        // FB case 11678, provide link back to pub.
        if (CollectionUtils.isNotEmpty(genoPublications) && genoPublications.size() == 1)
            return genoPublications.first();
        return null;
    }
}
