package org.zfin.anatomy.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.anatomy.AnatomySynonym;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.infrastructure.ActiveData;
import org.zfin.ontology.OntologyManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Action class that serves the anatomy search page.
 * 1) Search for a single string
 * 2) Search by developmental stage
 * 3) List all anatomy items.
 */
public class AnatomySearchController extends AbstractCommandController {

    private static final Logger LOG = Logger.getLogger(AnatomySearchController.class);
    // These two variables are injected by Spring
    private AnatomyRepository anatomyRepository;
    private String redirectUrlIfSingleResult;

    public AnatomySearchController() {
        setCommandClass(AnatomySearchBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {

        AnatomySearchBean anatomyForm = (AnatomySearchBean) command;
        AnatomySearchController.LOG.debug("Start Action Class");
        if (anatomyForm.isCompleteSearch()) {
            prepareCompleteSearch(anatomyForm);
        } else if (anatomyForm.isStageSearch()) {
            doTermSearchByStage(anatomyForm);
        }
        // search term is the default mode.
        else if (anatomyForm.isTermSearch()) {
            runTermSearch(anatomyForm);
            // If only one term was found or an exact match redirect directly to the details page
            if (anatomyForm.getStatisticItems().size() == 1) {
                AnatomyStatistics stats = anatomyForm.getStatisticItems().get(0);
                return new ModelAndView(redirectUrlIfSingleResult + "?anatomyItem.zdbID=" + stats.getAnatomyItem().getZdbID());
            }
        }
        populateDevelopmentStages(anatomyForm);

        LOG.debug(anatomyForm);
        return new ModelAndView("anatomy-search.page", LookupStrings.FORM_BEAN, anatomyForm);
    }

    private void populateDevelopmentStages(AnatomySearchBean anatomyForm) {
        // Always populate the developmental stage fields.
        List stages = anatomyRepository.getAllStagesWithoutUnknown();

        anatomyForm.setStages(stages);
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

    public void setAnatomyRepository(AnatomyRepository anatomyRepository) {
        this.anatomyRepository = anatomyRepository;
    }

    public void setRedirectUrlIfSingleResult(String redirectUrlIfSingleResult) {
        this.redirectUrlIfSingleResult = redirectUrlIfSingleResult;
    }
}
