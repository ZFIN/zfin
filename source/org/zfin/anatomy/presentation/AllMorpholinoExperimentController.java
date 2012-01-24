package org.zfin.anatomy.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.presentation.MorpholinoStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;

/**
 * This Controller serves the all morpholino experiment page.
 */
@Controller
public class AllMorpholinoExperimentController {

    @ModelAttribute("formBean")
    public AnatomySearchBean getDefaultFormBean() {
        return new AnatomySearchBean();
    }


    private
    @Autowired
    HttpServletRequest request;

    @RequestMapping(value = "/show-all-morpholinos/{zdbID}/{wildtype}")
    public String showAllMorpholinos(Model model
            , @PathVariable("zdbID") String termID
            , @PathVariable("wildtype") boolean isWildtype
            , @ModelAttribute("formBean") AnatomySearchBean form
    ) throws Exception {

        GenericTerm term = RepositoryFactory.getOntologyRepository().getTermByZdbID(termID);
        if (term == null)
            return "";
        form.setAoTerm(term);
        form.setRequestUrl(request.getRequestURL());
        if (request.getQueryString() != null)
            form.setQueryString(request.getQueryString());

        retrieveMorpholinoData(term, form, isWildtype);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        if (isWildtype)
            return "anatomy/show-all-wildtype-morpholinos.page";
        else
            return "anatomy/show-all-non-wildtype-morpholinos.page";
    }

    protected void retrieveMorpholinoData(GenericTerm term, AnatomySearchBean form, boolean wildtype) {

        PaginationResult<GenotypeExperiment> wildtypeMorphResults =
                getMutantRepository().getGenotypeExperimentMorpholinos(term, wildtype, form);
        int count = wildtypeMorphResults.getTotalCount();
        List<GenotypeExperiment> experiments = wildtypeMorphResults.getPopulatedResults();

        List<MorpholinoStatistics> morpholinoStats = createMorpholinoStats(experiments, term);
        Collections.sort(morpholinoStats, new Comparator<MorpholinoStatistics>() {
            public int compare(MorpholinoStatistics one, MorpholinoStatistics two) {
                return (one.getTargetGeneOrder().compareTo(two.getTargetGeneOrder()));
            }
        });
        form.setTotalRecords(count);

        form.setWildtypeMorpholinoCount(count);
        form.setAllMorpholinos(morpholinoStats);
    }

    protected List<MorpholinoStatistics> createMorpholinoStats(List<GenotypeExperiment> morpholinos, GenericTerm term) {
        if (morpholinos == null || term == null)
            return null;

        List<MorpholinoStatistics> stats = new ArrayList<MorpholinoStatistics>();
        for (GenotypeExperiment genoExp : morpholinos) {
            MorpholinoStatistics stat = new MorpholinoStatistics(genoExp, term);
            stats.add(stat);
        }
        return stats;
    }

}
