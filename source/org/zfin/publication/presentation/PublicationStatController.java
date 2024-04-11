package org.zfin.publication.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.framework.api.FieldFilter;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.View;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.stats.StatisticPublicationService;
import org.zfin.stats.StatisticRow;
import org.zfin.wiki.presentation.Version;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/publication/stats")
public class PublicationStatController {

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private HttpServletRequest request;

/*
    @JsonView(View.API.class)
    @RequestMapping(value = "/marker/{type}", method = RequestMethod.GET)
    public JsonResultResponse<PublicationStat> getPublicationMutation(@PathVariable String type,
                                                                      @Version Pagination pagination) {

        List<String> markerType = null;
        if (type.equals("marker"))
            markerType = List.of(Marker.TypeGroup.GENEDOM.name());
        if (type.equals("antibody"))
            markerType = List.of(Marker.TypeGroup.ATB.name());
        if (type.equals("str")) {
            markerType = List.of(Marker.TypeGroup.MRPHLNO.name(),
                    Marker.TypeGroup.TALEN.name(),
                    Marker.TypeGroup.CRISPR.name());
        }
        Map<Publication, Integer> histogramPubMarker = publicationRepository.getPublicationMarkerHistogram(markerType, pagination);
        List<PublicationStat> stats = histogramPubMarker.entrySet().stream()
                .map(entry -> {
                    PublicationStat stat = new PublicationStat();
                    stat.setMarkerCount(entry.getValue());
                    stat.setPublication(entry.getKey());
                    return stat;
                }).collect(Collectors.toList());

        JsonResultResponse<PublicationStat> response = new JsonResultResponse<>();
        response.setTotal(stats.size());
        List<PublicationStat> paginatedList = stats.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList());

        response.setResults(paginatedList);
        response.setHttpServletRequest(request);
        return response;
    }
*/

    @JsonView(View.API.class)
    @RequestMapping(value = "/antibody/histogram", method = RequestMethod.GET)
    public JsonResultResponse<StatisticRow> getPublicationStats(@RequestParam(value = "filter.publicationID", required = false) String publicationID,
                                                                @RequestParam(value = "filter.antibodyName", required = false) String antibodyName,
                                                                @RequestParam(value = "filter.clonalType", required = false) String clonalType,
                                                                @RequestParam(value = "filter.isotype", required = false) String isotype,
                                                                @RequestParam(value = "filter.hostOrganism", required = false) String host,
                                                                @RequestParam(value = "filter.assay", required = false) String assay,
                                                                @RequestParam(value = "filter.antigenGenes", required = false) String antigenGenes,
                                                                @RequestParam(value = "cardinalitySort.assay", required = false) String cardinalitySortAssay,
                                                                @RequestParam(value = "cardinalitySort.antigenGenes", required = false) String cardinalitySortAntigenGenes,
                                                                @RequestParam(value = "multiplicitySort.antibody", required = false) String multiplicitySortAntibody,
                                                                @Version Pagination pagination) {

        pagination.addFieldFilter(FieldFilter.ANTIBODY_NAME, antibodyName);
        pagination.addFieldFilter(FieldFilter.CLONAL_TYPE, clonalType);
        pagination.addFieldFilter(FieldFilter.ISOTYPE, isotype);
        pagination.addFieldFilter(FieldFilter.HOST, host);
        pagination.addFieldFilter(FieldFilter.ASSAY, assay);
        pagination.addFieldFilter(FieldFilter.ANTIGEN_GENE, antigenGenes);
        pagination.addFieldSorting(FieldFilter.ASSAY, cardinalitySortAssay);
        pagination.addFieldSorting(FieldFilter.ANTIGEN_GENE, cardinalitySortAntigenGenes);
        pagination.addFieldSorting(FieldFilter.ANTIBODY_NAME, multiplicitySortAntibody);
        StatisticPublicationService service = new StatisticPublicationService();
        JsonResultResponse<StatisticRow> response = service.getAllPublicationAntibodies(pagination);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/expression/histogram", method = RequestMethod.GET)
    public JsonResultResponse<StatisticRow> getPublicationStatsExpression(@RequestParam(value = "filter.publicationID", required = false) String publicationID,
                                                                          @RequestParam(value = "filter.antibodyName", required = false) String antibodyName,
                                                                          @RequestParam(value = "filter.clonalType", required = false) String clonalType,
                                                                          @RequestParam(value = "filter.isotype", required = false) String isotype,
                                                                          @RequestParam(value = "filter.hostOrganism", required = false) String host,
                                                                          @RequestParam(value = "filter.assay", required = false) String assay,
                                                                          @RequestParam(value = "filter.antigenGenes", required = false) String antigenGenes,
                                                                          @RequestParam(value = "cardinalitySort.assay", required = false) String cardinalitySortAssay,
                                                                          @RequestParam(value = "cardinalitySort.antigenGenes", required = false) String cardinalitySortAntigenGenes,
                                                                          @RequestParam(value = "cardinalitySort.antibody", required = false) String cardinalitySortAntibody,
                                                                          @RequestParam(value = "cardinalitySort.qualifier", required = false) String cardinalitySortQualifier,
                                                                          @RequestParam(value = "multiplicitySort.antibody", required = false) String multiplicitySortAntibody,
                                                                          @Version Pagination pagination) {

        pagination.addFieldFilter(FieldFilter.ANTIBODY_NAME, antibodyName);
        pagination.addFieldFilter(FieldFilter.CLONAL_TYPE, clonalType);
        pagination.addFieldFilter(FieldFilter.ISOTYPE, isotype);
        pagination.addFieldFilter(FieldFilter.HOST, host);
        pagination.addFieldFilter(FieldFilter.ASSAY, assay);
        pagination.addFieldFilter(FieldFilter.ANTIGEN_GENE, antigenGenes);
        pagination.addFieldSorting(FieldFilter.ASSAY, cardinalitySortAssay);
        pagination.addFieldSorting(FieldFilter.ANTIGEN_GENE, cardinalitySortAntigenGenes);
        pagination.addFieldSorting(FieldFilter.ANTIBODY_NAME, multiplicitySortAntibody);
        pagination.addFieldSorting(FieldFilter.ANTIBODY, cardinalitySortAntibody);
        pagination.addFieldSorting(FieldFilter.QUALIFIER, cardinalitySortQualifier);
        StatisticPublicationService service = new StatisticPublicationService();
        JsonResultResponse<StatisticRow> response = service.getAllPublicationExpression(pagination);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/str/histogram", method = RequestMethod.GET)
    public JsonResultResponse<StatisticRow> getPublicationSTRStats(@RequestParam(value = "filter.publicationID", required = false) String publicationID,
                                                                   @RequestParam(value = "filter.reagentType", required = false) String reagentType,
                                                                   @RequestParam(value = "filter.clonalType", required = false) String clonalType,
                                                                   @RequestParam(value = "multiplicitySort.antibody", required = false) String multiplicitySortAntibody,
                                                                   @Version Pagination pagination) {

        if (reagentType != null) {
            pagination.addFieldFilter(FieldFilter.STR_TYPE, reagentType);
        }
        pagination.addFieldFilter(FieldFilter.CLONAL_TYPE, clonalType);
        pagination.addFieldSorting(FieldFilter.ANTIBODY_NAME, multiplicitySortAntibody);
        StatisticPublicationService service = new StatisticPublicationService();
        JsonResultResponse<StatisticRow> response = service.getAllPublicationStrs(pagination);
        response.setHttpServletRequest(request);
        return response;
    }

/*
    @JsonView(View.API.class)
    @RequestMapping(value = "/gene/histogram", method = RequestMethod.GET)
    public JsonResultResponse<StatisticRow> getPublicationGeneStats(@RequestParam(value = "filter.publicationID", required = false) String publicationID,
                                                                    @RequestParam(value = "filter.antibodyName", required = false) String antibodyName,
                                                                    @RequestParam(value = "multiplicitySort.antibody", required = false) String multiplicitySortAntibody,
                                                                    @Version Pagination pagination) {

        pagination.addFieldFilter(FieldFilter.ANTIBODY_NAME, antibodyName);
        pagination.addFieldSorting(FieldFilter.ANTIBODY_NAME, multiplicitySortAntibody);
        StatisticPublicationService service = new StatisticPublicationService();
        JsonResultResponse<StatisticRow> response = service.getAllPublicationGenes(pagination);
        response.setHttpServletRequest(request);
        return response;
    }
*/

    @JsonView(View.API.class)
    @RequestMapping(value = "/zebrashare/histogram", method = RequestMethod.GET)
    public JsonResultResponse<StatisticRow> getPublicationZebrashareStats(@RequestParam(value = "filter.publicationID", required = false) String publicationID,
                                                                          @RequestParam(value = "multiplicitySort.antibody", required = false) String multiplicitySortAntibody,
                                                                          @Version Pagination pagination) {

        pagination.addFieldSorting(FieldFilter.ANTIBODY_NAME, multiplicitySortAntibody);
        StatisticPublicationService service = new StatisticPublicationService();
        JsonResultResponse<StatisticRow> response = service.getAllZebrashareStats(pagination);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/datasets/histogram", method = RequestMethod.GET)
    public JsonResultResponse<StatisticRow> getPublicationDatasetsStats(@RequestParam(value = "filter.publicationID", required = false) String publicationID,
                                                                        @RequestParam(value = "multiplicitySort.antibody", required = false) String multiplicitySortAntibody,
                                                                        @Version Pagination pagination) {

        pagination.addFieldSorting(FieldFilter.ANTIBODY_NAME, multiplicitySortAntibody);
        StatisticPublicationService service = new StatisticPublicationService();
        JsonResultResponse<StatisticRow> response = service.getDataSetsStats(pagination);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/probe/histogram", method = RequestMethod.GET)
    public JsonResultResponse<StatisticRow> getPublicationProbes(@RequestParam(value = "filter.publicationID", required = false) String publicationID,
                                                                 @RequestParam(value = "multiplicitySort.antibody", required = false) String multiplicitySortAntibody,
                                                                 @Version Pagination pagination) {

        pagination.addFieldSorting(FieldFilter.ANTIBODY_NAME, multiplicitySortAntibody);
        StatisticPublicationService service = new StatisticPublicationService();
        JsonResultResponse<StatisticRow> response = service.getAllProbeStats(pagination);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/disease/histogram", method = RequestMethod.GET)
    public JsonResultResponse<StatisticRow> getPublicationDisease(@RequestParam(value = "filter.publicationID", required = false) String publicationID,
                                                                  @RequestParam(value = "multiplicitySort.antibody", required = false) String multiplicitySortAntibody,
                                                                  @RequestParam(value = "cardinalitySortSort.environment", required = false) String cardinalitySortEnvironment,
                                                                  @Version Pagination pagination) {

        //pagination.addFieldSorting(FieldFilter.ANTIBODY_NAME, multiplicitySortAntibody);
        StatisticPublicationService service = new StatisticPublicationService();
        pagination.addFieldFilter(FieldFilter.PUBLICATION_ID, publicationID);
        //pagination.addFieldSorting();
        JsonResultResponse<StatisticRow> response = service.getAllDiseaseStats(pagination);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/fish/histogram", method = RequestMethod.GET)
    public JsonResultResponse<StatisticRow> getPublicationFishStats(@RequestParam(value = "filter.publicationID", required = false) String publicationID,
                                                                    @RequestParam(value = "filter.pubType", required = false) String publicationType,
                                                                    @RequestParam(value = "filter.pubShortAuthor", required = false) String publicationAuthor,
                                                                    @RequestParam(value = "filter.wildType", required = false) String wildType,
                                                                    @RequestParam(value = "filter.fishName", required = false) String fishName,
                                                                    @Version Pagination pagination) {

        pagination.addFieldFilter(FieldFilter.PUBLICATION_ID, publicationID);
        pagination.addFieldFilter(FieldFilter.PUBLICATION_TYPE, publicationType);
        pagination.addFieldFilter(FieldFilter.PUBLICATION_AUTHOR, publicationAuthor);
        pagination.addFieldFilter(FieldFilter.FISH_NAME, fishName);
        pagination.addFieldFilter(FieldFilter.FISH_TYPE, wildType);
        StatisticPublicationService service = new StatisticPublicationService();
        JsonResultResponse<StatisticRow> response = service.getAllFishStats(pagination);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/attribution/histogram", method = RequestMethod.GET)
    public JsonResultResponse<StatisticRow> getPublicationAttributionStats(@RequestParam(value = "filter.publicationID", required = false) String publicationID,
                                                                           @RequestParam(value = "filter.pubType", required = false) String publicationType,
                                                                           @RequestParam(value = "filter.pubShortAuthor", required = false) String publicationAuthor,
                                                                           @RequestParam(value = "filter.zdbEntityType", required = false) String zdbEntityId,
                                                                           @Version Pagination pagination) {

        pagination.addFieldFilter(FieldFilter.PUBLICATION_ID, publicationID);
        pagination.addFieldFilter(FieldFilter.PUBLICATION_TYPE, publicationType);
        pagination.addFieldFilter(FieldFilter.PUBLICATION_AUTHOR, publicationAuthor);
        pagination.addFieldFilter(FieldFilter.ZDB_ENTITY_TYPE, zdbEntityId);
        StatisticPublicationService service = new StatisticPublicationService();
        JsonResultResponse<StatisticRow> response = service.getAllAttributionStats(pagination);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/mutation/histogram", method = RequestMethod.GET)
    public JsonResultResponse<StatisticRow> getPublicationMutationStats(@RequestParam(value = "filter.publicationID", required = false) String publicationID,
                                                                        @RequestParam(value = "filter.antibodyName", required = false) String antibodyName,
                                                                        @RequestParam(value = "filter.clonalType", required = false) String clonalType,
                                                                        @RequestParam(value = "filter.isotype", required = false) String isotype,
                                                                        @RequestParam(value = "filter.hostOrganism", required = false) String host,
                                                                        @RequestParam(value = "filter.assay", required = false) String assay,
                                                                        @RequestParam(value = "filter.antigenGenes", required = false) String antigenGenes,
                                                                        @RequestParam(value = "cardinalitySort.assay", required = false) String cardinalitySortAssay,
                                                                        @RequestParam(value = "cardinalitySort.antigenGenes", required = false) String cardinalitySortAntigenGenes,
                                                                        @RequestParam(value = "multiplicitySort.antibody", required = false) String multiplicitySortAntibody,
                                                                        @Version Pagination pagination) {

        pagination.addFieldFilter(FieldFilter.ANTIBODY_NAME, antibodyName);
        pagination.addFieldFilter(FieldFilter.CLONAL_TYPE, clonalType);
        pagination.addFieldFilter(FieldFilter.ISOTYPE, isotype);
        pagination.addFieldFilter(FieldFilter.HOST, host);
        pagination.addFieldFilter(FieldFilter.ASSAY, assay);
        pagination.addFieldFilter(FieldFilter.ANTIGEN_GENE, antigenGenes);
        pagination.addFieldSorting(FieldFilter.ASSAY, cardinalitySortAssay);
        pagination.addFieldSorting(FieldFilter.ANTIGEN_GENE, cardinalitySortAntigenGenes);
        pagination.addFieldSorting(FieldFilter.ANTIBODY_NAME, multiplicitySortAntibody);
        StatisticPublicationService service = new StatisticPublicationService();
        JsonResultResponse<StatisticRow> response = service.getAllPublicationMutation(pagination);
        response.setHttpServletRequest(request);
        return response;
    }


}

