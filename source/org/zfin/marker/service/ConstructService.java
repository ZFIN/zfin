package org.zfin.marker.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.ConstructRelationship;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.FieldFilter;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.gwt.root.dto.ConstructRelationshipDTO;
import org.zfin.infrastructure.ControlledVocab;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.presentation.ConstructInfo;
import org.zfin.marker.presentation.MarkerRelationshipPresentation;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.search.Category;
import org.zfin.search.FieldName;
import org.zfin.search.service.SolrService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

@Service
public class ConstructService {

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    public JsonResultResponse<ConstructInfo> getConstructs(String zdbID, Pagination pagination) throws IOException, SolrServerException {
        long startTime = System.currentTimeMillis();

        ControlledVocab zebrafish = new ControlledVocab();
        zebrafish.setCvTermName("Dre.");
        zebrafish.setCvForeignSpecies("zebrafish");
        zebrafish.setCvNameDefinition("Danio rerio");

        SolrQuery query = new SolrQuery();
        query.setFilterQueries(
                FieldName.CATEGORY.getName() + ":" + Category.CONSTRUCT.getName(),
                FieldName.RELATED_GENE_ZDB_ID.getName() + ":" + zdbID
        );
        addFilterQuery(query, FieldName.CONSTRUCT_NAME, pagination.getFieldFilter(FieldFilter.NAME));
        addFilterQuery(query, FieldName.REGULATORY_REGION_AC, pagination.getFieldFilter(FieldFilter.REGULATORY_REGION));
        addFilterQuery(query, FieldName.CODING_SEQUENCE_AC, pagination.getFieldFilter(FieldFilter.CODING_SEQUENCE));
        addFilterQuery(query, FieldName.RELATED_SPECIES_NAME_AC, pagination.getFieldFilter(FieldFilter.SPECIES));

        switch (StringUtils.defaultString(pagination.getSortBy())) {
            case "constructNameUp" -> query.addSort(SolrQuery.SortClause.asc(FieldName.NAME_SORT.getName()));
            case "constructNameDown" -> query.addSort(SolrQuery.SortClause.desc(FieldName.NAME_SORT.getName()));
            case "regulatoryRegionUp" -> query.addSort(SolrQuery.SortClause.asc(FieldName.REGULATORY_REGION_SORT.getName()));
            case "regulatoryRegionDown" -> query.addSort(SolrQuery.SortClause.desc(FieldName.REGULATORY_REGION_SORT.getName()));
            case "codingSequenceUp" -> query.addSort(SolrQuery.SortClause.asc(FieldName.CODING_SEQUENCE_SORT.getName()));
            case "codingSequenceDown" -> query.addSort(SolrQuery.SortClause.desc(FieldName.CODING_SEQUENCE_SORT.getName()));
            case "speciesUp" -> query.addSort(SolrQuery.SortClause.asc(FieldName.CONSTRUCT_SPECIES_SORT.getName()));
            case "speciesDown" -> query.addSort(SolrQuery.SortClause.desc(FieldName.CONSTRUCT_SPECIES_SORT.getName()));
            case "citationLeast" -> query.addSort(SolrQuery.SortClause.asc(FieldName.CONSTRUCT_CITATION_SORT.getName()));
            case "citationMost" -> query.addSort(SolrQuery.SortClause.desc(FieldName.CONSTRUCT_CITATION_SORT.getName()));
            default -> {
            }
            //query.addSort(SolrQuery.SortClause.asc(FieldName.REGULATORY_REGION_AC.getName()));
        }

        query.setFields(FieldName.ID.getName());
        query.setRows(pagination.getLimit());
        query.setStart(pagination.getStart());

        QueryResponse queryResponse = SolrService.getSolrClient().query(query);

        List<ConstructInfo> constructInfoList = queryResponse.getResults().stream()
                .map(doc -> (String) doc.getFieldValue(FieldName.ID.getName()))
                .map(markerRepository::getMarkerByID)
                .map(marker -> {
                    ConstructInfo info = new ConstructInfo();
                    info.setConstruct(marker);

                    // Needs refactor
                    List<MarkerRelationshipPresentation> relationships = new ArrayList<>(markerRepository.getRelatedMarkerOrderDisplayForTypes(
                            marker,
                            true,
                            MarkerRelationship.Type.PROMOTER_OF,
                            MarkerRelationship.Type.CODING_SEQUENCE_OF,
                            MarkerRelationship.Type.CONTAINS_REGION
                    ));

                    List<Marker> regulatoryRegions = relationships.stream()
                            // needs refactor: make enum of has Promoter
                            .filter(presentation -> presentation.getRelationshipType().equals("Has Promoter"))
                            .map(presentation -> markerRepository.getMarkerByID(presentation.getZdbId()))
                            .collect(Collectors.toList());

                    List<Marker> codingSequences = relationships.stream()
                            // needs refactor: make enum of Has Coding Sequence
                            .filter(presentation -> presentation.getRelationshipType().equals("Has Coding Sequence"))
                            .map(presentation -> markerRepository.getMarkerByID(presentation.getZdbId()))
                            .collect(Collectors.toList());

                    info.setRegulatoryRegions(regulatoryRegions);
                    info.setCodingSequences(codingSequences);
                    info.setNumberOfPublications(publicationRepository.getNumberAssociatedPublicationsForZdbID(marker.getZdbID()));

                    info.setNumberOfTransgeniclines(featureRepository.getNumberOfFeaturesForConstruct(marker));
                    List<ControlledVocab> species = infrastructureRepository.getControlledVocabsForSpeciesByConstruct(marker);
                    species.add(zebrafish);
                    species.sort(Comparator.comparing(ControlledVocab::getCvNameDefinition));
                    info.setSpecies(species);
                    return info;
                })
                .collect(Collectors.toList());

        JsonResultResponse<ConstructInfo> response = new JsonResultResponse<>();
        response.calculateRequestDuration(startTime);
        response.setTotal(queryResponse.getResults().getNumFound());
        response.setResults(constructInfoList);

        return response;
    }

    public static String addConstructMarkerRelationship(ConstructRelationshipDTO constructRelationshipDTO) {
        return addConstructMarkerRelationship(
                constructRelationshipDTO.getConstructDTO().getZdbID(),
                constructRelationshipDTO.getMarkerDTO().getZdbID(),
                constructRelationshipDTO.getRelationshipType(),
                constructRelationshipDTO.getPublicationZdbID());
    }

    public static String addConstructMarkerRelationship(String constructZdbID, String markerZdbID, String relationshipType, String publicationZdbID) {
        ConstructRelationship constructRelationship = new ConstructRelationship();
        MarkerRelationship markerRelationship = new MarkerRelationship();
        ConstructCuration construct = getConstructRepository().getConstructByID(constructZdbID);
        constructRelationship.setConstruct(construct);

        Marker marker = getMarkerRepository().getMarkerByID(markerZdbID);
        constructRelationship.setMarker(marker);

        constructRelationship.setType(ConstructRelationship.Type.getType(relationshipType));
        markerRelationship.setFirstMarker(getMarkerRepository().getMarkerByID(constructZdbID));
        markerRelationship.setSecondMarker(marker);
        markerRelationship.setType(MarkerRelationship.Type.getType(relationshipType));
        HibernateUtil.currentSession().save(constructRelationship);
        HibernateUtil.currentSession().save(markerRelationship);
        getInfrastructureRepository().insertPublicAttribution(constructRelationship.getZdbID(), publicationZdbID);
//        getInfrastructureRepository().insertPublicAttribution(markerRelationship.getZdbID(), publicationZdbID); ??
        HibernateUtil.currentSession().flush();
        return constructRelationship.getZdbID();
    }

    public static void deleteConstructRelationship(String constructRelationshipZdbID) {
        //delete from construct_marker_relationship
        ConstructRelationship crel = getConstructRepository().getConstructRelationshipByID(constructRelationshipZdbID);
        getInfrastructureRepository().insertUpdatesTable(constructRelationshipZdbID, "Construct", constructRelationshipZdbID, "",
                "deleted construct/marker relationship between: "
                        + crel.getConstruct().getName()
                        + " and "
                        + crel.getMarker().getName()
                        + " of type "
                        + crel.getType().getValue()
        );
        getInfrastructureRepository().deleteActiveDataByZdbID(constructRelationshipZdbID);

        //delete from marker_relationship
        Marker marker1 = getMarkerRepository().getMarker(crel.getConstruct().getZdbID());
        Marker marker2 = getMarkerRepository().getMarker(crel.getMarker().getZdbID());
        MarkerRelationship.Type relationshipType = MarkerRelationship.Type.getType(crel.getType().getValue());
        MarkerRelationship markerRelationship = getMarkerRepository().getMarkerRelationship(marker1, marker2, relationshipType);
        if (markerRelationship != null) {
            getInfrastructureRepository().deleteActiveDataByZdbID(markerRelationship.getZdbID());
        }
    }

    public static void deleteConstructRelationship(ConstructRelationshipDTO constructRelationshipDTO) {
        deleteConstructRelationship(constructRelationshipDTO.getZdbID());
    }

    private void addFilterQuery(SolrQuery query, FieldName field, String value) {
        if (StringUtils.isNotEmpty(value)) {
            query.addFilterQuery(field.getName() + ":(" + value + ")");
        }
    }
}



