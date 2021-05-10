package org.zfin.figure.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.expression.Figure;
import org.zfin.figure.repository.FigureRepository;
import org.zfin.figure.service.FigureViewService;
import org.zfin.framework.api.View;

@RestController
@RequestMapping("/api/figure")
public class FigureAPIController {

    @Autowired
    private FigureRepository figureRepository;

    @Autowired
    private FigureViewService figureViewService;

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

}
