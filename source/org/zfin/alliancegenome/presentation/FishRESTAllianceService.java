package org.zfin.alliancegenome.presentation;

import lombok.extern.log4j.Log4j2;
import org.alliancegenome.curation_api.model.entities.AffectedGenomicModel;
import org.alliancegenome.curation_api.response.SearchResponse;
import org.springframework.stereotype.Service;
import org.zfin.alliancegenome.AllianceRestManager;
import org.zfin.alliancegenome.RestAllianceService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Log4j2
@Service
public class FishRESTAllianceService extends RestAllianceService {

    private final FishRESTInterfaceAlliance api = AllianceRestManager.getAGMPRodEndpoints();

    public List<AffectedGenomicModel> getAGM() {
        List<AffectedGenomicModel> models = new ArrayList<>();
        try {
            HashMap<String, Object> options = new HashMap<>();
            options.put("queryString", "zfin");
            HashMap<String, Object> dataProviderFilterMap = new HashMap<>();
            dataProviderFilterMap.put("dataProvider.sourceOrganization.abbreviation", options);
            HashMap<String, Object> providerFilterMap = new HashMap<>();
            providerFilterMap.put("dataProviderFilter", dataProviderFilterMap);
            HashMap<String, Object> params = new HashMap<>();
            params.put("searchFilters", providerFilterMap);
            models = runBatchQuery(params, 10_000);
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.getCause().getLocalizedMessage();
            log.error("Could not search for Affected Genomic Model (Fish) at Alliance: " + message);
        }
        return models;
    }

    private List<AffectedGenomicModel> runBatchQuery(HashMap<String, Object> params, int batchSize) {
        int page = 0;
        int pages;
        List<AffectedGenomicModel> models = new ArrayList<>();
        do {
            SearchResponse<AffectedGenomicModel> searchResponse = api.search(token, page, batchSize, params);
            models.addAll(searchResponse.getResults());
            pages = (int) (searchResponse.getTotalResults() / batchSize);
            page++;
        } while (page <= pages);
        return models;
    }

}
