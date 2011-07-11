package org.zfin.ontology.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.repository.RepositoryFactory;

/**
 * Controller that serves meta information about the ontologies.
 */
@Controller
public class OntologyController {

    private static final Logger log = Logger.getLogger(OntologyController.class);

    @RequestMapping("/version-info")
    private String retrieveVersionInfo(Model model) {
        OntologyBean form = new OntologyBean();
        model.addAttribute("formBean", form);
        OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
        form.setMetadataList(ontologyRepository.getAllOntologyMetadata());
        return "ontology/version-info.page";
    }

    @RequestMapping("/reload-ontology")
    private String forceOntologyLoad(@RequestParam(required = true) String ontologyName,
                                     Model model) {
        Ontology ontology = Ontology.getOntology(ontologyName);
        if(ontology == null){
            model.addAttribute(LookupStrings.ZDB_ID, ontologyName);
            return "record-not-found.popup";
        }
        OntologyManager.getInstance().reloadOntology(ontology);
        return "redirect:version-info";
    }

}