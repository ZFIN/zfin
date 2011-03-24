package org.zfin.publication.presentation;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.mutant.PhenotypeExperiment;
import org.zfin.mutant.repository.PhenotypeRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * This is called through an ajax call from the curation page (APG) when a publication is closed.
 * Temporary until we deal with the curation tracking in Java.
 */
public class ClosePublicationAjaxController extends MultiActionController {

    /**
     * Check if there are any unspecified phenotypes defined for a given publication.
     * If so, it sends out an email report to the owner of the curation to ensure no
     * phenotypes are left undone inadvertently.
     * This method is called upon closing curation for the given publication.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @return simple acknowledgement
     * @throws ServletException exception
     */
    public ModelAndView closePublicationHandler(HttpServletRequest request, HttpServletResponse response, UnfinishedPhenotypeBean bean)
            throws ServletException {
        String publicationID = request.getParameter("publicationID");
        PhenotypeRepository phenotypeRepository = RepositoryFactory.getPhenotypeRepository();
        List<PhenotypeExperiment> phenotypeExperiments = phenotypeRepository.getPhenotypeExperimentsWithoutAnnotation(publicationID);
        // no response needed.
        bean = new UnfinishedPhenotypeBean();
        bean.setPhenotypeExperiments(phenotypeExperiments);
        return new ModelAndView("close-curation.response.ajax", LookupStrings.FORM_BEAN, bean);
    }
}
