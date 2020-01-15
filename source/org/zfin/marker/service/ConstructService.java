package org.zfin.marker.service;

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
import org.zfin.repository.RepositoryFactory;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConstructService {

    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private FeatureRepository featureRepository = RepositoryFactory.getFeatureRepository();
    private InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();

    public JsonResultResponse<ConstructInfo> getConstructs(String zdbID, Pagination pagination) {
        long startTime = System.currentTimeMillis();
        Marker gene = markerRepository.getMarkerByID(zdbID);
        ControlledVocab zebrafish = new ControlledVocab();
        zebrafish.setCvTermName("Dre.");
        zebrafish.setCvForeignSpecies("zebrafish");
        zebrafish.setCvNameDefinition("Danio rerio");
        Set<MarkerRelationship.Type> types = new HashSet<>();
        types.add(MarkerRelationship.Type.PROMOTER_OF);
        types.add(MarkerRelationship.Type.CODING_SEQUENCE_OF);
        types.add(MarkerRelationship.Type.CONTAINS_REGION);
        Set<Marker> relatedMarkers = new TreeSet<>();
        relatedMarkers = MarkerService.getRelatedMarker(gene, types);
        List<ConstructInfo> constructInfoList = relatedMarkers.stream()
                .map(marker -> {
                    ConstructInfo info = new ConstructInfo();
                    info.setConstruct(marker);


                    // Needs refactor
                    List<MarkerRelationshipPresentation> mrkrRels = new ArrayList<>(markerRepository.getRelatedMarkerOrderDisplayForTypes(
                            marker, true
                            , MarkerRelationship.Type.PROMOTER_OF
                            , MarkerRelationship.Type.CODING_SEQUENCE_OF
                            , MarkerRelationship.Type.CONTAINS_REGION
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
                    info.setNumberOfPublications(RepositoryFactory.getPublicationRepository().getNumberAssociatedPublicationsForZdbID(marker.getZdbID()));
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
        response.setTotal(constructInfoList.size());

        // paginating
        response.setResults(constructInfoList.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList()));
        return response;
    }
}



