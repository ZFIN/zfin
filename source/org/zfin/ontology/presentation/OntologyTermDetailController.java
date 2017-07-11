package org.zfin.ontology.presentation;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.anatomy.presentation.AnatomySearchBean;
import org.zfin.framework.presentation.*;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.ActiveData;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.*;
import org.zfin.ontology.*;
import org.zfin.ontology.service.OntologyService;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationListAdapter;
import org.zfin.publication.presentation.PublicationListBean;
import org.zfin.repository.RepositoryFactory;

import java.net.URL;
import java.util.*;

import static org.zfin.repository.RepositoryFactory.*;

/**
 * Generic entry point for viewing a term detail page.
 */
@Controller
@RequestMapping("/ontology")
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

        GenericTerm term;
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
            Map<String, String> labelMap = getLabelMap(terms);
            model.addAttribute("labelMap", labelMap);
            Map<String, List<TermDTO>> termGroups = getAllListMap(terms, ontology);
            model.addAttribute("termGroups", termGroups);
            AnatomySearchBean anatomySearchBean = new AnatomySearchBean();
            anatomySearchBean.setQueryString(name);
            anatomySearchBean.setOntologyName(ontologyName);
            model.addAttribute("formBean", anatomySearchBean);
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, " Ontology Search");
            return "ontology/show-all-terms.page";
        } else {
            term = RepositoryFactory.getOntologyRepository().getTermByName(name, ontology);
            if (term != null)
                return "redirect:/action/ontology/term-detail/" + term.getOboID();
        }
        model.addAttribute(LookupStrings.ZDB_ID, name);
        return LookupStrings.RECORD_NOT_FOUND_PAGE;
    }

    private Map<String, List<TermDTO>> getAllListMap(List<TermDTO> terms, Ontology ontology) {
        Collection<Ontology> ontologyList = ontology.getIndividualOntologies();
        Map<String, List<TermDTO>> termGroups = new HashMap<>(ontologyList.size());
        // Add the complete list manually and then add the list of terms per ontology
        termGroups.put("All", terms);
        for (Ontology ont : ontologyList) {
            List<TermDTO> subList = new ArrayList<>(terms.size());
            for (TermDTO term : terms) {
                if (term.getOntology().equals(DTOConversionService.convertToOntologyDTO(ont)))
                    subList.add(term);
            }
            termGroups.put(DTOConversionService.convertToOntologyDTO(ont).getDisplayName(), subList);
        }
        return termGroups;
    }

    private Map<String, String> getLabelMap(List<TermDTO> terms) {
        Map<OntologyDTO, Integer> hist = OntologyService.getHistogramOfTerms(terms);
        Map<String, String> histogram = new HashMap<>(hist.size());
        for (OntologyDTO ontologyDTO : hist.keySet()) {
            histogram.put(ontologyDTO.getOntologyName(), ontologyDTO.getDisplayName());
        }
        return histogram;
    }

    @RequestMapping("/term-detail/{termID}")
    protected String termDetailPage(@PathVariable String termID,
                                    @ModelAttribute("formBean") OntologyBean form,
                                    Model model) throws Exception {

        if (termID == null) {
            return getErrorPage(model);
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
        if (term == null) {
            model.addAttribute(LookupStrings.ZDB_ID, termID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        List<RelationshipPresentation> termRelationships = OntologyService.getRelatedTermsWithoutStages(term);

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
        model.addAttribute("jspFunctions", new ZfinJSPFunctions());
        int number = getInfrastructureRepository().getTermReferences(term, null).size();

        model.addAttribute("numberOfCitations", number);
        boolean isDiseaseTerm = term.getOntology().equals(Ontology.DISEASE_ONTOLOGY);
        if (isDiseaseTerm) {
            int numberOfGenes = OntologyService.getNumberOfDiseaseGenes(term);
            model.addAttribute("diseaseGenes", numberOfGenes);
            form.setOmimPhenos(OntologyService.getOmimPhenotypeForTerm(term));
            model.addAttribute("fishModels", OntologyService.getDiseaseModelsWithFishModel(term));
        }
        model.addAttribute("isDiseaseTerm", isDiseaseTerm);
        model.addAttribute("showPhenotypeSection", !term.getOntology().equals(Ontology.ECO));
        return "ontology/ontology-term.page";

    }

    private String getErrorPage(Model model) {
        return getErrorPage(null, model);
    }

    private String getErrorPage(String id, Model model) {
        if (id == null)
            model.addAttribute(LookupStrings.ZDB_ID, "No term ID provided");
        else
            model.addAttribute(LookupStrings.ZDB_ID, id);
        return LookupStrings.RECORD_NOT_FOUND_PAGE;
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

        List<RelationshipPresentation> termRelationships = OntologyService.getRelatedTermsWithoutStages(term);

        model.addAttribute("term", term);
        model.addAttribute("termRelationships", termRelationships);
        return "ontology/ontology-term-popup.popup";
    }

    @RequestMapping(value = {("/term-citations/{oboID}")})
    public String getCitationList(@PathVariable String oboID,
                                  Model model) {

        Term term = getOntologyRepository().getTermByOboID(oboID);
        if (term == null) {
            return getErrorPage(oboID, model);
        }
        model.addAttribute("term", term);
        model.addAttribute("jspFunctions", new ZfinJSPFunctions());
        return "ontology/term-citations.page";
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
        return getMutantRepository().hasPhenotype(term);
    }

    @RequestMapping("/disease-publication-list/{termID}")
    public String diseaseCitationList(@PathVariable String termID,
                                      @RequestParam(required = false) String orderBy,
                                      Model model) throws Exception {

        if (termID == null) {
            return getErrorPage(model);
        }
        GenericTerm term = getOntologyRepository().getTermByOboID(termID);
        if (term == null) {
            return getErrorPage(termID, model);
        }
        model.addAttribute("term", term);
        model.addAttribute("orderBy", orderBy);
        PublicationListBean citationBean = new PublicationListAdapter(getInfrastructureRepository().getTermReferences(term, orderBy));
        citationBean.setOrderBy(orderBy);
        model.addAttribute("citationList", citationBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Publication List");
        return "ontology/disease-publication-list.page";
    }

    @RequestMapping("/fish-model-publication-list/{termID}/{id}")
    public String fishModelPublicationList(@PathVariable String termID,
                                           @PathVariable String id,
                                           @RequestParam(required = false) String orderBy,
                                           Model model) throws Exception {

        if (id == null) {
            return getErrorPage(model);
        }

        GenericTerm disease = getOntologyRepository().getTermByOboID(termID);
        if (disease == null) {
            return getErrorPage(termID, model);
        }

        Fish fish;
        List<Publication> citationList;
        // The id parameter is intended to either be a Fish ID or FishExperiment ID
        ActiveData.Type type = ActiveData.getType(id);
        switch (type) {
            case FISH:
                fish = getMutantRepository().getFish(id);
                citationList = PhenotypeService.getPublicationList(disease, fish, orderBy);
                break;

            case GENOX:
                FishExperiment fishExperiment = getExpressionRepository().getFishExperimentByID(id);
                fish = fishExperiment.getFish();
                citationList = PhenotypeService.getPublicationList(disease, fishExperiment, orderBy);
                break;

            default:
                return getErrorPage(id, model);
        }

        model.addAttribute("fish", fish);
        model.addAttribute("term", disease);
        PublicationListBean citationBean = new PublicationListAdapter(citationList);
        citationBean.setOrderBy(orderBy);
        model.addAttribute("citationList", citationBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Publication List");
        return "ontology/fish-model-publication-list.page";
    }

    @RequestMapping("/term-detail/{termID}/phenogrid")
    public String phenogrid(@PathVariable String termID, Model model) {

        if (termID == null) {
            return getErrorPage(model);
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
        if (term == null) {
            model.addAttribute(LookupStrings.ZDB_ID, termID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("term", term);

        return "ontology/phenogrid.page";
    }

    @RequestMapping("/note/ontology-relationship")
    public String getOntologyRelationshipNote() {
        return "ontology/ontology-relationship-note.insert";
    }
    
}
