package org.zfin.ontology.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.anatomy.presentation.AnatomySearchBean;
import org.zfin.anatomy.service.AnatomyService;
import org.zfin.database.DbSystemUtil;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.mutant.presentation.AntibodyStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/ontology")
public class TermAPIController {

    @Autowired
    private OntologyRepository ontologyRepository;

    @Autowired
    private HttpServletRequest request;

    @JsonView(View.API.class)
    @RequestMapping(value = "/{termID}/antibodies", method = RequestMethod.GET)
    public JsonResultResponse<AntibodyStatistics> getLabeledAntibodies(@PathVariable String termID,
                                                                       @RequestParam(value = "directAnnotation", required = false, defaultValue = "false") boolean directAnnotation,
                                                                       @Version Pagination pagination) {

        HibernateUtil.createTransaction();
        JsonResultResponse<AntibodyStatistics> response = new JsonResultResponse<>();
        response.setHttpServletRequest(request);
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return response;

        AnatomySearchBean form = new AnatomySearchBean();
        form.setAoTerm(term);
        retrieveAntibodyData(term, form, pagination, directAnnotation);
        response.setResults(form.getAntibodyStatistics());
        response.setTotal(form.getAntibodyCount());
        HibernateUtil.flushAndCommitCurrentSession();
        return response;
    }

    private void retrieveAntibodyData(GenericTerm aoTerm, AnatomySearchBean form, Pagination pagi, boolean directAnnotation) {

        PaginationBean pagination = new PaginationBean();
        pagination.setMaxDisplayRecords(pagi.getLimit());
        pagination.setPageInteger(pagi.getPage());
        PaginationResult<org.zfin.mutant.presentation.AntibodyStatistics> antibodies = AnatomyService.getAntibodyStatistics(aoTerm, pagination, !directAnnotation);
        form.setAntibodyStatistics(antibodies.getPopulatedResults());
        form.setAntibodyCount(antibodies.getTotalCount());
    }

}

