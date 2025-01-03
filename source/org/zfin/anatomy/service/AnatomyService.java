package org.zfin.anatomy.service;

import org.springframework.stereotype.Service;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.presentation.StagePresentation;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.presentation.AntibodyStatistics;
import org.zfin.ontology.GenericTerm;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getAnatomyRepository;
import static org.zfin.repository.RepositoryFactory.getAntibodyRepository;

/**
 * Basic Anatomy service class.
 */
@Service
public class AnatomyService {

    public static PaginationResult<AntibodyStatistics> getAntibodyStatistics(GenericTerm aoTerm,
                                                                             Pagination pagination,
                                                                             boolean includeSubstructures) {
        int totalCount = getAntibodyRepository().getAntibodyCount(aoTerm, includeSubstructures, pagination);
        List<String> totalIds = getAntibodyRepository().getPaginatedAntibodyIds(aoTerm, includeSubstructures, pagination);
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
        List<AntibodyStatistics> list = getAntibodyRepository().getAntibodyStatisticsPaginated(aoTerm, paginationBean, paginatedAntibodyIDs, includeSubstructures);
        Map<String, List<Marker>> antibodyAntigenGeneMap = getAntibodyRepository().getAntibodyAntigenGeneMap(paginatedAntibodyIDs);
        list.forEach(antibodyStatistics -> {
            antibodyStatistics.setAntigenGeneList(antibodyAntigenGeneMap.get(antibodyStatistics.getAntibody().getZdbID()));
        });
        return new PaginationResult<>(totalCount, list);
    }

    public static PaginationResult<HighQualityProbe> getHighQualityProbeStatistics(GenericTerm aoTerm,
                                                                                   Pagination pagination,
                                                                                   boolean includeSubstructures) {
        PaginationBean paginationBean = new PaginationBean();
        paginationBean.setMaxDisplayRecords(pagination.getLimit());
        paginationBean.setPageInteger(pagination.getPage());
        paginationBean.setFilterMap(pagination.getFilterMap());

        int totalCount = getAntibodyRepository().getProbeCount(aoTerm, includeSubstructures, pagination);
        List<String> totalIds = getAntibodyRepository().getPaginatedHighQualityProbeIds(aoTerm, includeSubstructures, pagination);
        // if no antibodies found return here
        if (totalCount == 0)
            return new PaginationResult<>(0, null);

        //set paginated antibodyIDs
        List<String> paginatedAntibodyIDs = totalIds.stream()
            .skip(paginationBean.getFirstRecord() - 1)
            .limit(paginationBean.getMaxDisplayRecordsInteger())
            .collect(Collectors.toList());

        List<HighQualityProbe> list = getAntibodyRepository().getProbeStatisticsPaginated(aoTerm, paginationBean, paginatedAntibodyIDs, includeSubstructures);
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

}
