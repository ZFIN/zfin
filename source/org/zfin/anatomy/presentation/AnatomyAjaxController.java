package org.zfin.anatomy.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.anatomy.service.AnatomyService;
import org.zfin.database.DbSystemUtil;
import org.zfin.expression.FigureService;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.MarkerStatistic;
import org.zfin.marker.presentation.ExpressedGeneDisplay;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.presentation.AntibodyStatistics;
import org.zfin.mutant.presentation.FishStatistics;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to serve ajax calls for expression and phenotype data
 * for a given anatomy term.
 */
@Controller
@RequestMapping("/ontology")
public class AnatomyAjaxController {

    @Autowired
    private AnatomyRepository anatomyRepository;
    @Autowired
    private MutantRepository mutantRepository;
    @Autowired
    private OntologyRepository ontologyRepository;
    private PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();

    private static final Logger LOG = Logger.getLogger(AnatomyAjaxController.class);

    @ModelAttribute("formBean")
    public AnatomySearchBean getDefaultFormBean() {
        return new AnatomySearchBean();
    }


    @RequestMapping(value = "/show-expressed-genes/{zdbID}")
    public String showExpressedGenes(Model model
            , @PathVariable("zdbID") String termID
    ) throws Exception {
        LOG.info("Start Anatomy Term Detail Controller");

        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return "";

        AnatomySearchBean form = new AnatomySearchBean();
        form.setAoTerm(term);
        retrieveExpressedGenesData(term, form);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        return "anatomy/show-expressed-genes.ajax";
    }

    @RequestMapping(value = "/show-expressed-insitu-probes/{zdbID}")
    public String showExpressedInSituProbes(Model model
            , @PathVariable("zdbID") String termID
    ) throws Exception {

        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return "";

        AnatomySearchBean form = new AnatomySearchBean();
        form.setAoTerm(term);
        retrieveHighQualityProbeData(term, form);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        return "anatomy/show-expressed-insitu-probes.ajax";
    }

    @RequestMapping(value = "/show-labeled-antibodies/{zdbID}")
    public String showExpressedAntibodies(Model model
            , @PathVariable("zdbID") String termID
    ) throws Exception {

        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return "";

        AnatomySearchBean form = new AnatomySearchBean();
        form.setAoTerm(term);
        retrieveAntibodyData(term, form);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        return "anatomy/show-labeled-antibodies.ajax";
    }

    @RequestMapping(value = "/show-clean-fish/{zdbID}")
    public String showPhenotypeCleanFish(Model model,
                                         @PathVariable("zdbID") String termID
    ) throws Exception {

        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return "";

        AnatomySearchBean form = new AnatomySearchBean();
        form.setMaxDisplayRecords(AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        form.setAoTerm(term);
        retrieveMutantData(term, form, false);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        return "anatomy/show-clean-fish.ajax";
    }

    @RequestMapping(value = "/show-all-clean-fish/{zdbID}")
    public String showAllPhenotypeCleanFish(Model model,
                                            @ModelAttribute("formBean") AnatomySearchBean form,
                                            @PathVariable("zdbID") String termID) throws Exception {

        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return "";

        form.setAoTerm(term);
        retrieveMutantData(term, form, false);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        return "anatomy/show-all-clean-fish.page";
    }

    @RequestMapping(value = "/show-all-clean-fish-include-substructures/{zdbID}")
    public String showAllPhenotypeCleanFishIncludingSubs(Model model
            , @ModelAttribute("formBean") AnatomySearchBean form
            , @PathVariable("zdbID") String termID
    ) throws Exception {

        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return "";

        form.setAoTerm(term);
        retrieveMutantData(term, form, true);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        return "anatomy/show-all-clean-fish.page";
    }

    @RequestMapping(value = "/show-all-in-situ-probes/{zdbID}")
    public String showAllInSituProbes(Model model
            , @ModelAttribute("formBean") AnatomySearchBean form
            , @PathVariable("zdbID") String termID
    ) throws Exception {

        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return "";

        form.setAoTerm(term);
        retrieveMutantData(term, form, false);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        return "anatomy/show-all-in-situ-probes.page";
    }

    @RequestMapping(value = "/show-all-phenotype-mutants-substructures/{zdbID}")
    public String showAllPhenotypeMutantsIncludingSubstructures(Model model
            , @ModelAttribute("formBean") AnatomySearchBean form
            , @PathVariable("zdbID") String termID
    ) throws Exception {

        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return "";

        form.setAoTerm(term);
        retrieveMutantData(term, form, true);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        model.addAttribute("includingSubstructures", true);
        return "anatomy/show-all-phenotype-mutants.page";
    }

    @RequestMapping(value = "/{oboID}/phenotype-summary/{fishID}")
    public String genotypeSummary(Model model
            , @PathVariable("oboID") String oboID
            , @PathVariable("fishID") String fishID
    ) throws Exception {
        GenericTerm term = ontologyRepository.getTermByOboID(oboID);
        if (term == null) {
            model.addAttribute(LookupStrings.ZDB_ID, oboID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        Fish fish = RepositoryFactory.getMutantRepository().getFish(fishID);
        if (fish == null) {
            model.addAttribute(LookupStrings.ZDB_ID, fishID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }


        AnatomySearchBean form = new AnatomySearchBean();

        form.setAoTerm(term);

        List<FigureSummaryDisplay> figureSummaryDisplayList = FigureService.createPhenotypeFigureSummary(term, fish, true);
        model.addAttribute("figureSummaryDisplayList", figureSummaryDisplayList);

        retrieveMutantData(term, form, true);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        model.addAttribute("includingSubstructures", true);
        model.addAttribute("fish", fish);
        model.addAttribute("entity", term);
        return "anatomy/phenotype-summary.page";
    }

    private void retrieveAntibodyData(GenericTerm aoTerm, AnatomySearchBean form) {

        PaginationBean pagination = new PaginationBean();
        pagination.setMaxDisplayRecords(AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        PaginationResult<AntibodyStatistics> antibodies = AnatomyService.getAntibodyStatistics(aoTerm, pagination, false);
        form.setAntibodyStatistics(antibodies.getPopulatedResults());
        form.setAntibodyCount(antibodies.getTotalCount());
        DbSystemUtil.logLockInfo();

        HibernateUtil.currentSession().flush();

        PaginationResult<AntibodyStatistics> antibodiesIncludingSubstructures = AnatomyService.getAntibodyStatistics(aoTerm, pagination, true);
        DbSystemUtil.logLockInfo();
        AnatomyStatistics statistics = new AnatomyStatistics();
        statistics.setNumberOfTotalDistinctObjects(antibodiesIncludingSubstructures.getTotalCount());
        statistics.setNumberOfObjects(antibodies.getTotalCount());
        form.setAnatomyStatisticsAntibodies(statistics);
    }

    private void retrieveHighQualityProbeData(GenericTerm anatomyTerm, AnatomySearchBean form) {
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        PaginationBean pagination = new PaginationBean();
        pagination.setMaxDisplayRecords(AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        PaginationResult<HighQualityProbe> hqp = markerRepository.getHighQualityProbeStatistics(anatomyTerm, pagination, false);
        form.setHighQualityProbeGenes(hqp.getPopulatedResults());
        form.setNumberOfHighQualityProbes(hqp.getTotalCount());

        PaginationResult<HighQualityProbe> hqpIncludingSubstructures = markerRepository.getHighQualityProbeStatistics(anatomyTerm, pagination, true);
        AnatomyStatistics statistics = new AnatomyStatistics();
        statistics.setNumberOfTotalDistinctObjects(hqpIncludingSubstructures.getTotalCount());
        statistics.setNumberOfObjects(hqp.getTotalCount());
        form.setAnatomyStatisticsProbe(statistics);
    }

    private void retrieveExpressedGenesData(GenericTerm anatomyTerm, AnatomySearchBean form) {

        PaginationResult<MarkerStatistic> expressionMarkersResult =
                publicationRepository.getAllExpressedMarkers(anatomyTerm, 0, AnatomySearchBean.MAX_NUMBER_EPRESSED_GENES);

        List<MarkerStatistic> markers = expressionMarkersResult.getPopulatedResults();
        form.setExpressedGeneCount(expressionMarkersResult.getTotalCount());
        List<ExpressedGeneDisplay> expressedGenes = new ArrayList<>();
        if (markers != null) {
            for (MarkerStatistic marker : markers) {
                ExpressedGeneDisplay expressedGene = new ExpressedGeneDisplay(marker);
                expressedGenes.add(expressedGene);
            }
        }


        form.setAllExpressedMarkers(expressedGenes);

        // todo: could we get this as part of our statistic?
        form.setTotalNumberOfFiguresPerAnatomyItem(publicationRepository.getTotalNumberOfFiguresPerAnatomyItem(anatomyTerm));
        // maybe used later?
        form.setTotalNumberOfExpressedGenes(expressionMarkersResult.getTotalCount());

        AnatomyStatistics statistics = anatomyRepository.getAnatomyStatistics(anatomyTerm.getZdbID());
        form.setAnatomyStatistics(statistics);
    }

    private
    @Autowired
    HttpServletRequest request;

    private void retrieveMutantData(GenericTerm ai, AnatomySearchBean form, boolean includeSubstructures) {
        PaginationResult<Fish> genotypeResult;
        if (includeSubstructures)
            genotypeResult = mutantRepository.getFishByAnatomyTermIncludingSubstructures(ai, false, form);
        else
            genotypeResult = mutantRepository.getFishByAnatomyTerm(ai, false, form);
        populateFormBeanForMutantList(ai, form, genotypeResult, includeSubstructures);
    }

    private void populateFormBeanForMutantList(GenericTerm ai, AnatomySearchBean form, PaginationResult<Fish> fishResult, boolean includeSubstructures) {
        form.setFishCount(fishResult.getTotalCount());
        form.setTotalRecords(fishResult.getTotalCount());
        form.setQueryString(request.getQueryString());
        form.setRequestUrl(request.getRequestURL());

        List<Fish> fishList = fishResult.getPopulatedResults();
        form.setFish(fishList);
        List<FishStatistics> genoStats = createGenotypeStats(fishList, ai, includeSubstructures);
        form.setGenotypeStatistics(genoStats);

        AnatomyStatistics statistics = anatomyRepository.getAnatomyStatisticsForMutants(ai.getZdbID());
        form.setAnatomyStatisticsMutant(statistics);
    }

    private List<FishStatistics> createGenotypeStats(List<Fish> fishList, GenericTerm ai, boolean includeSubstructures) {
        if (fishList == null || ai == null)
            return null;

        List<FishStatistics> stats = new ArrayList<>();
        for (Fish fish : fishList) {
            FishStatistics stat = new FishStatistics(fish, ai, includeSubstructures);
            stats.add(stat);
        }
        return stats;
    }

}
