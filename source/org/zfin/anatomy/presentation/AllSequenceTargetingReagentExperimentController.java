package org.zfin.anatomy.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.presentation.SequenceTargetingReagentStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;

/**
 * This Controller serves the all sequence targeting reagent experiment page.
 */
@Controller
@RequestMapping("/ontology")
public class AllSequenceTargetingReagentExperimentController {

    private static final Logger LOG = RootLogger.getLogger(AllSequenceTargetingReagentExperimentController.class);

    @ModelAttribute("formBean")
    public AnatomySearchBean getDefaultFormBean() {
        return new AnatomySearchBean();
    }


    private
    @Autowired
    HttpServletRequest request;

    @RequestMapping(value = "/show-all-sequence-targeting-reagents/{zdbID}/{wildtype}")
    public String showAllSequenceTargetingReagents(Model model
            , @PathVariable("zdbID") String termID
            , @PathVariable("wildtype") boolean isWildtype
            , @ModelAttribute("formBean") AnatomySearchBean form
    ) throws Exception {

        GenericTerm term = RepositoryFactory.getOntologyRepository().getTermByZdbID(termID);

        //try converting from anatomy to term if it gets an anatomy id...
        if (term == null && StringUtils.contains(termID, "ZDB-ANAT")) {
            LOG.error("Usage of ZDB-ANAT terms is obsoleted: " + termID);
            return LookupStrings.idNotFound(model, termID);
        }

        if (term == null) {
            model.addAttribute(LookupStrings.ZDB_ID, "No term name provided");
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }


        form.setAoTerm(term);
        form.setRequestUrl(request.getRequestURL());
        if (request.getQueryString() != null)
            form.setQueryString(request.getQueryString());

        retrieveSequenceTargetingReagentData(term, form, isWildtype);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        if (isWildtype)
            return "anatomy/show-all-wildtype-sequence-targeting-reagents.page";
        else
            return "anatomy/show-all-non-wildtype-sequence-targeting-reagents.page";
    }

    protected void retrieveSequenceTargetingReagentData(GenericTerm term, AnatomySearchBean form, boolean wildtype) {

        PaginationResult<GenotypeExperiment> wildtypeMorphResults =
                getMutantRepository().getGenotypeExperimentSequenceTargetingReagents(term, wildtype, form);
        int count = wildtypeMorphResults.getTotalCount();
        List<GenotypeExperiment> experiments = wildtypeMorphResults.getPopulatedResults();

        List<SequenceTargetingReagentStatistics> sequenceTargetingReagentsStats = createSequenceTargetingReagentStats(experiments, term);
        Collections.sort(sequenceTargetingReagentsStats, new Comparator<SequenceTargetingReagentStatistics>() {
            public int compare(SequenceTargetingReagentStatistics one, SequenceTargetingReagentStatistics two) {
                return (one.getTargetGeneOrder().compareTo(two.getTargetGeneOrder()));
            }
        });
        form.setTotalRecords(count);

        form.setWildtypeMorpholinoCount(count);
        form.setAllMorpholinos(sequenceTargetingReagentsStats);
    }

    protected List<SequenceTargetingReagentStatistics> createSequenceTargetingReagentStats(List<GenotypeExperiment> strExperiments, GenericTerm term) {
        if (strExperiments == null || term == null)
            return null;

        List<SequenceTargetingReagentStatistics> stats = new ArrayList<>();
        for (GenotypeExperiment genoExp : strExperiments) {
            SequenceTargetingReagentStatistics stat = new SequenceTargetingReagentStatistics(genoExp, term);
            stats.add(stat);
        }
        return stats;
    }

}
