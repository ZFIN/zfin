package org.zfin.marker.fluorescence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;


public class FPBaseService {
    public static final String API_ID_ENDPOINT_TEMPLATE = "https://www.fpbase.org/api/proteins/?uuid__iexact=%s&format=json";
    public static final String API_NAME_ENDPOINT_TEMPLATE = "https://www.fpbase.org/api/proteins/?name__iexact=%s&format=json";
    public static final String API_NAME_CONTAINS_ENDPOINT_TEMPLATE = "https://www.fpbase.org/api/proteins/?name__icontains=%s&format=json";
    public static final String API_ALL_PROTEINS_ENDPOINT_TEMPLATE = "https://www.fpbase.org/api/proteins/?format=json";


    public List<FPBaseApiResultItem> lookupFPBaseProteinByID(String id) {
        try {
            String json = IOUtils.toString(new URL(String.format(API_ID_ENDPOINT_TEMPLATE, id)), StandardCharsets.UTF_8);
            return deserializeApiResponse(json);
        } catch (IOException e) {
            return null;
        }
    }

    public List<FPBaseApiResultItem> lookupFPBaseProteinByName(String name) {
        try {
            String json = IOUtils.toString(new URL(String.format(API_NAME_ENDPOINT_TEMPLATE, name)), StandardCharsets.UTF_8);
            return deserializeApiResponse(json);
        } catch (IOException e) {
            return null;
        }
    }

    public List<FPBaseApiResultItem> lookupFPBaseProteinByNameContains(String namePart) {
        try {
            String json = IOUtils.toString(new URL(String.format(API_NAME_CONTAINS_ENDPOINT_TEMPLATE, namePart)), StandardCharsets.UTF_8);
            return deserializeApiResponse(json);
        } catch (IOException e) {
            return null;
        }
    }

    public List<FPBaseApiResultItem> fetchAllProteinData() {
        try {
            String json = IOUtils.toString(new URL(API_ALL_PROTEINS_ENDPOINT_TEMPLATE), StandardCharsets.UTF_8);
            return deserializeApiResponse(json);
        } catch (IOException e) {
            return null;
        }
    }

    public List<FluorescentProtein> importMissingProteins() {
        List<FluorescentProtein> allExistingProteinsLocally = getMarkerRepository().getAllFluorescentProteins();
        Set<String> existingProteinUUIDs = allExistingProteinsLocally.stream().map(FluorescentProtein::getUuid).collect(Collectors.toSet());
        List<FluorescentProtein> imported = new ArrayList<>();

        List<FPBaseApiResultItem> upstreamProteins = fetchAllProteinData();
        for (FPBaseApiResultItem protein : upstreamProteins) {
            if (!existingProteinUUIDs.contains(protein.uuid())) {
                imported.addAll(importProtein(protein));
            }
        }
        return imported;
    }

    private List<FluorescentProtein> importProtein(FPBaseApiResultItem protein) {
        List<FluorescentProtein> newProteins = new ArrayList<>();
        int numStates = protein.states().size();
        for(FPBaseApiResultState state : protein.states()) {
            FluorescentProtein newProtein = new FluorescentProtein();
            newProtein.setUuid(protein.uuid());
            newProtein.setName(protein.name());
            if (numStates > 1) {
                newProtein.setName(protein.name() + " (" + state.name() + ")");
            }
            newProtein.setEmissionLength(state.em_max());
            newProtein.setExcitationLength(state.ex_max());
            getMarkerRepository().addFluorescentProtein(newProtein);
            newProteins.add(newProtein);
        }
        return newProteins;
    }

    public List<FPBaseApiResultItem> deserializeApiResponse(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<FPBaseApiResultItem> dataList = objectMapper.readValue(json, new TypeReference<>() {});
        return dataList;
    }

}
