package org.zfin.fish.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zfin.expression.ExpressionFigureStage;
import org.zfin.expression.presentation.ExpressionDisplay;
import org.zfin.expression.presentation.ProteinExpressionDisplay;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.*;
import org.zfin.mutant.Fish;
import org.zfin.mutant.PhenotypeService;
import org.zfin.mutant.PhenotypeStatementWarehouse;
import org.zfin.mutant.presentation.PhenotypeDisplay;
import org.zfin.repository.RepositoryFactory;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getExpressionRepository;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;

@RestController
@RequestMapping("/api/fish")
public class FishAPIController {

    @Autowired
    private HttpServletRequest request;

    @JsonView(View.API.class)
    @RequestMapping(value = "/{fishID}/rna-expression", method = RequestMethod.GET)
    public JsonResultResponse<ExpressionDisplay> getRnaExpression(@PathVariable String fishID,
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

        List<ExpressionFigureStage> fishNonEfgExpressionResults = getExpressionRepository().getNonEfgExpressionResultsByFish(fish);
        List<String> fishExpressionFigureIDs = getExpressionRepository().getExpressionFigureIDsByFish(fish);
        List<String> fishExpressionPublicationIDs = getExpressionRepository().getExpressionPublicationIDsByFish(fish);
        List<ExpressionDisplay> fishNonEfgExpressionDisplays = ExpressionService.createExpressionDisplays(fish.getZdbID(), fishNonEfgExpressionResults, fishExpressionFigureIDs, fishExpressionPublicationIDs, true);

        // filtering
        FilterService<ExpressionDisplay> filterService = new FilterService<>(new ExpressionDisplayFiltering());
        List<ExpressionDisplay> filteredExpressionList = filterService.filterAnnotations(fishNonEfgExpressionDisplays, pagination.getFieldFilterValueMap());

        if (CollectionUtils.isNotEmpty(filteredExpressionList)) {
            response.setResults(filteredExpressionList.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList()));
            response.setTotal(filteredExpressionList.size());
        }
        response.calculateRequestDuration(startTime);
        HibernateUtil.flushAndCommitCurrentSession();
        return response;
    }

    @JsonView(View.FigureAPI.class)
    @RequestMapping(value = "/{fishID}/protein-expression", method = RequestMethod.GET)
    public JsonResultResponse<ProteinExpressionDisplay> getProteinExpression(@PathVariable String fishID,
                                                                             @RequestParam(value = "filter.antibodyName", required = false) String filterAntibodyName,
                                                                             @RequestParam(value = "filter.geneName", required = false) String filterGeneName,
                                                                             @RequestParam(value = "filter.conditionName", required = false) String filterConditionName,
                                                                             @RequestParam(value = "filter.termName", required = false) String filterTermName,
                                                                             @Version Pagination pagination) {

        LocalDateTime startTime = LocalDateTime.now();
        HibernateUtil.createTransaction();
        JsonResultResponse<ProteinExpressionDisplay> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        Fish fish = RepositoryFactory.getMutantRepository().getFish(fishID);
        if (fish == null)
            return response;

        if (StringUtils.isNotEmpty(filterGeneName)) {
            pagination.addFieldFilter(FieldFilter.NAME, filterGeneName);
        }
        if (StringUtils.isNotEmpty(filterTermName)) {
            pagination.addFieldFilter(FieldFilter.FILTER_TERM_NAME, filterTermName);
        }
        if (StringUtils.isNotEmpty(filterConditionName)) {
            pagination.addFieldFilter(FieldFilter.CONDITION_NAME, filterConditionName);
        }
        if (StringUtils.isNotEmpty(filterAntibodyName)) {
            pagination.addFieldFilter(FieldFilter.ANTIBODY_NAME, filterAntibodyName);
        }

        List<ExpressionFigureStage> fishProteinExpressionResults = getExpressionRepository().getProteinExpressionResultsByFish(fish);
        List<String> fishExpressionFigureIDs = getExpressionRepository().getExpressionFigureIDsByFish(fish);
        List<String> fishExpressionPublicationIDs = getExpressionRepository().getExpressionPublicationIDsByFish(fish);
        List<ProteinExpressionDisplay> fishProteinExpressionDisplays = ExpressionService.createProteinExpressionDisplays(fish.getZdbID(), fishProteinExpressionResults, fishExpressionFigureIDs, fishExpressionPublicationIDs, true);

        // filtering
        FilterService<ProteinExpressionDisplay> filterService = new FilterService<>(new ProteinExpressionDisplayFiltering());
        List<ProteinExpressionDisplay> filteredExpressionList = filterService.filterAnnotations(fishProteinExpressionDisplays, pagination.getFieldFilterValueMap());


        if (CollectionUtils.isNotEmpty(filteredExpressionList)) {
            response.setResults(filteredExpressionList.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList()));
            response.setTotal(filteredExpressionList.size());
        }
        response.calculateRequestDuration(startTime);
        HibernateUtil.flushAndCommitCurrentSession();
        return response;
    }

    @JsonView(View.FigureAPI.class)
    @RequestMapping(value = "/{fishID}/reporter-expression", method = RequestMethod.GET)
    public JsonResultResponse<ExpressionDisplay> getReporterExpression(@PathVariable String fishID,
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

        if (StringUtils.isNotEmpty(filterGeneName)) {
            pagination.addFieldFilter(FieldFilter.NAME, filterGeneName);
        }
        if (StringUtils.isNotEmpty(filterTermName)) {
            pagination.addFieldFilter(FieldFilter.FILTER_TERM_NAME, filterTermName);
        }
        if (StringUtils.isNotEmpty(filterConditionName)) {
            pagination.addFieldFilter(FieldFilter.CONDITION_NAME, filterConditionName);
        }

        List<ExpressionFigureStage> fishEfgExpressionResults = getExpressionRepository().getEfgExpressionResultsByFish(fish);
        List<String> fishExpressionFigureIDs = getExpressionRepository().getExpressionFigureIDsByFish(fish);
        List<String> fishExpressionPublicationIDs = getExpressionRepository().getExpressionPublicationIDsByFish(fish);
        List<ExpressionDisplay> fishEfgExpressionDisplays = ExpressionService.createExpressionDisplays(fish.getZdbID(), fishEfgExpressionResults, fishExpressionFigureIDs, fishExpressionPublicationIDs, true);

        List<ExpressionDisplay> filteredExpressionList = new ArrayList<>();
        // filtering
        if (CollectionUtils.isNotEmpty(fishEfgExpressionDisplays)) {
            FilterService<ExpressionDisplay> filterService = new FilterService<>(new ExpressionDisplayFiltering());
            filteredExpressionList = filterService.filterAnnotations(fishEfgExpressionDisplays, pagination.getFieldFilterValueMap());
        }

        response.setResults(filteredExpressionList.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .collect(Collectors.toList()));
        response.setTotal(filteredExpressionList.size());
        response.calculateRequestDuration(startTime);
        HibernateUtil.flushAndCommitCurrentSession();
        return response;
    }

    @JsonView(View.FigureAPI.class)
    @RequestMapping(value = "/{fishID}/phenotype", method = RequestMethod.GET)
    public JsonResultResponse<PhenotypeDisplay> getPHenotype(@PathVariable String fishID,
                                                             @RequestParam(value = "filter.phenotype", required = false) String filterPhenotype,
                                                             @RequestParam(value = "filter.conditionName", required = false) String filterConditionName,
                                                             @Version Pagination pagination) {

        LocalDateTime startTime = LocalDateTime.now();
        HibernateUtil.createTransaction();
        JsonResultResponse<PhenotypeDisplay> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        Fish fish = RepositoryFactory.getMutantRepository().getFish(fishID);
        if (fish == null)
            return response;

        if (StringUtils.isNotEmpty(filterPhenotype)) {
            pagination.addFieldFilter(FieldFilter.PHENOTYPE, filterPhenotype);
        }
        if (StringUtils.isNotEmpty(filterConditionName)) {
            pagination.addFieldFilter(FieldFilter.CONDITION_NAME, filterConditionName);
        }

        List<PhenotypeStatementWarehouse> phenotypeStatements = getMutantRepository().getPhenotypeStatementWarehousesByFish(fish);
        List<PhenotypeDisplay> phenotypeDisplayList = PhenotypeService.getPhenotypeDisplays(phenotypeStatements, "condition", "phenotypeStatement");

        // filtering
        FilterService<PhenotypeDisplay> filterService = new FilterService<>(new PhenotypeDisplayFiltering());
        List<PhenotypeDisplay> filteredPhenotypeList = filterService.filterAnnotations(phenotypeDisplayList, pagination.getFieldFilterValueMap());


        if (CollectionUtils.isNotEmpty(filteredPhenotypeList)) {
            response.setResults(filteredPhenotypeList.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList()));
            response.setTotal(filteredPhenotypeList.size());
        }
        response.calculateRequestDuration(startTime);
        HibernateUtil.flushAndCommitCurrentSession();
        return response;
    }


}

