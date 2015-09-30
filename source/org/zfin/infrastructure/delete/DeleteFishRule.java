package org.zfin.infrastructure.delete;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.mutant.DiseaseAnnotation;
import org.zfin.mutant.Fish;
import org.zfin.mutant.PhenotypeStatement;
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
        List<DiseaseAnnotation> diseaseAnnotationList = getPhenotypeRepository().getHumanDiseaseModelsByFish(zdbID);
        if (CollectionUtils.isNotEmpty(diseaseAnnotationList) ) {
            addToValidationReport(fish.getAbbreviation() + " associated with : "+ diseaseAnnotationList.size()+" disease model(s)", diseaseAnnotationList);
        }
        List<PhenotypeStatement> phenotypeStatements = RepositoryFactory.getMutantRepository().getPhenotypeStatementsByFish(fish);
        // Can't delete a genotype if it has phenotypes associated
       if (CollectionUtils.isNotEmpty(phenotypeStatements)) {
            SortedSet<PhenotypeStatement> sortedPhenotypesForFish = new TreeSet<>();
            for (PhenotypeStatement pheno : phenotypeStatements) {
                sortedPhenotypesForFish.add(pheno);
            }
            addToValidationReport(fish.getAbbreviation() + " has the following phenotype annotation", sortedPhenotypesForFish);
        }
        List<ExpressionResult> fishExpressionResults = RepositoryFactory.getExpressionRepository().getExpressionResultsByFish(fish);
         //Can't delete a genotype if it has expression data associated
        if (CollectionUtils.isNotEmpty(fishExpressionResults)) {
            Set<ExpressionExperiment> expressionExperiments = new HashSet<>();
            for (ExpressionResult fishExpressionResult : fishExpressionResults) {
                expressionExperiments.add(fishExpressionResult.getExpressionExperiment());
            }
            int numExpression = expressionExperiments.size();
            Set<Publication> pubs = new HashSet<>();
            for (ExpressionExperiment expressionExperiment : expressionExperiments) {
                pubs.add(expressionExperiment.getPublication());
            }
            addToValidationReport(fish.getAbbreviation() + " is used in " + numExpression +
                    " expression records in the following " + pubs.size() + " publication(s): <br/>", pubs);
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
