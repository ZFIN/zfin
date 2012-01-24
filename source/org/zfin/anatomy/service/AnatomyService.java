package org.zfin.anatomy.service;

import org.springframework.stereotype.Service;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyRelationship;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.presentation.AnatomyPresentation;
import org.zfin.anatomy.presentation.RelationshipPresentation;
import org.zfin.anatomy.presentation.RelationshipSorting;
import org.zfin.anatomy.presentation.StagePresentation;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.mutant.presentation.AntibodyStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getAnatomyRepository;

/**
 * Basic Anatomy service class.
 */
@Service
public class AnatomyService {

    private static AnatomyRepository anatomyRepository = RepositoryFactory.getAnatomyRepository();

    public static List<RelationshipPresentation> getRelations(AnatomyItem term) {
        Set<String> types = new HashSet<String>();
        List<AnatomyRelationship> relatedItems = term.getRelatedItems();
        if (relatedItems == null) {
            relatedItems = anatomyRepository.getAnatomyRelationships(term);
            term.setRelatedItems(relatedItems);
        }
        if (relatedItems != null) {
            for (AnatomyRelationship rel : relatedItems) {
                types.add(rel.getRelationship());
            }
        }
        List<String> uniqueTypes = new ArrayList<String>(types);
        Collections.sort(uniqueTypes, new RelationshipSorting());
        return AnatomyPresentation.createRelationshipPresentation(uniqueTypes, term);

    }


    public static PaginationResult<AntibodyStatistics> getAntibodyStatistics(GenericTerm aoTerm,
                                                                             PaginationBean pagination,
                                                                             boolean includeSubstructures) {
        int totalCount = RepositoryFactory.getAntibodyRepository().getAntibodyCount(aoTerm, includeSubstructures);
        // if no antibodies found return here
        if (totalCount == 0)
            return new PaginationResult<AntibodyStatistics>(0, null);

        if (includeSubstructures)
            return new PaginationResult<AntibodyStatistics>(totalCount, null);


        List<AntibodyStatistics> list = RepositoryFactory.getAntibodyRepository().getAntibodyStatistics(aoTerm, pagination, includeSubstructures);

        return new PaginationResult<AntibodyStatistics>(totalCount, list);
    }

    public static Map<String, String> getDisplayStages() {
        List<DevelopmentStage> stages = getAnatomyRepository().getAllStagesWithoutUnknown();
        LinkedHashMap<String, String> stageListDisplay = new LinkedHashMap<String, String>(stages.size());
        for (DevelopmentStage stage : stages) {
            String labelString = StagePresentation.createDisplayEntry(stage);
            stageListDisplay.put(stage.getZdbID(), labelString);
        }
        return stageListDisplay;
    }


}
