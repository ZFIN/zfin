package org.zfin.figure.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zfin.expression.Figure;
import org.zfin.figure.repository.FigureRepository;
import org.zfin.figure.service.FigureViewService;
import org.zfin.framework.api.*;
import org.zfin.mutant.PhenotypeWarehouse;
import org.zfin.wiki.presentation.Version;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getPhenotypeRepository;

@RestController
@RequestMapping("/api/figure")
public class FigureAPIController {

    @Autowired
    private FigureRepository figureRepository;

    @Autowired
    private FigureViewService figureViewService;

    @Autowired
    private HttpServletRequest request;

    @JsonView(View.API.class)
    @RequestMapping("/{zdbID}/summary")
    public FigureGalleryImagePresentation getFigureDataSummary(@PathVariable String zdbID) {
        Figure figure = figureRepository.getFigure(zdbID);

        FigureGalleryImagePresentation result = new FigureGalleryImagePresentation();
        result.setFigureExpressionSummary(figureViewService.getFigureExpressionSummary(figure));
        result.setDetails(figure.getCaption());
        result.setFigurePhenotypeSummary(figureViewService.getFigurePhenotypeSummary(figure));
        return result;
    }

    @JsonView(View.ExpressionPublicationUI.class)
    @RequestMapping(value = "/{zdbID}/expression-detail", method = RequestMethod.GET)
    public JsonResultResponse<ExpressionTableRow> getFigureExpressionDetail(@PathVariable String zdbID,
                                                                            @RequestParam(value = "filter.geneAbbreviation", required = false) String geneAbbreviation,
                                                                            @RequestParam(value = "filter.antibody", required = false) String antibody,
                                                                            @RequestParam(value = "filter.stage", required = false) String stage,
                                                                            @RequestParam(value = "filter.anatomy", required = false) String anatomy,
                                                                            @RequestParam(value = "filter.assay", required = false) String assay,
                                                                            @RequestParam(value = "filter.fish", required = false) String fish,
                                                                            @RequestParam(value = "filter.experiment", required = false) String experiment,
                                                                            @Version Pagination pagination) {
        pagination.addFieldFilter(FieldFilter.GENE_ABBREVIATION, geneAbbreviation);
        pagination.addFieldFilter(FieldFilter.ANTIBODY_NAME, antibody);
        pagination.addFieldFilter(FieldFilter.ANATOMY, anatomy);
        pagination.addFieldFilter(FieldFilter.STAGE, stage);
        pagination.addFieldFilter(FieldFilter.FISH_NAME, fish);
        pagination.addFieldFilter(FieldFilter.ASSAY, assay);
        pagination.addFieldFilter(FieldFilter.EXPERIMENT, experiment);
        Figure figure = figureRepository.getFigure(zdbID);

        List<ExpressionTableRow> expressionTableRows = figureViewService.getExpressionTableRows(figure);
        JsonResultResponse<ExpressionTableRow> response = new JsonResultResponse<>();

        // filtering
        FilterService<ExpressionTableRow> filterService = new FilterService<>(new ExpressionTableRowFiltering());
        List<ExpressionTableRow> filteredExpressionList = filterService.filterAnnotations(expressionTableRows, pagination.getFieldFilterValueMap());

        response.setTotal(filteredExpressionList.size());
        List<ExpressionTableRow> paginatedExpressionList = filteredExpressionList.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .collect(Collectors.toList());

        response.setResults(paginatedExpressionList);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.FigureAPI.class)
    @RequestMapping(value = "/{zdbID}/antibody-labeling", method = RequestMethod.GET)
    public JsonResultResponse<AntibodyTableRow> getFigureExpressionAntibodyDetail(@PathVariable String zdbID,
                                                                                  @Version Pagination pagination) {
        Figure figure = figureRepository.getFigure(zdbID);

        List<AntibodyTableRow> antibodyTableRows = figureViewService.getAntibodyTableRows(figure);
        JsonResultResponse<AntibodyTableRow> response = new JsonResultResponse<>();
        response.setTotal(antibodyTableRows.size());
        List<AntibodyTableRow> paginatedFeatureList = antibodyTableRows.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .collect(Collectors.toList());

        response.setResults(paginatedFeatureList);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.FigureAPI.class)
    @RequestMapping(value = "/{zdbID}/phenotype-detail", method = RequestMethod.GET)
    public JsonResultResponse<PhenotypeTableRow> getFigurePhenotypeDetail(@PathVariable String zdbID,
                                                                          @RequestParam(value = "filter.fish", required = false) String fish,
                                                                          @RequestParam(value = "filter.stage", required = false) String stage,
                                                                          @RequestParam(value = "filter.phenotype", required = false) String phenotype,
                                                                          @RequestParam(value = "filter.condition", required = false) String condition,
                                                                          @Version Pagination pagination) {

        pagination.addFieldFilter(FieldFilter.PHENOTYPE, phenotype);
        pagination.addFieldFilter(FieldFilter.STAGE, stage);
        pagination.addFieldFilter(FieldFilter.FISH_NAME, fish);
        pagination.addFieldFilter(FieldFilter.EXPERIMENT, condition);

        Figure figure = figureRepository.getFigure(zdbID);

        List<PhenotypeWarehouse> warehouseList = getPhenotypeRepository().getPhenotypeWarehouse(figure.getZdbID());
        List<PhenotypeTableRow> phenotypeTableRows = figureViewService.getPhenotypeTableRows(warehouseList);
        JsonResultResponse<PhenotypeTableRow> response = new JsonResultResponse<>();


        // filtering
        FilterService<PhenotypeTableRow> filterService = new FilterService<>(new PhenotypeTableRowFiltering());
        List<PhenotypeTableRow> filteredExpressionList = filterService.filterAnnotations(phenotypeTableRows, pagination.getFieldFilterValueMap());

        response.setTotal(filteredExpressionList.size());
        List<PhenotypeTableRow> paginatedExpressionList = filteredExpressionList.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .collect(Collectors.toList());

        response.setResults(paginatedExpressionList);
        response.setHttpServletRequest(request);
        return response;
    }


}
