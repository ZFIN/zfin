package org.zfin.anatomy.presentation;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.zfin.anatomy.AnatomyService;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.repository.AntibodyRepository;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to serve ajax calls for expression and phenotype data
 * for a given anatomy term.
 */
public class AnatomyAjaxController extends MultiActionController {

    private AnatomyRepository anatomyRepository = RepositoryFactory.getAnatomyRepository();
    private PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    private MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
    private OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();

    public ModelAndView showExpressionGenes(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

        String termID = request.getParameter(LookupStrings.ZDB_ID);
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return new ModelAndView(LookupStrings.RECORD_NOT_FOUND_PAGE, LookupStrings.ZDB_ID, termID);

        AnatomySearchBean form = new AnatomySearchBean();
        form.setAoTerm(term);
        retrieveExpressedGenesData(term, form);
        return new ModelAndView("anatomy-show-expression-genes.ajax", LookupStrings.FORM_BEAN, form);
    }

    public ModelAndView showExpressionInsituProbes(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

        String termID = request.getParameter(LookupStrings.ZDB_ID);
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return new ModelAndView(LookupStrings.RECORD_NOT_FOUND_PAGE, LookupStrings.ZDB_ID, termID);

        AnatomySearchBean form = new AnatomySearchBean();
        form.setAoTerm(term);
        retrieveHighQualityProbeData(term, form);
        return new ModelAndView("anatomy-show-expression-insitu-probes.ajax", LookupStrings.FORM_BEAN, form);
    }

    public ModelAndView showExpressionAntibodies(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

        String termID = request.getParameter(LookupStrings.ZDB_ID);
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return new ModelAndView(LookupStrings.RECORD_NOT_FOUND_PAGE, LookupStrings.ZDB_ID, termID);

        AnatomySearchBean form = new AnatomySearchBean();
        form.setAoTerm(term);
        retrieveAntibodyData(term, form);
        return new ModelAndView("anatomy-show-expression-antibodies.ajax", LookupStrings.FORM_BEAN, form);
    }

    public ModelAndView showPhenotypeMutants(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

        String termID = request.getParameter(LookupStrings.ZDB_ID);
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return new ModelAndView(LookupStrings.RECORD_NOT_FOUND_PAGE, LookupStrings.ZDB_ID, termID);

        AnatomySearchBean form = new AnatomySearchBean();
        form.setAoTerm(term);
        retrieveMutantData(term, form);
        return new ModelAndView("anatomy-show-phenotype-mutant.ajax", LookupStrings.FORM_BEAN, form);
    }

    public ModelAndView showPhenotypeMorpholinos(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

        String termID = request.getParameter(LookupStrings.ZDB_ID);
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return new ModelAndView(LookupStrings.RECORD_NOT_FOUND_PAGE, LookupStrings.ZDB_ID, termID);

        AnatomySearchBean form = new AnatomySearchBean();
        form.setAoTerm(term);
        retrieveMorpholinoData(term, form, true);
        return new ModelAndView("anatomy-show-phenotype-morpholinos.ajax", LookupStrings.FORM_BEAN, form);
    }

    public ModelAndView showPhenotypeNonwildtypeMorpholinos(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

        String termID = request.getParameter(LookupStrings.ZDB_ID);
        GenericTerm term = ontologyRepository.getTermByZdbID(termID);
        if (term == null)
            return new ModelAndView(LookupStrings.RECORD_NOT_FOUND_PAGE, LookupStrings.ZDB_ID, termID);

        AnatomySearchBean form = new AnatomySearchBean();
        form.setAoTerm(term);
        retrieveMorpholinoData(term, form, false);
        return new ModelAndView("anatomy-show-phenotype-non-wildtype-morpholinos.ajax", LookupStrings.FORM_BEAN, form);
    }

    private void retrieveAntibodyData(GenericTerm aoTerm, AnatomySearchBean form) {

        PaginationBean pagination = new PaginationBean();
        pagination.setMaxDisplayRecords(AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
        PaginationResult<AntibodyStatistics> antibodies = AnatomyService.getAntibodyStatistics(aoTerm, pagination, false);
        form.setAntibodyStatistics(antibodies.getPopulatedResults());
        form.setAntibodyCount(antibodies.getTotalCount());

        HibernateUtil.currentSession().flush();

        PaginationResult<AntibodyStatistics> antibodiesIncludingSubstructures = AnatomyService.getAntibodyStatistics(aoTerm, pagination, true);
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

    private void retrieveMutantData(GenericTerm ai, AnatomySearchBean form) {
        PaginationResult<Genotype> genotypeResult = mutantRepository.getGenotypesByAnatomyTerm(ai, false,
                AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        form.setGenotypeCount(genotypeResult.getTotalCount());

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
