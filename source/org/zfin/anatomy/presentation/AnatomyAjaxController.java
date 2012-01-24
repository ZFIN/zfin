package org.zfin.anatomy.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.anatomy.service.AnatomyService;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.database.DbSystemUtil;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.MarkerStatistic;
import org.zfin.marker.presentation.ExpressedGeneDisplay;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.presentation.AntibodyStatistics;
import org.zfin.mutant.presentation.GenotypeStatistics;
import org.zfin.mutant.presentation.MorpholinoStatistics;
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
public class AnatomyAjaxController {

    @Autowired
    private AnatomyService anatomyService;
    @Autowired
    private AnatomyRepository anatomyRepository;
    @Autowired
    private MutantRepository mutantRepository;
    @Autowired
    private OntologyRepository ontologyRepository;
    private PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();

    private static final Logger LOG = Logger.getLogger(AnatomyTermDetailController.class);

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

    @RequestMapping(value = "/show-phenotype-mutants/{zdbID}")
    public String showPhenotypeMutants(Model model
            , @PathVariable("zdbID") String termID
    ) throws Exception {

        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return "";

        AnatomySearchBean form = new AnatomySearchBean();
        form.setMaxDisplayRecords(AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        form.setAoTerm(term);
        retrieveMutantData(term, form);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        return "anatomy/show-phenotype-mutants.ajax";
    }

    @RequestMapping(value = "/show-all-phenotype-mutants/{zdbID}")
    public String showAllPhenotypeMutants(Model model
            , @ModelAttribute("formBean") AnatomySearchBean form
            , @PathVariable("zdbID") String termID
    ) throws Exception {

        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return "";

        form.setAoTerm(term);
        retrieveMutantData(term, form);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        return "anatomy/show-all-phenotype-mutants.page";
    }

    @RequestMapping(value = "/show-phenotype-wildtype-morpholinos/{zdbID}")
    public String showWildtypePhenotypeMorpholinos(Model model
            , @PathVariable("zdbID") String termID
    ) throws Exception {
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return "";

        AnatomySearchBean form = new AnatomySearchBean();
        form.setAoTerm(term);
        retrieveMorpholinoData(term, form, true);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        return "anatomy/show-phenotype-wildtype-morpholinos.ajax";
    }

    @RequestMapping(value = "/show-phenotype-non-wildtype-morpholinos/{zdbID}")
    public String showNonWildtypePhenotypeMorpholinos(Model model
            , @PathVariable("zdbID") String termID
    ) throws Exception {
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return "";

        AnatomySearchBean form = new AnatomySearchBean();
        form.setAoTerm(term);
        retrieveMorpholinoData(term, form, false);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        return "anatomy/show-phenotype-non-wildtype-morpholinos.ajax";
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
        List<ExpressedGeneDisplay> expressedGenes = new ArrayList<ExpressedGeneDisplay>();
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

    private @Autowired
    HttpServletRequest request;

    private void retrieveMutantData(GenericTerm ai, AnatomySearchBean form) {
        PaginationResult<Genotype> genotypeResult = mutantRepository.getGenotypesByAnatomyTerm(ai, false, form);
        form.setGenotypeCount(genotypeResult.getTotalCount());
        form.setTotalRecords(genotypeResult.getTotalCount());
        form.setQueryString(request.getQueryString());
        form.setRequestUrl(request.getRequestURL());

        List<Genotype> genotypes = genotypeResult.getPopulatedResults();
        form.setGenotypes(genotypes);
        List<GenotypeStatistics> genoStats = createGenotypeStats(genotypes, ai);
        form.setGenotypeStatistics(genoStats);

        AnatomyStatistics statistics = anatomyRepository.getAnatomyStatisticsForMutants(ai.getZdbID());
        form.setAnatomyStatisticsMutant(statistics);
    }

    private List<GenotypeStatistics> createGenotypeStats(List<Genotype> genotypes, GenericTerm ai) {
        if (genotypes == null || ai == null)
            return null;

        List<GenotypeStatistics> stats = new ArrayList<GenotypeStatistics>();
        for (Genotype genoType : genotypes) {
            GenotypeStatistics stat = new GenotypeStatistics(genoType, ai);
            stats.add(stat);
        }
        return stats;
    }

    /**
     * Note: method 1 - very slow to do one query and then split
     * because you need to rehydrate each instance
     * in order to compare. So instead did as two separate queries.
     *
     * @param ai       ao term
     * @param form     form bean
     * @param wildtype wild type or not
     */
    protected void retrieveMorpholinoData(GenericTerm ai, AnatomySearchBean form, boolean wildtype) {

        PaginationResult<GenotypeExperiment> wildtypeMorphResults =
                mutantRepository.getGenotypeExperimentMorpholinos(ai, wildtype, AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        int count = wildtypeMorphResults.getTotalCount();
        List<GenotypeExperiment> experiments = wildtypeMorphResults.getPopulatedResults();

        List<MorpholinoStatistics> morpholinoStats = createMorpholinoStats(experiments, ai);
        if (wildtype) {
            form.setWildtypeMorpholinoCount(count);
            form.setAllMorpholinos(morpholinoStats);
        } else {
            form.setMutantMorpholinoCount(count);
            form.setNonWildtypeMorpholinos(morpholinoStats);
        }
    }

    protected static List<MorpholinoStatistics> createMorpholinoStats(List<GenotypeExperiment> morpholinos, GenericTerm ai) {
        if (morpholinos == null || ai == null)
            return null;

        List<MorpholinoStatistics> stats = new ArrayList<MorpholinoStatistics>();
        for (GenotypeExperiment genoExp : morpholinos) {
            MorpholinoStatistics stat = new MorpholinoStatistics(genoExp, ai);
            stats.add(stat);
        }
        return stats;
    }


}
