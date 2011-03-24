package org.zfin.ontology.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.infrastructure.PatriciaTrieMultiMap;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.Term;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

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

}