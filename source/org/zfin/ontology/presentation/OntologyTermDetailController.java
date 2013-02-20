package org.zfin.ontology.presentation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.anatomy.presentation.AnatomySearchBean;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.framework.presentation.SectionVisibility;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.infrastructure.ActiveData;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.ontology.*;
import org.zfin.ontology.service.OntologyService;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.*;

/**
 * Generic entry point for viewing a term detail page.
 */
@Controller
public class OntologyTermDetailController {


    @RequestMapping("/term-detail/term")
    protected String termDetailPageByName(@RequestParam(required = true) String name,
                                          @RequestParam(required = false) String ontologyName,
                                          @ModelAttribute("formBean") OntologyBean form,
                                          Model model) throws Exception {
        if (name == null) {
            model.addAttribute(LookupStrings.ZDB_ID, "No term name provided");
            return LookupStrings.RECORD_NOT_FOUND_PAGE;

        }
        // set default ontology
        Ontology ontology = Ontology.ANATOMY;
        // use ontology if provided
        if (StringUtils.isNotEmpty(ontologyName)) {
            ontology = Ontology.getOntology(ontologyName);
        }

        GenericTerm term = null;
        // has a wild card
        // check if the term name contains an asterisk at the end of the string, indicating that
        // we are looking for a list of terms matching the name
        if (name.endsWith("*")) {
            String queryString = name.substring(0, name.indexOf("*"));
            MatchingTermService matcher = new MatchingTermService(-1);
            List<TermDTO> terms = matcher.getMatchingTermList(queryString, ontology);
            Collections.sort(terms);
            model.addAttribute("terms", terms);
            model.addAttribute("query", queryString);
            model.addAttribute("ontology", ontology);
            AnatomySearchBean anatomySearchBean = new AnatomySearchBean();
            anatomySearchBean.setQueryString(name);
            model.addAttribute("formBean", anatomySearchBean);
            return "anatomy/show-all-terms.page";
        } else {
            term = RepositoryFactory.getOntologyRepository().getTermByName(name, ontology);
            if (term != null)
                return "redirect:/action/ontology/term-detail/" + term.getOboID();
        }
        model.addAttribute(LookupStrings.ZDB_ID, name);
        return LookupStrings.RECORD_NOT_FOUND_PAGE;
    }

    @RequestMapping("/term-detail/{termID}")
    protected String termDetailPage(@PathVariable String termID,
                                    @ModelAttribute("formBean") OntologyBean form,
                                    Model model) throws Exception {

        if (termID == null) {
            model.addAttribute(LookupStrings.ZDB_ID, "No term ID provided");
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
        if (termID.contains("*"))
            return termDetailPageByName(termID, null, form, model);

        // if ZDB-ANAT id obtain ZDB-TERM ID and re-direct
        if (ActiveData.isValidActiveData(termID, ActiveData.Type.ANAT)) {
            String newTermID = RepositoryFactory.getInfrastructureRepository().getReplacedZdbID(termID);
            if (newTermID == null) {
                model.addAttribute(LookupStrings.ZDB_ID, "No replacement term ID found for " + termID);
                return LookupStrings.RECORD_NOT_FOUND_PAGE;
            }
            return "redirect:/action/ontology/term-detail/" + newTermID;
        }

        GenericTerm term = null;
        // check if TERM id
        if (ActiveData.isValidActiveData(termID, ActiveData.Type.TERM)) {
            term = RepositoryFactory.getInfrastructureRepository().getTermByID(termID);
        } else {
            // check if it is an OBO ID
            if (Ontology.isOboID(termID))
                term = RepositoryFactory.getOntologyRepository().getTermByOboID(termID);
        }

        List<RelationshipPresentation> termRelationships = OntologyService.getRelatedTermsWithoutStages(term);
        Collections.sort(termRelationships);

        SectionVisibility sectionVisibility = form.getSectionVisibility();
        if (sectionVisibility.isVisible(OntologyBean.Section.EXPRESSION)) {
            sectionVisibility.setSectionData(OntologyBean.Section.EXPRESSION, true);
        } else {
            // check if there are any data in this section.
            if (term.getOntology().isExpressionData()) {
                sectionVisibility.setSectionData(OntologyBean.Section.EXPRESSION, hasExpressionData(term));
            }
        }
        if (sectionVisibility.isVisible(OntologyBean.Section.PHENOTYPE)) {
            sectionVisibility.setSectionData(OntologyBean.Section.PHENOTYPE, true);
        } else {
            if (term.getOntology().isPhenotypeData()) {
                sectionVisibility.setSectionData(OntologyBean.Section.PHENOTYPE, hasPhenotypeData(term));
            }
        }

        form.setTermRelationships(termRelationships);
        form.setTerm(term);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, term.getOntology().getCommonName() + ": " + term.getTermName());

        return "ontology/ontology-term.page";

    }

    @RequestMapping(value = {("/term-detail-popup-button")})
    public String getTermDetailPopupButton(@RequestParam String termID, Model model) {
        model.addAttribute("hasAddToSearchButton", true);
        return getTermDetailPopup(termID, model);

    }

    @RequestMapping(value = {("/term-detail-popup")})
    public String getTermDetailPopup(@RequestParam String termID, Model model) {
        if (termID.contains("ZDB-TERM")) {
            GenericTerm term = RepositoryFactory.getOntologyRepository().getTermByZdbID(termID);
            termID = term.getOboID();
        }

        GenericTerm term = RepositoryFactory.getOntologyRepository().getTermByOboID(termID);

        if (term == null) {
            model.addAttribute(LookupStrings.ZDB_ID, termID);
            return "record-not-found.popup";
        }

        List<RelationshipPresentation> termRelationships = OntologyService.getRelatedTerms(term);
        Collections.sort(termRelationships);

        /*form.setTermRelationships(termRelationships);
        form.setTerm(term);
        model.addAttribute("formBean", form);*/
        //model.addAttribute(LookupStrings.DYNAMIC_TITLE, term.getTermName());


        model.addAttribute("term", term);
        model.addAttribute("termRelationships", termRelationships);
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
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
        if (entity.getSubterm() == null) {
            model.addAttribute(LookupStrings.ZDB_ID, subTermID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
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

    private boolean hasExpressionData(Term anatomyTerm) {
        AnatomyStatistics statistics = getAnatomyRepository().getAnatomyStatistics(anatomyTerm.getZdbID());
        if (statistics == null || statistics.getNumberOfObjects() > 0 || statistics.getNumberOfTotalDistinctObjects() > 0)
            return true;
        // check for antibody records including substructures
        PaginationBean pagination = new PaginationBean();
        pagination.setMaxDisplayRecords(1);
        int numOfAntibodies = getAntibodyRepository().getAntibodyCount(anatomyTerm, true);
        if (numOfAntibodies > 0)
            return true;

        // check for in situ-probes
        PaginationResult<HighQualityProbe> hqp = getPublicationRepository().getHighQualityProbeNames(anatomyTerm, 1);
        if (hqp != null && hqp.getTotalCount() > 0)
            return true;

        return false;
    }

    private boolean hasPhenotypeData(Term anatomyTerm) {
        GenericTerm term = getOntologyRepository().getTermByOboID(anatomyTerm.getOboID());
        AnatomyStatistics statistics = getAnatomyRepository().getAnatomyStatisticsForMutants(term.getZdbID());
        if (statistics != null && (statistics.getNumberOfObjects() > 0 || statistics.getNumberOfTotalDistinctObjects() > 0))
            return true;

        // check for MOs
        List<GenotypeExperiment> morphs =
                getMutantRepository().getGenotypeExperimentMorpholinos(term, null);
        return morphs != null && morphs.size() > 0;
    }


}
