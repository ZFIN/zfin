package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.api.FieldFilter;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.orthology.presentation.OrthologDTO;
import org.zfin.orthology.presentation.OrthologyController;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.service.SequenceService;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api")
@Log4j2
@Repository
public class SequenceController {

    public SequenceController() {
    }

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private OrthologyController orthologyController;

    @Autowired
    private HttpServletRequest request;

    @JsonView(View.SequenceAPI.class)
    @RequestMapping(value = "/marker/{zdbID}/sequences")
    public JsonResultResponse<MarkerDBLink> getSequenceView(@PathVariable("zdbID") String zdbID,
                                                            @RequestParam(value = "filter.type", required = false) String type,
                                                            @RequestParam(value = "filter.accession", required = false) String accessionNumber,
                                                            @RequestParam(value = "filter.length", required = false) String length,
                                                            @Version Pagination pagination) {
        pagination.addFieldFilter(FieldFilter.SEQUENCE_ACCESSION, accessionNumber);
        pagination.addFieldFilter(FieldFilter.SEQUENCE_TYPE, type);

        JsonResultResponse<MarkerDBLink> response = sequenceService.getMarkerDBLinkJsonResultResponse(zdbID, pagination);
        response.setHttpServletRequest(request);

        return response;
    }

    @JsonView(View.OrthologyAPI.class)
    @RequestMapping(value = "/gene/{geneID}/orthologs", method = RequestMethod.GET)
    public JsonResultResponse<OrthologDTO> listOrthologsApi(@PathVariable String geneID) throws InvalidWebRequestException {
        JsonResultResponse<OrthologDTO> response = new JsonResultResponse<>();
        List<OrthologDTO> list = orthologyController.listOrthologs(geneID);
        if (list != null) {
            response.setResults(list);
            response.setTotal(list.size());
        }
        response.setHttpServletRequest(request);
        return response;
    }


}