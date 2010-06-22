package org.zfin.ontology.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.view.RedirectView;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.ontology.OntologyManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller that serves the overview page of all loaded ontologies.
 */
public class OntologyManagerController extends AbstractCommandController {

    private String viewName;

    public OntologyManagerController() {
        setCommandClass(OntologyBean.class);
    }

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        OntologyBean form = (OntologyBean) command;


        OntologyBean.ActionType actionType = form.getActionType();
        if (actionType != null) {
            switch (actionType) {
                case SERIALIZE_ONTOLOGIES:
                    OntologyManager.getInstance().serializeOntologies();
                case LOAD_FROM_DATABASE:
                    OntologyManager.getInstance(OntologyManager.LoadingMode.DATABASE).serializeOntologies();
                case LOAD_FROM_SERIALIZED_FILE:
                    OntologyManager.getInstance(OntologyManager.LoadingMode.SERIALIZED_FILE);
            }
        }
        if (OntologyManager.hasStartedLoadingOntologies()) {
            OntologyManager manager = OntologyManager.getInstance();
            form.setOntologyManager(manager);
        } else {
            form.setOntologiesLoaded(false);
        }
        // If this was an action request redirect to the normal page to
        // allow refresh of the page without submitting the action again.
        if (actionType != null)
            return new ModelAndView(new RedirectView("ontology-caching"));
        return new ModelAndView(viewName, LookupStrings.FORM_BEAN, form);
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
}
