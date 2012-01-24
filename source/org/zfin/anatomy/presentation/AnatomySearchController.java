package org.zfin.anatomy.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.anatomy.AnatomySynonym;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.infrastructure.ActiveData;
import org.zfin.ontology.OntologyManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Action class that serves the anatomy search page.
 * 1) Search for a single string
 * 2) Search by developmental stage
 * 3) List all anatomy items.
 */
@Controller
public class AnatomySearchController {

    private static final Logger LOG = Logger.getLogger(AnatomySearchController.class);

    @Autowired
    private AnatomyRepository anatomyRepository;

    @RequestMapping("/anatomy-search")
    protected String showSearchForm(Model model) throws Exception {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Anatomy Search");
        return "anatomy/search-form.page";
    }

    @RequestMapping(value = "/show-all-anatomy-terms", method = RequestMethod.GET)
    public String showAllAnatomyTerms(@ModelAttribute("formBean") AnatomySearchBean anatomyForm) throws Exception {
        LOG.debug("Start Action Class");
        List<AnatomyStatistics> anatomyStatistics = anatomyRepository.getAllAnatomyItemStatistics();
        Collections.sort(anatomyStatistics);
        anatomyForm.setStatisticItems(anatomyStatistics);
        return "anatomy/show-all-terms.page";
    }

    @RequestMapping(value = "/show-terms-by-stage", method = RequestMethod.GET)
    public String showAnatomyTermsByStage(@ModelAttribute("formBean") AnatomySearchBean anatomyForm) throws Exception {
        LOG.debug("Start Action Class");
        doTermSearchByStage(anatomyForm);
        return "anatomy/show-terms-by-stage.page";
    }

    @RequestMapping(value = "/anatomy-do-search", method = RequestMethod.GET)
    public String doSearch(@ModelAttribute("formBean") AnatomySearchBean anatomyForm
    ) throws Exception {
        LOG.debug("Start Action Class");
        // search term is the default mode.
        runTermSearch(anatomyForm);
        // If only one term was found or an exact match redirect directly to the details page
        if (anatomyForm.getStatisticItems().size() == 1) {
            AnatomyStatistics stats = anatomyForm.getStatisticItems().get(0);
            //return new ModelAndView(redirectUrlIfSingleResult + "?anatomyItem.zdbID=" + stats.getAnatomyItem().getZdbID());
            return "redirect:/action/anatomy/anatomy-view/" + stats.getAnatomyItem().getZdbID();
        }
        LOG.debug(anatomyForm);
        return "anatomy/search-form.page";
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

    private void runTermSearch(AnatomySearchBean anatomyForm) {
        String searchTerm = anatomyForm.getSearchTerm();
        AnatomyItem term;
        if (ActiveData.isValidActiveData(searchTerm, ActiveData.Type.TERM)) {
            TermDTO termDTO = OntologyManager.getInstance().getTermByID(searchTerm);
            term = anatomyRepository.getAnatomyTermByOboID(termDTO.getOboID());
        } else {
            term = anatomyRepository.getAnatomyItem(searchTerm);
        }
        // if the search contains a wild-card, then don't return a single item
        if (term != null && false == anatomyForm.isWildCard()) {
            AnatomyStatistics stat = new AnatomyStatistics();
            stat.setAnatomyItem(term);
            List<AnatomyStatistics> stats = new ArrayList<AnatomyStatistics>();
            stats.add(stat);
            anatomyForm.setStatisticItems(stats);
            return;
        }

        // check if there is an exact match for a synonym
        List<AnatomySynonym> synonyms = anatomyRepository.getAnatomyTermsBySynonymName(searchTerm);
        if (synonyms != null && synonyms.size() == 1) {
            term = synonyms.get(0).getItem();
            AnatomyStatistics stat = new AnatomyStatistics();
            stat.setAnatomyItem(term);
            List<AnatomyStatistics> stats = new ArrayList<AnatomyStatistics>();
            stats.add(stat);
            anatomyForm.setStatisticItems(stats);
        } else {
            List<AnatomyStatistics> anatomyStatistics = anatomyRepository.getAnatomyItemStatistics(searchTerm);
            anatomyForm.setStatisticItems(anatomyStatistics);
        }
    }

    private void prepareCompleteSearch(AnatomySearchBean anatomyForm) {
        List<AnatomyStatistics> anatomyStatistics = anatomyRepository.getAllAnatomyItemStatistics();
        Collections.sort(anatomyStatistics);
        anatomyForm.setStatisticItems(anatomyStatistics);
    }

}
