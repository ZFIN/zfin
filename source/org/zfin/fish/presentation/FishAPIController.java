package org.zfin.fish.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.presentation.ExpressionDisplay;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.*;
import org.zfin.mutant.Fish;
import org.zfin.repository.RepositoryFactory;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getExpressionRepository;

@RestController
@RequestMapping("/api/fish")
public class FishAPIController {

    @Autowired
    private HttpServletRequest request;

    @JsonView(View.API.class)
    @RequestMapping(value = "/{fishID}/rna-expression", method = RequestMethod.GET)
    public JsonResultResponse<ExpressionDisplay> getRnaExpression(@PathVariable String fishID,
                                                                  @RequestParam(value = "directAnnotation", required = false, defaultValue = "false") boolean directAnnotation,
                                                                  @RequestParam(value = "filter.geneName", required = false) String filterGeneName,
                                                                  @RequestParam(value = "filter.conditionName", required = false) String filterConditionName,
                                                                  @RequestParam(value = "filter.termName", required = false) String filterTermName,
                                                                  @Version Pagination pagination) {

        LocalDateTime startTime = LocalDateTime.now();
        HibernateUtil.createTransaction();
        JsonResultResponse<ExpressionDisplay> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        Fish fish = RepositoryFactory.getMutantRepository().getFish(fishID);
        if (fish == null)
            return response;

        pagination.addFieldFilter(FieldFilter.NAME, filterGeneName);
        pagination.addFieldFilter(FieldFilter.FILTER_TERM_NAME, filterTermName);
        pagination.addFieldFilter(FieldFilter.CONDITION_NAME, filterConditionName);

        List<ExpressionResult> fishNonEfgExpressionResults = getExpressionRepository().getNonEfgExpressionResultsByFish(fish);
        List<String> fishExpressionFigureIDs = getExpressionRepository().getExpressionFigureIDsByFish(fish);
        List<String> fishExpressionPublicationIDs = getExpressionRepository().getExpressionPublicationIDsByFish(fish);
        List<ExpressionDisplay> fishNonEfgExpressionDisplays = ExpressionService.createExpressionDisplays(fish.getZdbID(), fishNonEfgExpressionResults, fishExpressionFigureIDs, fishExpressionPublicationIDs, true);

        // filtering
        FilterService<ExpressionDisplay> filterService = new FilterService<>(new ExpressionDisplayFiltering());
        List<ExpressionDisplay> filteredExpressionList = filterService.filterAnnotations(fishNonEfgExpressionDisplays, pagination.getFieldFilterValueMap());


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

