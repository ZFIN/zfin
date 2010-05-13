package org.zfin.ontology.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.Term;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
            switch (actionType) {
                case SHOW_ALIASES:
                    form.setAliasTermMap(OntologyManager.getInstance().getAliasOntologyMap(ontology));
                    break;
                case SHOW_OBSOLETE_TERMS:
                    form.setTermMap(OntologyManager.getInstance().getObsoleteTermMap(ontology));
                    break;
                default:
                    form.setTermMap(OntologyManager.getInstance().getTermOntologyMap(ontology));
            }
        }
        return new ModelAndView(viewName, LookupStrings.FORM_BEAN, form);
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