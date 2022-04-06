package org.zfin.publication.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyService;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.feature.Feature;
import org.zfin.figure.presentation.ExpressionTableRow;
import org.zfin.figure.presentation.PhenotypeTableRow;
import org.zfin.figure.service.FigureViewService;
import org.zfin.framework.api.FieldFilter;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.View;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerType;
import org.zfin.marker.presentation.GeneBean;
import org.zfin.marker.presentation.STRTargetRow;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Fish;
import org.zfin.mutant.PhenotypeWarehouse;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.orthology.Ortholog;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getPhenotypeRepository;

@RestController
@RequestMapping("/api/publication")
public class PublicationAPIController {

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private FigureViewService figureViewService;

    @Autowired
    private HttpServletRequest request;

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/genes", method = RequestMethod.GET)
    public List<MarkerDTO> getPublicationGenes(@PathVariable String zdbID) {
        List<Marker> genes = publicationRepository.getGenesByPublication(zdbID, false);
        List<MarkerDTO> dtos = new ArrayList<>();
        for (Marker gene : genes) {
            dtos.add(DTOConversionService.convertToMarkerDTO(gene));
        }
        return dtos;
    }

    @JsonView(View.FeatureAPI.class)
    @RequestMapping(value = "/{pubID}/features", method = RequestMethod.GET)
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

    @JsonView(View.FigureAPI.class)
    @RequestMapping(value = "/{pubID}/phenotype", method = RequestMethod.GET)
    public JsonResultResponse<PhenotypeTableRow> getPublicationPhenotype(@PathVariable String pubID,
                                                                         @Version Pagination pagination) {

        Publication publication = publicationRepository.getPublication(pubID);
        List<PhenotypeWarehouse> warehouseList = publication.getFigures().stream()
                .map(figure -> getPhenotypeRepository().getPhenotypeWarehouse(figure.getZdbID()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        List<PhenotypeTableRow> phenotypeTableRows = figureViewService.getPhenotypeTableRows(warehouseList);
        JsonResultResponse<PhenotypeTableRow> response = new JsonResultResponse<>();
        response.setTotal(phenotypeTableRows.size());
        List<PhenotypeTableRow> paginatedFeatureList = phenotypeTableRows.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList());

        response.setResults(paginatedFeatureList);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.FigureAPI.class)
    @RequestMapping(value = "/{pubID}/expression", method = RequestMethod.GET)
    public JsonResultResponse<ExpressionTableRow> getPublicationExpression(@PathVariable String pubID,
                                                                           @Version Pagination pagination) {

        Publication publication = publicationRepository.getPublication(pubID);
        List<ExpressionTableRow> expressionTableRows = publication.getFigures().stream()
                .map(figure -> figureViewService.getExpressionTableRows(figure))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
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

    @JsonView(View.OrthologyAPI.class)
    @RequestMapping(value = "/{pubID}/orthology", method = RequestMethod.GET)
    public JsonResultResponse<GeneBean> getOrthology(@PathVariable String pubID,
                                                     @Version Pagination pagination) {

        List<Ortholog> orthologList = publicationRepository.getOrthologPaginationByPub(pubID);

        List<GeneBean> list = getOrthologyBeanList(orthologList);
        JsonResultResponse<GeneBean> response = new JsonResultResponse<>();
        response.setTotal(list.size());

        response.setResults(list.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList()));
        response.setHttpServletRequest(request);
        return response;
    }

    public List<GeneBean> getOrthologyBeanList(List<Ortholog> orthologList) {
        List<GeneBean> beanList = new ArrayList<>(orthologList.size() * 4);
        List<Ortholog> orthologsPerGene = new ArrayList<>(5);
        for (int index = 0; index < orthologList.size(); index++) {
            Ortholog ortholog = orthologList.get(index);
            Marker zebrafishGene = ortholog.getZebrafishGene();
            Marker nextZebrafishGene = null;
            // if not last element set next gene
            if (index != orthologList.size() - 1) {
                nextZebrafishGene = orthologList.get(index + 1).getZebrafishGene();
            }
            orthologsPerGene.add(ortholog);

            // if the last element or the next element is a different gene
            if (nextZebrafishGene == null || !nextZebrafishGene.equals(zebrafishGene)) {
                GeneBean orthologyBean = new GeneBean();
                orthologyBean.setMarker(zebrafishGene);
                //orthologyBean.setOrthologyPresentationBean(MarkerService.getOrthologyPresentationBean(orthologsPerGene, zebrafishGene, publication));
                beanList.add(orthologyBean);
            }
            if (nextZebrafishGene != null && !nextZebrafishGene.equals(zebrafishGene)) {
                orthologsPerGene = new ArrayList<>(5);
            }

        }
        return beanList;
    }


    @JsonView(View.API.class)
    @RequestMapping(value = "/{zdbID}/marker", method = RequestMethod.GET)
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
    @RequestMapping(value = "/{publicationID}/antibodies")
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

    @JsonView(View.FishAPI.class)
    @RequestMapping(value = "/{publicationID}/fish")
    public JsonResultResponse<Fish> getPublicationFeatures(@PathVariable("publicationID") String publicationID,
                                                           @Version Pagination pagination) {
        List<Fish> fishList = publicationRepository.getFishByPublication(publicationID);
        JsonResultResponse<Fish> response = new JsonResultResponse<>();
        response.setTotal(fishList.size());
        List<Fish> paginatedFishList = fishList.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList());

        response.setResults(paginatedFishList);
        response.setHttpServletRequest(request);
        return response;
    }

    @RequestMapping(value = "/{publicationID}/direct-attribution")
    public JsonResultResponse<String> getPublicationAttribution(@PathVariable("publicationID") String publicationID,
                                                                @RequestParam(value = "filter.entityID", required = false) String entityID,
                                                                @Version Pagination pagination) {

        pagination.addFieldFilter(FieldFilter.ENTITY_ID, entityID);
        List<String> directedAttributionIDs = RepositoryFactory.getPublicationRepository().getDirectlyAttributedZdbids(publicationID, pagination);
        JsonResultResponse<String> response = new JsonResultResponse<>();
        response.setTotal(directedAttributionIDs.size());
        List<String> paginatedDirectedAttributionIDs = directedAttributionIDs.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList());

        response.setResults(paginatedDirectedAttributionIDs);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/{publicationID}/efgs")
    public JsonResultResponse<Marker> getPublicationEFGs(@PathVariable("publicationID") String publicationID,
                                                         @Version Pagination pagination) {
        MarkerType efgType = markerRepository.getMarkerTypeByName(Marker.Type.EFG.name());
        List<Marker> markers = publicationRepository.getMarkersByTypeForPublication(publicationID, efgType);
        JsonResultResponse<Marker> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        if (CollectionUtils.isEmpty(markers))
            return response;
        response.setTotal(markers.size());
        List<Marker> markerList = markers.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList());

        response.setResults(markerList);
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping("/{publicationID}/strs")
    public JsonResultResponse<STRTargetRow> showSTRList(@PathVariable String publicationID,
                                                        @RequestParam(value = "filter.targetName", required = false) String targetName,
                                                        @RequestParam(value = "filter.strName", required = false) String strName,
                                                        @Version Pagination pagination) {

        pagination.addFieldFilter(FieldFilter.TARGET_NAME, targetName);
        pagination.addFieldFilter(FieldFilter.STR_NAME, strName);
        List<SequenceTargetingReagent> strs = publicationRepository.getSTRsByPublication(publicationID, pagination);

        List<STRTargetRow> rows = new ArrayList<>(strs.size());
        for (SequenceTargetingReagent str : strs) {
            for (Marker target : str.getTargetGenes()) {
                if (StringUtils.isEmpty(targetName) || target.getAbbreviation().contains(targetName)) {
                    rows.add(new STRTargetRow(str, target));

                }
            }
        }
        rows.sort(Comparator.comparing(STRTargetRow::getTarget));
        JsonResultResponse<STRTargetRow> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        if (CollectionUtils.isEmpty(rows))
            return response;
        response.setTotal(rows.size());
        List<STRTargetRow> markerList = rows.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList());

        response.setResults(markerList);
        return response;
    }

}

