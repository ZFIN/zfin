package org.zfin.publication.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyService;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.feature.Feature;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.View;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class PublicationAPIController {

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private HttpServletRequest request;

    @ResponseBody
    @RequestMapping(value = "/publication/{zdbID}/genes", method = RequestMethod.GET)
    public List<MarkerDTO> getPublicationGenes(@PathVariable String zdbID) {
        List<Marker> genes = publicationRepository.getGenesByPublication(zdbID, false);
        List<MarkerDTO> dtos = new ArrayList<>();
        for (Marker gene : genes) {
            dtos.add(DTOConversionService.convertToMarkerDTO(gene));
        }
        return dtos;
    }

    @JsonView(View.FeatureAPI.class)
    @RequestMapping(value = "/publication/{pubID}/features", method = RequestMethod.GET)
    public JsonResultResponse<Feature> getPublicationMutation(@PathVariable String pubID,
                                                              @Version Pagination pagination) {

        List<Feature> featureList = publicationRepository.getFeaturesByPublication(pubID);
        JsonResultResponse<Feature> response = new JsonResultResponse<>();
        response.setTotal(featureList.size());
        List<Feature> paginatedFeatureList = featureList.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList());

        response.setResults(paginatedFeatureList);
        response.setHttpServletRequest(request);
        return response;
    }


    @JsonView(View.API.class)
    @RequestMapping(value = "/publication/{zdbID}/marker", method = RequestMethod.GET)
    public JsonResultResponse<Marker> getPublicationMarker(@PathVariable String zdbID,
                                                           @Version Pagination pagination) {
        List<Marker> marker = publicationRepository.getGenesByPublication(zdbID, false);
        JsonResultResponse<Marker> response = new JsonResultResponse<>();
        response.setTotal(marker.size());
        List<Marker> markerList = marker.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList());

        response.setResults(markerList);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.AntibodyDetailsAPI.class)
    @RequestMapping(value = "/publication/{publicationID}/antibodies")
    public JsonResultResponse<Antibody> getSequenceView(@PathVariable("publicationID") String publicationID,
                                                        @Version Pagination pagination) {
        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        Publication publication = pr.getPublication(publicationID);

        AntibodyRepository ar = RepositoryFactory.getAntibodyRepository();
        List<Antibody> antibodies = ar.getAntibodiesByPublication(publication);

        JsonResultResponse<Antibody> response = new JsonResultResponse<>();
        response.setTotal(antibodies.size());
        List<Antibody> antibodyList = antibodies.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList());

        antibodyList.forEach(antibody -> {
            AntibodyService service = new AntibodyService(antibody);
            Set<MarkerRelationship> relatedMarker = service.getSortedAntigenRelationships();
            antibody.setAntigenGenes(relatedMarker.stream().map(MarkerRelationship::getFirstMarker).collect(Collectors.toList()));
        });
        response.setResults(antibodyList);

        response.setHttpServletRequest(request);
        return response;
    }

}
