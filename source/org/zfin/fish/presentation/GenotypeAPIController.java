package org.zfin.fish.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.*;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeService;
import org.zfin.mutant.presentation.GenotypeFishResult;
import org.zfin.wiki.presentation.Version;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;

@RestController
@RequestMapping("/api/genotype")
public class GenotypeAPIController {

    @Autowired
    private HttpServletRequest request;

    @JsonView(View.API.class)
    @RequestMapping(value = "/{genotypeID}/fish", method = RequestMethod.GET)
    public JsonResultResponse<GenotypeFishResult> getRnaExpression(@PathVariable String genotypeID,
                                                                   @RequestParam(value = "filter.geneName", required = false) String filterGeneName,
                                                                   @RequestParam(value = "filter.fishName", required = false) String filterFishName,
                                                                   @Version Pagination pagination) {

        LocalDateTime startTime = LocalDateTime.now();
        HibernateUtil.createTransaction();
        JsonResultResponse<GenotypeFishResult> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        Genotype genotype = getMutantRepository().getGenotypeByID(genotypeID);

        if (genotype == null)
            return response;

        if (StringUtils.isNotEmpty(filterGeneName)) {
            pagination.addFieldFilter(FieldFilter.NAME, filterGeneName);
        }
        if (StringUtils.isNotEmpty(filterFishName)) {
            pagination.addFieldFilter(FieldFilter.FISH_NAME, filterFishName);
        }

        List<GenotypeFishResult> allFish = new ArrayList<>();

        List<GenotypeFishResult> fishSummaryList = FishService.getFishExperimentSummaryForGenotype(genotype);
        for (GenotypeFishResult fishSummary : fishSummaryList) {
            fishSummary.setAffectedMarkers(GenotypeService.getAffectedMarker(genotype));
            allFish.add(fishSummary);
        }

        // filtering
        FilterService<GenotypeFishResult> filterService = new FilterService<>(new GenotypeFishResultFiltering());
        List<GenotypeFishResult> filteredExpressionList = filterService.filterAnnotations(allFish, pagination.getFieldFilterValueMap());


        response.setResults(filteredExpressionList.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .collect(Collectors.toList()));
        response.setTotal(filteredExpressionList.size());
        response.calculateRequestDuration(startTime);
        HibernateUtil.flushAndCommitCurrentSession();
        return response;
    }


}

