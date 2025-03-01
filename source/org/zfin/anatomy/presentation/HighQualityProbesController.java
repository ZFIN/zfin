package org.zfin.anatomy.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.repository.RepositoryFactory;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Action class that serves the Thisse probes pages.
 */
@Controller
@RequestMapping("/ontology")
public class HighQualityProbesController {

    private static Logger LOG = LogManager.getLogger(HighQualityProbesController.class);

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private OntologyRepository ontologyRepository;

    @RequestMapping(value = "/show-high-quality-probes/{zdbID}")
    public String showHighQualityProbes(Model model,
                                        @ModelAttribute("formBean") AnatomySearchBean anatomyForm,
                                        @PathVariable("zdbID") String termID
    ) throws Exception {

        LOG.info("Start High Quality Probes Controller");
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null) {
            model.addAttribute(LookupStrings.ZDB_ID, "No term name found");
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        anatomyForm.setAoTerm(term);
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        anatomyForm.setRequestUrl(request.getRequestURL());

        PaginationResult<HighQualityProbe> hqp = markerRepository.getHighQualityProbeStatistics(term, anatomyForm, false);
        anatomyForm.setHighQualityProbeGenes(hqp.getPopulatedResults());
        anatomyForm.setNumberOfHighQualityProbes(hqp.getTotalCount());
        anatomyForm.setTotalRecords(hqp.getTotalCount());
        model.addAttribute(LookupStrings.FORM_BEAN, anatomyForm);

        return "anatomy/show-high-quality-probes";
    }

    @RequestMapping(value = "/show-high-quality-probes-substructures/{zdbID}")
    public String showHighQualityProbesSubstructures(Model model,
                                                     @ModelAttribute("formBean") AnatomySearchBean anatomyForm,
                                                     @PathVariable("zdbID") String termID
    ) throws Exception {

        LOG.info("Start High Quality Probes Controller");
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null) {
            model.addAttribute(LookupStrings.ZDB_ID, "No term name found");
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        anatomyForm.setAoTerm(term);
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        anatomyForm.setRequestUrl(request.getRequestURL());
        model.addAttribute("includingSubstructures", true);

        PaginationResult<HighQualityProbe> hqp = markerRepository.getHighQualityProbeStatistics(term, anatomyForm, true);
        anatomyForm.setHighQualityProbeGenes(hqp.getPopulatedResults());
        anatomyForm.setNumberOfHighQualityProbes(hqp.getTotalCount());
        anatomyForm.setTotalRecords(hqp.getTotalCount());
        model.addAttribute(LookupStrings.FORM_BEAN, anatomyForm);

        return "anatomy/show-high-quality-probes";
    }

}
