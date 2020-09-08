package org.zfin.marker.presentation;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.expression.presentation.MarkerExpression;
import org.zfin.expression.service.ExpressionService;
import org.zfin.feature.Feature;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.PubPrioritizationGeneSorting;
import org.zfin.marker.Marker;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.DiseaseAnnotationModel;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getPhenotypeRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;


@RestController
@RequestMapping("/api/publication")
@Log4j2
@Repository
public class PublicationPrioritizationController {

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private ExpressionService expressionService;

    @RequestMapping(value = "/{publicationId}/prioritization/genes")
    public JsonResultResponse<Prioritization> getGenePubPrioritization(@PathVariable String publicationId,
                                                                       @Version Pagination pagination) {

        List<Marker> attributedMarker = getPublicationRepository().getGenesByPublication(publicationId);
        Map<Marker, Boolean> isNewGeneMap = getPublicationRepository().areNewGenePubAttribution(attributedMarker, publicationId);
        List<Prioritization> prioList = attributedMarker.stream()
                .map(marker -> {
                    Prioritization prioritization = new Prioritization();
                    prioritization.setId(marker.getZdbID());
                    //     prioritization.setMarker(marker);
                    prioritization.setName(marker.getAbbreviation());
                    prioritization.setNewWithThisPaper(isNewGeneMap.get(marker));

                    prioritization.setHasOrthology(getPublicationRepository().hasCuratedOrthology(marker, publicationId));
                    PhenotypeOnMarkerBean phenotypeOnMarkerBean = MarkerService.getPhenotypeOnGene(marker);
                    prioritization.setPhenotypeFigures(phenotypeOnMarkerBean.getNumFigures());
                    prioritization.setPhenotypePublication(phenotypeOnMarkerBean.getNumPublications());
                    MarkerExpression markerExpression = expressionService.getExpressionForGene(marker);
                    if (marker.isGenedom()) {

                        prioritization.setExpressionFigures(markerExpression.getAllExpressionData().getFigureCount());
                        prioritization.setExpressionInSitu(markerExpression.getDirectlySubmittedExpression().getFigureCount());
                        prioritization.setExpressionPublication(markerExpression.getExpressionPubCount());

                    }
                    List<DiseaseAnnotationModel> diseaseAnnotationModels = getPhenotypeRepository().getDiseaseAnnotationModelsByGene(marker);
                    if (diseaseAnnotationModels != null)
                        prioritization.setAssociatedDiseases(diseaseAnnotationModels.size());
                    return prioritization;
                })
                .collect(Collectors.toList());
        JsonResultResponse<Prioritization> response = new JsonResultResponse<>();
        response.setTotal(prioList.size());
        response.setResults(prioList);
        if (pagination.getSortBy() != null) {
            PubPrioritizationGeneSorting sorting = new PubPrioritizationGeneSorting();
            prioList.sort(sorting.getComparator(pagination.getSortBy()));
        }

        response.setResults(prioList.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList()));

        response.setHttpServletRequest(request);

        return response;
    }

    @RequestMapping(value = "/{publicationId}/prioritization/strs")
    public JsonResultResponse<Prioritization> getStrPubPrioritization(@PathVariable String publicationId)
            throws Exception {

        List<Marker> attributedStrs = getPublicationRepository().getSTRByPublication(publicationId);
        List<Prioritization> prioList = attributedStrs.stream()
                .map(marker -> {
                    Prioritization prioritization = new Prioritization();
                    prioritization.setId(marker.getZdbID());
                    prioritization.setName(marker.getAbbreviation());
                    PhenotypeOnMarkerBean phenotypeOnMarkerBean = MarkerService.getPhenotypeOnGene(marker);
                    prioritization.setPhenotypeFigures(phenotypeOnMarkerBean.getNumFigures());
                    prioritization.setPhenotypePublication(phenotypeOnMarkerBean.getNumPublications());
                    return prioritization;
                })
                .collect(Collectors.toList());
        JsonResultResponse<Prioritization> response = new JsonResultResponse<>();
        response.setTotal(prioList.size());
        response.setResults(prioList);
        response.setHttpServletRequest(request);

        return response;


    }

    @RequestMapping(value = "/{publicationId}/prioritization/features")
    public JsonResultResponse<Prioritization> getFeaturePubPrioritization(@PathVariable String publicationId)
            throws Exception {
        List<Feature> attributedFeatures = getPublicationRepository().getFeaturesByPublication(publicationId);
        List<Prioritization> prioList = attributedFeatures.stream()
                .map(feature -> {
                    Prioritization prioritization = new Prioritization();
                    prioritization.setId(feature.getZdbID());
                    prioritization.setName(feature.getAbbreviation());
                    prioritization.setNewWithThisPaper(getPublicationRepository().isNewFeaturePubAttribution(feature, publicationId));
                    return prioritization;
                })
                .collect(Collectors.toList());
        JsonResultResponse<Prioritization> response = new JsonResultResponse<>();
        response.setTotal(prioList.size());

        response.setResults(prioList);
        response.setHttpServletRequest(request);

        return response;
    }

}
