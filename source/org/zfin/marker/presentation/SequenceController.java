package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.FieldFilter;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.orthology.Ortholog;
import org.zfin.orthology.presentation.OrthologDTO;
import org.zfin.orthology.presentation.OrthologyController;
import org.zfin.orthology.repository.OrthologyRepository;
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
    private OrthologyRepository orthologyRepository;

    @Autowired
    private HttpServletRequest request;

    @JsonView(View.SequenceAPI.class)
    @RequestMapping(value = "/marker/{zdbID}/sequences")
    public JsonResultResponse<MarkerDBLink> getSequenceView(@PathVariable("zdbID") String zdbID,
                                                            @RequestParam(value = "summary", required = false, defaultValue = "false") boolean summary,
                                                            @RequestParam(value = "filter.type", required = false) String type,
                                                            @RequestParam(value = "filter.accession", required = false) String accessionNumber,
                                                            @Version Pagination pagination) {
        pagination.addFieldFilter(FieldFilter.SEQUENCE_ACCESSION, accessionNumber);
        pagination.addFieldFilter(FieldFilter.SEQUENCE_TYPE, type);
        JsonResultResponse<MarkerDBLink> response = sequenceService.getMarkerDBLinkJsonResultResponse(zdbID, pagination, summary);
        response.setHttpServletRequest(request);
        return response;
    }



    @JsonView(View.OrthologyAPI.class)
    @RequestMapping(value = "/marker/{geneID}/orthologs", method = RequestMethod.GET)
    public JsonResultResponse<OrthologDTO> listOrthologsApi(@PathVariable String geneID) throws InvalidWebRequestException {
        JsonResultResponse<OrthologDTO> response = new JsonResultResponse<>();
        List<OrthologDTO> list = orthologyController.listOrthologs(geneID);
        if (list != null) {
            response.setResults(list);
            response.setTotal(list.size());
        }
        response.addSupplementalData("evidenceCodes", orthologyController.listOrthologyEvidenceCodes());
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.OrthologyAPI.class)
    @RequestMapping(value = "/marker/{markerZdbID}/orthologs", method = RequestMethod.POST)
    public OrthologDTO addOrtholog(@PathVariable String markerZdbID, @RequestBody OrthologDTO newOrtholog) {
        return orthologyController.createOrthologFromNcbi(markerZdbID, newOrtholog.getNcbiOtherSpeciesGeneDTO().getID());
    }

    @RequestMapping(value = "/marker/orthologs/{orthoZdbID}", method = RequestMethod.DELETE, produces = "text/plain")
    public String deleteOrtholog(@PathVariable String orthoZdbID) {
        Ortholog ortholog = orthologyRepository.getOrtholog(orthoZdbID);
        if (ortholog == null) {
            throw new InvalidWebRequestException("No Ortholog with ID " + orthoZdbID + " found", null);
        }

        Transaction tx = null;
        try {
            tx = HibernateUtil.createTransaction();
            orthologyRepository.deleteOrtholog(ortholog);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw new InvalidWebRequestException("Error while deleting Ortholog: " + orthoZdbID + ": " + e.getMessage(), null);
        }
        return "OK";
    }
}