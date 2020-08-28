package org.zfin.marker.presentation;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.marker.Marker;
import org.zfin.mutant.DiseaseAnnotationModel;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
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

    @RequestMapping(value = "/{publicationId}/prioritization/genes")
    public JsonResultResponse<Prioritization> getGenePubPrioritization(@PathVariable String publicationId)
            throws Exception {

        List<Marker> attributedMarker = getPublicationRepository().getGenesByPublication(publicationId);
        List<Prioritization> prioList = attributedMarker.stream()
                .map(marker -> {
                    Prioritization prioritization = new Prioritization();
                    prioritization.setId(marker.getZdbID());
                    prioritization.setName(marker.getAbbreviation());
                    prioritization.setNewWithThisPaper(getPublicationRepository().isNewGenePubAttribution(marker, publicationId));
                    List<DiseaseAnnotationModel> diseaseAnnotationModels = getPhenotypeRepository().getDiseaseAnnotationModelsByGene(marker);
                    if (diseaseAnnotationModels != null)
                        prioritization.setAssociatedDiseases(diseaseAnnotationModels.size());
                    return prioritization;
                })
                .collect(Collectors.toList());
        JsonResultResponse<Prioritization> response = new JsonResultResponse<>();
        response.setTotal(prioList.size());
        response.setResults(prioList);
        response.setHttpServletRequest(request);

        return response;
    }

    @RequestMapping(value = "/{publicationId}/prioritization/strs")
    public JsonResultResponse<Prioritization> getStrPubPrioritization(@PathVariable String publicationId)
            throws Exception {

        return null;
    }

    @RequestMapping(value = "/{publicationId}/prioritization/features")
    public JsonResultResponse<Prioritization> getFeaturePubPrioritization(@PathVariable String publicationId)
            throws Exception {

        return null;
    }

}
