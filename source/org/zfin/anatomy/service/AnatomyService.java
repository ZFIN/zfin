package org.zfin.anatomy.service;

import org.springframework.stereotype.Service;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.presentation.StagePresentation;
import org.zfin.expression.ExpressionResult;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.presentation.AntibodyStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.repository.RepositoryFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getAnatomyRepository;

/**
 * Basic Anatomy service class.
 */
@Service
public class AnatomyService {

    public static PaginationResult<AntibodyStatistics> getAntibodyStatistics(GenericTerm aoTerm,
                                                                             Pagination pagination,
                                                                             boolean includeSubstructures) {
        int totalCount = RepositoryFactory.getAntibodyRepository().getAntibodyCount(aoTerm, includeSubstructures, pagination);
        List<String> totalIds = RepositoryFactory.getAntibodyRepository().getPaginatedAntibodyIds(aoTerm, includeSubstructures, pagination);
        // if no antibodies found return here
        if (totalCount == 0)
            return new PaginationResult<>(0, null);

        //set paginated antibodyIDs
        List<String> paginatedAntibodyIDs = totalIds.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .collect(Collectors.toList());

        PaginationBean paginationBean = new PaginationBean();
        paginationBean.setMaxDisplayRecords(pagination.getLimit());
        paginationBean.setPageInteger(pagination.getPage());
        paginationBean.setFilterMap(pagination.getFilterMap());
        List<AntibodyStatistics> list = RepositoryFactory.getAntibodyRepository().getAntibodyStatisticsPaginated(aoTerm, paginationBean, paginatedAntibodyIDs, includeSubstructures);

        return new PaginationResult<>(totalCount, list);
    }

    public static PaginationResult<HighQualityProbe> getHighQualityProbeStatistics(GenericTerm aoTerm,
                                                                                   Pagination pagination,
                                                                                   boolean includeSubstructures) {
        PaginationBean paginationBean = new PaginationBean();
        paginationBean.setMaxDisplayRecords(pagination.getLimit());
        paginationBean.setPageInteger(pagination.getPage());
        paginationBean.setFilterMap(pagination.getFilterMap());

        int totalCount = RepositoryFactory.getAntibodyRepository().getProbeCount(aoTerm, includeSubstructures, pagination);
        List<String> totalIds = RepositoryFactory.getAntibodyRepository().getPaginatedHighQualityProbeIds(aoTerm, includeSubstructures, pagination);
        // if no antibodies found return here
        if (totalCount == 0)
            return new PaginationResult<>(0, null);

        //set paginated antibodyIDs
        List<String> paginatedAntibodyIDs = totalIds.stream()
            .skip(paginationBean.getFirstRecord() - 1)
            .limit(paginationBean.getMaxDisplayRecordsInteger())
            .collect(Collectors.toList());

        List<HighQualityProbe> list = RepositoryFactory.getAntibodyRepository().getProbeStatisticsPaginated(aoTerm, paginationBean, paginatedAntibodyIDs, includeSubstructures);
        return new PaginationResult<>(totalCount, list);
    }

    public static Map<String, String> getDisplayStages() {
        List<DevelopmentStage> stages = getAnatomyRepository().getAllStagesWithoutUnknown();
        LinkedHashMap<String, String> stageListDisplay = new LinkedHashMap<>(stages.size());
        for (DevelopmentStage stage : stages) {
            String labelString = StagePresentation.createDisplayEntry(stage);
            stageListDisplay.put(stage.getZdbID(), labelString);
        }
        return stageListDisplay;
    }

    public static DevelopmentStage getEarliestStartStage(Collection<ExpressionResult> results) {
        if (results == null)
            return null;

        DevelopmentStage stage = null;
        for (ExpressionResult result : results) {
            if (stage == null) {
                stage = result.getStartStage();
                continue;
            }
            if (result.getStartStage().earlierThan(stage))
                stage = result.getStartStage();
        }
        return stage;
    }

    public static DevelopmentStage getLatestEndStage(Collection<ExpressionResult> results) {
        if (results == null)
            return null;

        DevelopmentStage stage = null;
        for (ExpressionResult result : results) {
            if (stage == null) {
                stage = result.getEndStage();
                continue;
            }
            if (stage.earlierThan(result.getEndStage()))
                stage = result.getEndStage();
        }
        return stage;
    }

}
