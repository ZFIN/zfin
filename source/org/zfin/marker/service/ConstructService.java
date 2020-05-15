package org.zfin.marker.service;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
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
                    List<MarkerRelationshipPresentation> mrkrRels = new ArrayList<>(markerRepository.getRelatedMarkerOrderDisplayForTypes(
                            marker,
                            true,
                            MarkerRelationship.Type.PROMOTER_OF,
                            MarkerRelationship.Type.CODING_SEQUENCE_OF,
                            MarkerRelationship.Type.CONTAINS_REGION
                    ));

                    List<Marker> regulatoryRegions = mrkrRels.stream()
                            // needs refactor: make enum of has Promoter
                            .filter(presentation -> presentation.getRelationshipType().equals("Has Promoter"))
                            .map(presentation -> markerRepository.getMarkerByID(presentation.getZdbId()))
                            .collect(Collectors.toList());

                    List<Marker> codingSequences = mrkrRels.stream()
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
}



