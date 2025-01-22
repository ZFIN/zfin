package org.zfin.publication.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyService;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.construct.ConstructRelationship;
import org.zfin.feature.Feature;
import org.zfin.figure.presentation.ExpressionTableRow;
import org.zfin.figure.presentation.PhenotypeTableRow;
import org.zfin.figure.service.FigureViewService;
import org.zfin.framework.api.*;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.root.dto.ConstructDTO;
import org.zfin.gwt.root.dto.ConstructRelationshipDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.mapping.MappingService;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerType;
import org.zfin.marker.presentation.GeneBean;
import org.zfin.marker.presentation.STRTargetRow;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.*;
import org.zfin.orthology.Ortholog;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.wiki.presentation.Version;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.*;

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
                                                                         @RequestParam(value = "filter.fish", required = false) String fish,
                                                                         @RequestParam(value = "filter.stage", required = false) String stage,
                                                                         @RequestParam(value = "filter.phenotype", required = false) String phenotype,
                                                                         @RequestParam(value = "filter.condition", required = false) String condition,
                                                                         @Version Pagination pagination) {
        pagination.addFieldFilter(FieldFilter.PHENOTYPE, phenotype);
        pagination.addFieldFilter(FieldFilter.STAGE, stage);
        pagination.addFieldFilter(FieldFilter.FISH_NAME, fish);
        pagination.addFieldFilter(FieldFilter.EXPERIMENT, condition);

        Publication publication = publicationRepository.getPublication(pubID);
        List<PhenotypeWarehouse> warehouseList = publication.getFigures().stream()
            .map(figure -> getPhenotypeRepository().getPhenotypeWarehouse(figure.getZdbID()))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        List<PhenotypeTableRow> phenotypeTableRows = figureViewService.getPhenotypeTableRows(warehouseList);
        // filtering
        FilterService<PhenotypeTableRow> filterService = new FilterService<>(new PhenotypeTableRowFiltering());
        List<PhenotypeTableRow> filteredExpressionList = filterService.filterAnnotations(phenotypeTableRows, pagination.getFieldFilterValueMap());

        JsonResultResponse<PhenotypeTableRow> response = new JsonResultResponse<>();
        response.setTotal(filteredExpressionList.size());
        List<PhenotypeTableRow> paginatedFeatureList = filteredExpressionList.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .toList();

        response.setResults(paginatedFeatureList);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.ExpressionPublicationUI.class)
    @RequestMapping(value = "/{pubID}/expression", method = RequestMethod.GET)
    public JsonResultResponse<ExpressionTableRow> getPublicationExpression(@PathVariable String pubID,
                                                                           @RequestParam(value = "filter.geneAbbreviation", required = false) String geneAbbreviation,
                                                                           @RequestParam(value = "filter.qualifier", required = false) String qualifier,
                                                                           @RequestParam(value = "filter.anatomy", required = false) String anatomyFilter,
                                                                           @RequestParam(value = "filter.fish", required = false) String fishFilter,
                                                                           @RequestParam(value = "filter.stage", required = false) String stageFilter,
                                                                           @RequestParam(value = "filter.assay", required = false) String assayFilter,
                                                                           @RequestParam(value = "filter.experiment", required = false) String experimentFilter,
                                                                           @Version Pagination pagination) {

        LocalDateTime startTime = LocalDateTime.now();
        JsonResultResponse<ExpressionTableRow> response = new JsonResultResponse<>();
        Publication publication = publicationRepository.getPublication(pubID);
        if (publication == null)
            return response;

        if (StringUtils.isNotEmpty(geneAbbreviation)) {
            pagination.addToFilterMap("tableRow.gene.abbreviation", geneAbbreviation);
        }
        if (StringUtils.isNotEmpty(qualifier)) {
            pagination.addToFilterMap("tableRow.qualifier", qualifier);
        }
       if (StringUtils.isNotEmpty(anatomyFilter)) {
           pagination.addToFilterMap("tableRow.anatomyDisplay", anatomyFilter);
        }
       if (StringUtils.isNotEmpty(fishFilter)) {
            pagination.addToFilterMap("tableRow.fish.name", fishFilter);
        }
       if (StringUtils.isNotEmpty(assayFilter)) {
            pagination.addToFilterMap("tableRow.assay.abbreviation", assayFilter);
        }
       if (StringUtils.isNotEmpty(experimentFilter)) {
            pagination.addToFilterMap("tableRow.experimentDisplay", experimentFilter);
        }
       if (StringUtils.isNotEmpty(stageFilter)) {
            pagination.addToFilterMap("tableRow.start.name OR tableRow.end.name", stageFilter);
        }

        PaginationResult<ExpressionTableRow> expressionTableRows = getPublicationPageRepository().getPublicationExpression(publication, pagination);

        response.setTotal(expressionTableRows.getTotalCount());
        response.setResults(expressionTableRows.getPopulatedResults());
        response.setHttpServletRequest(request);
        response.calculateRequestDuration(startTime);
        return response;
    }

    @JsonView(View.PublicationUI.class)
    @RequestMapping(value = "/{pubID}/probes", method = RequestMethod.GET)
    public JsonResultResponse<Clone> getPublicationProbes(@PathVariable String pubID,
                                                          @RequestParam(value = "filter.symbol", required = false) String probeSymbol,
                                                          @RequestParam(value = "filter.type", required = false) String probeType,
                                                          @Version Pagination pagination) {

        LocalDateTime startTime = LocalDateTime.now();
        JsonResultResponse<Clone> response = new JsonResultResponse<>();
        Publication publication = publicationRepository.getPublication(pubID);
        if (publication == null)
            return response;

        if (StringUtils.isNotEmpty(probeSymbol)) {
            pagination.addToFilterMap("exp.probe.abbreviation", probeSymbol);
        }
        if (StringUtils.isNotEmpty(probeType)) {
            pagination.addToFilterMap("exp.probe.zdbID", probeType);
        }

        PaginationResult<Clone> expressionTableRows = getPublicationPageRepository().getProbes(publication, pagination);

        response.setTotal(expressionTableRows.getTotalCount());
        response.setResults(expressionTableRows.getPopulatedResults());
        response.addSupplementalData("probeTypes", getPublicationPageRepository().getProbeTypes(publication, pagination));


        response.setHttpServletRequest(request);
        response.calculateRequestDuration(startTime);
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
        List<Marker> markers = publicationRepository.getGenesAndMarkersByPublication(zdbID);
        JsonResultResponse<Marker> response = new JsonResultResponse<>();
        response.setTotal(markers.size());
        List<Marker> markerList = markers.stream()
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
    @RequestMapping(value = "/{publicationID}/mapping")
    public JsonResultResponse<ChromosomeLinkage> getPublicationMapping(@PathVariable("publicationID") String publicationID,
                                                                       @Version Pagination pagination) {


        Publication publication = getPublicationRepository().getPublication(publicationID);
        List<EntityZdbID> mappedEntities = getLinkageRepository().getMappedEntitiesByPub(publication);

        // add chromosome info
        List<ChromosomeLinkage> list = mappedEntities.stream()
            .map(entityZdbID -> {
                ChromosomeLinkage linkage = new ChromosomeLinkage();
                linkage.setEntity(entityZdbID);
                linkage.setChromosome(MappingService.getChromosomeLocationDisplay(entityZdbID));
                return linkage;
            })
            .toList();

        JsonResultResponse<ChromosomeLinkage> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        if (CollectionUtils.isEmpty(mappedEntities))
            return response;
        response.setTotal(list.size());
        List<ChromosomeLinkage> markerList = list.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .collect(Collectors.toList());

        response.setResults(markerList);
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/{publicationID}/diseases")
    public JsonResultResponse<DiseaseAnnotationModel> getPublicationDisease(@PathVariable("publicationID") String publicationID,
                                                                            @Version Pagination pagination) {
        List<DiseaseAnnotationModel> list = getPhenotypeRepository().getHumanDiseaseAnnotationModels(publicationID);
        JsonResultResponse<DiseaseAnnotationModel> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        if (CollectionUtils.isEmpty(list))
            return response;
        response.setTotal(list.size());
        List<DiseaseAnnotationModel> markerList = list.stream()
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

    @ResponseBody
    @RequestMapping(value = "/{publicationID}/{featureTypeName}/markersForRelation")
    public List<MarkerDTO> getMarkersForRelation(@PathVariable String publicationID, @PathVariable String featureTypeName) {
        List<Marker> markers = markerRepository.getMarkersForRelation(featureTypeName, publicationID);
        List<MarkerDTO> markerDTOs = new ArrayList<MarkerDTO>();
        for (Marker m : markers) {
            markerDTOs.add(DTOConversionService.convertToMarkerDTO(m));
        }
        return markerDTOs;
    }

    @ResponseBody
    @RequestMapping(value = "/{publicationID}/construct-relationships")
    public List<ConstructRelationshipDTO> getConstructMarkerRelationshipsForPub(@PathVariable String publicationID) {
        List<ConstructRelationshipDTO> constructRelnDTOs = new ArrayList<>();
        List<ConstructRelationship> constructMarkerRelationships = getConstructRepository().getConstructRelationshipsByPublication(publicationID);
        if (CollectionUtils.isNotEmpty(constructMarkerRelationships)) {
            for (ConstructRelationship markerRelationship : constructMarkerRelationships) {
                constructRelnDTOs.add(DTOConversionService.convertToConstructRelationshipDTO(markerRelationship));
            }
        }

        getMarkerRepository().getConstructsForAttribution(publicationID).forEach(construct -> {
            Set<ConstructRelationship> crels = construct.getConstructRelations();
            for(ConstructRelationship crel : crels) {
                ConstructRelationshipDTO dto = DTOConversionService.convertToConstructRelationshipDTO(crel);
                constructRelnDTOs.add(dto);
            }
        });

        //Create a Set to ensure uniqueness
        //Filter down the unique set of construct relationship DTOs to only include those that are in the original list constructMarkerRelationships
        //Sort by name
        return new HashSet<>(constructRelnDTOs).stream()
            .filter(cmrel -> constructMarkerRelationships.stream()
                .anyMatch(cmrel2 -> cmrel2.getZdbID().equals(cmrel.getZdbID())))
            .sorted(Comparator.comparing(constructRelationshipDTO -> constructRelationshipDTO.getConstructDTO().getName()))
            .toList();
    }

    @ResponseBody
    @RequestMapping(value = "/{publicationID}/constructs")
    public List<ConstructDTO> getConstructsForPub(@PathVariable String publicationID) {
        return getMarkerRepository().getConstructsForAttribution(publicationID).stream()
                .filter(m -> m.getZdbID().contains("CONSTRCT"))
                .map(DTOConversionService::convertToConstructDTO)
                .sorted(Comparator.comparing(ConstructDTO::getName))
                .toList();
    }

    @Getter
    @Setter
    static class ChromosomeLinkage {
        @JsonView(View.API.class)
        private EntityZdbID entity;
        @JsonView(View.API.class)
        private String chromosome;
    }
}

