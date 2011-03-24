package org.zfin.ontology.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.infrastructure.PatriciaTrieMultiMap;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

/**
 * Controller that serves the overview page of all loaded ontologies.
 */
@Controller
public class OntologyTermController {

    private String viewName;
    private OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();

    private static Logger logger = Logger.getLogger(OntologyTermController.class);

    @RequestMapping("/terms")
    private String getModelAndViewForListOfTerms(@RequestParam String action,
                                                 @RequestParam String ontologyName,
                                                 Model model) {
        OntologyBean form = new OntologyBean();
        model.addAttribute("formBean",form);
        form.setAction(action);
        OntologyBean.ActionType actionType = form.getActionType();
        Ontology ontology = Ontology.getOntology(ontologyName);
        if (actionType != null) {
//            List<Term> values = new ArrayList<Term>() ;
//            values.addAll(OntologyManager.getInstance().getTermOntologyMap(ontology).values()) ;
//            Collections.sort(values);
//            form.setTerms(values);
//            break ;
            switch (actionType) {
                case SHOW_KEYS:
                    // remap so that the list will be sorted
                    Set<Map.Entry<String,Set<TermDTO>>> entries = OntologyManager.getInstance()
                            .getTermOntologyMapCopy(ontology).entrySet() ;
                    TreeMap<String,Set<TermDTO>> keys = new TreeMap<String,Set<TermDTO>>() ;
                    for(Map.Entry<String,Set<TermDTO>> entry: entries){
                        keys.put(entry.getKey(),entry.getValue()) ;
                    }
                    form.setKeys(keys);
                    break;
                case SHOW_VALUES:
                    form.setValueMap(createValueMap(OntologyManager.getInstance().getTermOntologyMapCopy(ontology))) ;
                    break;
                case SHOW_ALIASES:
                case SHOW_OBSOLETE_TERMS:
                case SHOW_ALL_TERMS:
                default:
                    form.setTerms(OntologyManager.getInstance().getTermOntologyMapCopy(ontology).getAllValues()) ;
                    break ;
            }
        }
        form.setOntology(ontology);
        return "ontology/ontology_terms.page";
    }

    private Map<TermDTO,List<String>> createValueMap(PatriciaTrieMultiMap<TermDTO> termOntologyMap) {
        Map<TermDTO,List<String>> valueMap = new TreeMap<TermDTO,List<String>>() ;
        // add them all by their termName first.
        for(TermDTO t: termOntologyMap.getAllValues() ){
            List<String> termNames = new ArrayList<String>() ;
            termNames.add(t.getName()) ;
            valueMap.put(t,termNames) ;
        }

        // assume that each term has now been added
        for(String s: termOntologyMap.keySet() ){
            try {
                for(TermDTO term: termOntologyMap.get(s)){
                    if(!valueMap.get(term).contains(s)){
                        valueMap.get(term).add(s) ;
                    }
                }
            } catch (Exception e) {
                logger.error("error adding key: "+ s + " ",e);
            }
        }


        return valueMap ;
    }

    /**
     * Display details for a single term.
     *
     * @param form form bean
     * @return model and view
     */
    private ModelAndView getModelAndViewForSingleTerm(OntologyBean form) {
        String termID = form.getTermID();
        if (termID == null)
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, "");
        GenericTerm term = RepositoryFactory.getOntologyRepository().getTermByZdbID(termID);
        if (term == null)
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, termID);
        form.setTerm(term);
        form.setAllChildren(ontologyRepository.getChildrenTransitiveClosures(term));
        return new ModelAndView(viewName, LookupStrings.FORM_BEAN, form);
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
}