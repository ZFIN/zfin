package org.zfin.ontology.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.ontology.OntologyManager;

/**
 * Controller that serves the overview page of all loaded ontologies.
 */
@Controller
public class OntologyManagerController {

    @RequestMapping("/devtool/ontology/summary")
    protected String handle(@ModelAttribute("formBean") OntologyBean form,
                            Model model) throws Exception {

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
            // init ontology manager loading?  shouldn't this already be happening?
            OntologyManager.getInstance();
        } else {
            form.setOntologiesLoaded(false);
        }

        form.setOntologyManager(OntologyManager.getInstance());
        // If this was an action request redirect to the normal page to
        // allow handleCurationEvent of the page without submitting the action again.
        if (actionType != null)
            return "redirect:ontology-caching";
        return "dev-tools/ontology-manager.page";
    }
}
