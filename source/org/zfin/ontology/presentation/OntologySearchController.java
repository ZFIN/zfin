package org.zfin.ontology.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.presentation.AnatomySearchBean;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.ontology.Ontology;

import java.util.List;

/**
 * Action class that serves the anatomy search page.
 * 1) Search for a single string
 * 2) Search by developmental stage
 * 3) List all anatomy items.
 */
@Controller
@RequestMapping("/ontology")
public class OntologySearchController {

    private static final Logger LOG = LogManager.getLogger(OntologySearchController.class);

    @Autowired
    private AnatomyRepository anatomyRepository;

    @RequestMapping("/search")
    protected String showSearchForm(Model model,
                                    AnatomySearchBean form) throws Exception {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "AO / GO Search");
        form.setOntologyName(Ontology.AOGODO.getOntologyName());
        model.addAttribute("formBean", form);
        return "ontology/search-form";
    }

    @RequestMapping(value = "/show-anatomy-terms-by-stage", method = RequestMethod.GET)
    public String showAnatomyTermsByStage(@ModelAttribute("formBean") AnatomySearchBean anatomyForm) throws Exception {
        LOG.debug("Start Action Class");
        doTermSearchByStage(anatomyForm);
        return "ontology/show-anatomy-terms-by-stage";
    }

    private void doTermSearchByStage(AnatomySearchBean anatomyForm) {

        // ensure that the search term is unset.
        anatomyForm.setSearchTerm("");

        // Check if a valid stage is selected.
        DevelopmentStage stage = anatomyForm.getStage();
        if (StringUtils.isEmpty(stage.getZdbID()))
            return;

        stage = anatomyRepository.getStage(stage);
        anatomyForm.setStage(stage);
        List<AnatomyStatistics> anatomyStatistics = anatomyRepository.getAnatomyItemStatisticsByStage(stage);
        anatomyForm.setStatisticItems(anatomyStatistics);

    }

}
