package org.zfin.figure.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.expression.Figure;
import org.zfin.figure.repository.FigureRepository;
import org.zfin.figure.service.FigureViewService;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.View;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

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

    @JsonView(View.FigureAPI.class)
    @RequestMapping(value = "/{zdbID}/expression-detail", method = RequestMethod.GET)
    public JsonResultResponse<ExpressionTableRow> getFigureExpressionDetail(@PathVariable String zdbID,
                                                                            @Version Pagination pagination) {
        Figure figure = figureRepository.getFigure(zdbID);

        List<ExpressionTableRow> expressionTableRows = figureViewService.getExpressionTableRows(figure);
        JsonResultResponse<ExpressionTableRow> response = new JsonResultResponse<>();
        response.setTotal(expressionTableRows.size());
        List<ExpressionTableRow> paginatedFeatureList = expressionTableRows.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList());

        response.setResults(paginatedFeatureList);
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


}
