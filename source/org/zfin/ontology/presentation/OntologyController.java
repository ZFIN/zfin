package org.zfin.ontology.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.OntologyMetadata;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.repository.RepositoryFactory;

/**
 * Controller that serves meta information about the ontologies.
 */
@Controller
@RequestMapping("/ontology")
public class OntologyController {

    private static final Logger LOG = LogManager.getLogger(OntologyController.class);

    @RequestMapping("/version-info")
    private String retrieveVersionInfo(Model model) {
        OntologyBean form = new OntologyBean();
        model.addAttribute("formBean", form);
        OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
        form.setMetadataList(ontologyRepository.getAllOntologyMetadata());
        return "ontology/version-info";
    }

    @RequestMapping("/reload-ontology")
    private String forceOntologyLoad(@RequestParam(required = true) String ontologyName,
                                     Model model) {
        Ontology ontology = Ontology.getOntology(ontologyName);
        if (ontology == null) {
            model.addAttribute(LookupStrings.ZDB_ID, ontologyName);
            return LookupStrings.RECORD_NOT_FOUND_POPUP;
        }
        OntologyManager.getInstance().reloadOntology(ontology);
        return "redirect:version-info";
    }

    @RequestMapping("/unset-ontology/{ontologyName}")
    private String unsetOntology(@PathVariable String ontologyName,
                                 Model model) {
        Ontology ontology = Ontology.getOntology(ontologyName);
        if (ontology == null) {
            model.addAttribute(LookupStrings.ZDB_ID, ontologyName);
            return LookupStrings.RECORD_NOT_FOUND_POPUP;
        }
        OntologyMetadata metadata = RepositoryFactory.getOntologyRepository().getOntologyMetadata(ontologyName);
        try {
            HibernateUtil.createTransaction();
            // set author to something different than it is to allow re-loading
            metadata.setSavedBy("me");
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            LOG.error(e);
        } finally {
            HibernateUtil.closeSession();
        }

        return "redirect:/version-info";
    }

}