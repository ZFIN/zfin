package org.zfin.ontology.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.PatriciaTrieMultiMap;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.Term;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Controller that serves the overview page of all loaded ontologies.
 */
public class OntologyTermController extends AbstractCommandController {

    private String viewName;

    public OntologyTermController() {
        setCommandClass(OntologyBean.class);
    }

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        OntologyBean form = (OntologyBean) command;

        OntologyBean.ActionType actionType = form.getActionType();
        if (actionType.equals(OntologyBean.ActionType.SHOW_TERM)) {
            return getModelAndViewForSingleTerm(form);
        }
        return getModelAndViewForListOfTerms(form);
    }

    private ModelAndView getModelAndViewForListOfTerms(OntologyBean form) {
        OntologyBean.ActionType actionType = form.getActionType();
        Ontology ontology = Ontology.getOntology(form.getOntologyName());
        if (actionType != null) {
//            List<Term> values = new ArrayList<Term>() ;
//            values.addAll(OntologyManager.getInstance().getTermOntologyMap(ontology).values()) ;
//            Collections.sort(values);
//            form.setTerms(values);
//            break ;
            switch (actionType) {
                case SHOW_KEYS:
                    // remap so that the list will be sorted
                    Set<Map.Entry<String,Set<Term>>> entries = OntologyManager.getInstance()
                            .getTermOntologyMap(ontology).entrySet() ;
                    TreeMap<String,Set<Term>> keys = new TreeMap<String,Set<Term>>() ;
                    for(Map.Entry<String,Set<Term>> entry: entries){
                        keys.put(entry.getKey(),entry.getValue()) ;
                    }
                    form.setKeys(keys);
                    break;
                case SHOW_VALUES:
                    form.setValueMap(createValueMap(OntologyManager.getInstance().getTermOntologyMap(ontology))) ;
                    break;
                case SHOW_ALIASES:
                case SHOW_OBSOLETE_TERMS:
                case SHOW_ALL_TERMS:
                default:
                    form.setTerms(OntologyManager.getInstance().getTermOntologyMap(ontology).getAllValues()) ;
                    break ;
            }
        }
        form.setOntology(ontology);
        form.setOntologyManager(OntologyManager.getInstance());
        return new ModelAndView(viewName, LookupStrings.FORM_BEAN, form);
    }

    private Map<Term,List<String>> createValueMap(PatriciaTrieMultiMap<Term> termOntologyMap) {
        Map<Term,List<String>> valueMap = new TreeMap<Term,List<String>>() ;
        // add them all by their termName first.
        for(Term t: termOntologyMap.getAllValues() ){
            List<String> termNames = new ArrayList<String>() ;
            termNames.add(t.getTermName()) ;
            valueMap.put(t,termNames) ;
        }

        // assume that each term has now been added
        for(String s: termOntologyMap.keySet() ){
            try {
                for(Term term: termOntologyMap.get(s)){
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
        Term term = RepositoryFactory.getInfrastructureRepository().getTermByID(termID);
        if (term == null)
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, termID);
        form.setTerm(term);
        return new ModelAndView(viewName, LookupStrings.FORM_BEAN, form);
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
}