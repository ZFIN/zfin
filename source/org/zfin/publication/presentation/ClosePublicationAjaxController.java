package org.zfin.publication.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.mutant.PhenotypeExperiment;
import org.zfin.mutant.repository.PhenotypeRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.ServletException;
import java.util.List;

/**
 * This is called through an ajax call from the curation page (APG) when a publication is closed.
 * Temporary until we deal with the curation tracking in Java.
 */
@Controller
public class ClosePublicationAjaxController {

    /**
     * Check if there are any unspecified phenotypes defined for a given publication.
     * If so, it sends out an email report to the owner of the curation to ensure no
     * phenotypes are left undone inadvertently.
     * This method is called upon closing curation for the given publication.
     *
     * @return simple acknowledgement
     * @throws ServletException exception
     */
    @RequestMapping("/publication/close-curation")
    protected String closeCuration(@RequestParam(required = true) String publicationID,
                                   @ModelAttribute("formBean") UnfinishedPhenotypeBean bean) throws Exception {
        PhenotypeRepository phenotypeRepository = RepositoryFactory.getPhenotypeRepository();
        List<PhenotypeExperiment> phenotypeExperiments = phenotypeRepository.getPhenotypeExperimentsWithoutAnnotation(publicationID);
        // no response needed.
        bean.setPhenotypeExperiments(phenotypeExperiments);
        return "close-curation.response.ajax";
    }
}
