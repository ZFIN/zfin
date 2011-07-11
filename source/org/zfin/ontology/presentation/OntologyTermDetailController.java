package org.zfin.ontology.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.ActiveData;
import org.zfin.ontology.*;
import org.zfin.ontology.service.OntologyService;
import org.zfin.repository.RepositoryFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generic entry point for viewing a term detail page.
 */
@Controller
public class OntologyTermDetailController {


    @RequestMapping("/term-detail")
    protected String termDetailPage(@RequestParam String termID,
                                    @RequestParam(required = false) String ontologyName,
                                    Model model) throws Exception {

        if (termID == null) {
            model.addAttribute(LookupStrings.ZDB_ID, termID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE ;
        }
        //redirect to anatomy detail page for ANAT zdbIDs
        if (ActiveData.isValidActiveData(termID, ActiveData.Type.ANAT))
            return "redirect:/action/anatomy/term-detail?anatomyItem.zdbID=" + termID;

        OntologyBean form = new OntologyBean();
        GenericTerm term = null;
        if (termID.contains(ActiveData.Type.TERM.name())) {
            term = RepositoryFactory.getInfrastructureRepository().getTermByID(termID);
        } else {
            //try an obo id
            term = RepositoryFactory.getOntologyRepository().getTermByOboID(termID);
        }

        //if an anatomy term was linked by obo id, we still want to view it as an anatomy page
        //  (for now)
        if (term != null && term.getOntology().equals(Ontology.ANATOMY)) {
            return "redirect:/action/anatomy/term-detail?anatomyItem.zdbID=" + termID;
        }

        // check if the term name contains an asterisk at the end of the string, indicating that
        // we are looking for a list of terms matching the name
        if (termID.endsWith("*")) {
            String queryString = termID.substring(0, termID.indexOf("*"));
            Ontology ontology = null;
            try {
                ontology = Ontology.getOntology(ontologyName);
            } catch (Exception e) {
                // ignore
            }
            MatchingTermService matcher = new MatchingTermService();
            Set<MatchingTerm> terms = matcher.getMatchingTerms(queryString, ontology);
            if (terms == null)
                terms = new HashSet<MatchingTerm>(0);
            model.addAttribute("terms", terms);
            model.addAttribute("query", queryString);
            model.addAttribute("ontology", ontology);
            return "ontology/term-list.page";
        }

        // try by name
        if (term == null && ontologyName != null) {
            Ontology ontology = Ontology.getOntology(ontologyName);
            term = RepositoryFactory.getOntologyRepository().getTermByName(termID, ontology);
        }

        //after all of that, we really just don't have it.
        if (term == null) {
            model.addAttribute(LookupStrings.ZDB_ID, termID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE ;
        }

        List<RelationshipPresentation> termRelationships = OntologyService.getRelatedTerms(term);
        Collections.sort(termRelationships);

        form.setTermRelationships(termRelationships);
        form.setTerm(term);
        model.addAttribute("formBean", form);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, term.getTermName());

        return "ontology/ontology-term.page";

    }

    @RequestMapping(value = {("/term-detail-popup")})
    public String getTermDetailPopup(@RequestParam String termID, Model model) {
        GenericTerm term = RepositoryFactory.getOntologyRepository().getTermByOboID(termID);
        if (term == null) {
            model.addAttribute(LookupStrings.ZDB_ID, termID);
            return "record-not-found.popup";
        }

        model.addAttribute("term", term);
        return "ontology/ontology-term-popup.popup";
    }

    @RequestMapping(value = {("/post-composed-term-detail")})
    public String getPostComposedTermDetailPage(@RequestParam String superTermID,
                                                @RequestParam String subTermID,
                                                Model model) {
        PostComposedEntity entity = new PostComposedEntity();
        entity.setSuperterm(RepositoryFactory.getOntologyRepository().getTermByOboID(superTermID));
        entity.setSubterm(RepositoryFactory.getOntologyRepository().getTermByOboID(subTermID));

        if (entity.getSuperterm() == null) {
            model.addAttribute(LookupStrings.ZDB_ID, superTermID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE ;
        }
        if (entity.getSubterm() == null) {
            model.addAttribute(LookupStrings.ZDB_ID, subTermID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE ;
        }

        model.addAttribute("entity", entity);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Post-Composed Term: " +
                entity.getSuperterm().getTermName() + " " + entity.getSubterm().getTermName());
        return "ontology/post-composed-term-detail.page";
    }

    @RequestMapping(value = {("/post-composed-term-detail-popup")})
    public String getPostComposedTermDetailPopup(@RequestParam String superTermID,
                                                 @RequestParam String subTermID,
                                                 Model model) {
        PostComposedEntity entity = new PostComposedEntity();
        entity.setSuperterm(RepositoryFactory.getOntologyRepository().getTermByOboID(superTermID));
        entity.setSubterm(RepositoryFactory.getOntologyRepository().getTermByOboID(subTermID));

        if (entity.getSuperterm() == null) {
            model.addAttribute(LookupStrings.ZDB_ID, superTermID);
            return "record-not-found.popup";
        }
        if (entity.getSubterm() == null) {
            model.addAttribute(LookupStrings.ZDB_ID, subTermID);
            return "record-not-found.popup";
        }

        model.addAttribute("entity", entity);
        return "ontology/post-composed-term-detail-popup.popup";
    }

}
