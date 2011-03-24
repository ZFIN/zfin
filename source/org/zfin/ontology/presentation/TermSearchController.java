package org.zfin.ontology.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.Term;
import org.zfin.repository.RepositoryFactory;

import java.util.Set;

/**
 * Search for terms by given names, ...
 */
@Controller
public class TermSearchController {

    private static final Logger LOG = Logger.getLogger(TermSearchController.class);
    // These two variables are injected by Spring
    private String redirectUrlIfSingleResult;

    @RequestMapping("/term-search")
    protected String termDetailPage(@RequestParam String termName,
                                    Model model) throws Exception {

        if (termName == null) {
            model.addAttribute(LookupStrings.ZDB_ID, termName);
            return "record-not-found.page";
        }

        //Ontology ontology = Ontology.getOntology(ontologyName);
        ///Set<Term> terms = RepositoryFactory.getOntologyRepository().getTerm(termName);
        Set<Term> terms = null;
        if (terms != null && terms.size() == 1) {
            return "redirect:/action/ontology/term-detail?termID=" + terms.iterator().next().getOboID();
        }
        OntologyBean form = new OntologyBean();
        return null;
    }
}
