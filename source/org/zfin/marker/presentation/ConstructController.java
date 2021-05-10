package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.framework.api.FieldFilter;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.View;
import org.zfin.marker.service.ConstructService;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/api")
@Log4j2
@Repository
public class ConstructController {

    @Autowired
    private ConstructService constructService;

    @Autowired
    private HttpServletRequest request;

    @JsonView(View.SequenceAPI.class)
    @RequestMapping(value = "/marker/{zdbID}/constructs")
    public JsonResultResponse<ConstructInfo> getConstructView(@PathVariable("zdbID") String zdbID,
                                                              @RequestParam(value = "filter.name", required = false) String filterName,
                                                              @RequestParam(value = "filter.regulatoryRegion", required = false) String filterRegulatoryRegion,
                                                              @RequestParam(value = "filter.codingSequence", required = false) String filterCodingSequence,
                                                              @RequestParam(value = "filter.species", required = false) String filterSpecies,
                                                              @Version Pagination pagination) throws IOException, SolrServerException {
        pagination.addFieldFilter(FieldFilter.NAME, filterName);
        pagination.addFieldFilter(FieldFilter.REGULATORY_REGION, filterRegulatoryRegion);
        pagination.addFieldFilter(FieldFilter.CODING_SEQUENCE, filterCodingSequence);
        pagination.addFieldFilter(FieldFilter.SPECIES, filterSpecies);
        JsonResultResponse<ConstructInfo> response = constructService.getConstructs(zdbID, pagination);
        response.setHttpServletRequest(request);

        return response;
    }

}