package org.zfin.anatomy.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyRelationship;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.expression.Figure;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.marker.MarkerStatistic;
import org.zfin.marker.presentation.ExpressedGeneDisplay;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.AntibodyStatistics;
import org.zfin.mutant.presentation.GenotypeStatistics;
import org.zfin.mutant.presentation.MorpholinoStatistics;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class that serves the anatomy term detail page.
 */
public class AnatomyTermDetailController extends AbstractCommandController {

    private static final Logger LOG = Logger.getLogger(AnatomyTermDetailController.class);

    private AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
    private AnatomyRepository anatomyRepository;
    private MutantRepository mutantRepository;
    private PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();

    public AnatomyTermDetailController() {
        setCommandClass(AnatomySearchBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        LOG.info("Start Anatomy Term Detail Controller");
        AnatomySearchBean form = (AnatomySearchBean) command;
        AnatomyItem term = retrieveAnatomyTermData(form);
        if (term == null){
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, form.getAnatomyItem().getZdbID());
		}

        retrieveExpressedGenesData(term, form);
        retrieveHighQualityProbeData(term, form);
        retrieveAntibodyData(term, form);
        retrieveMutantData(term, form);
        retrieveMorpholinoData(term, form);

        ModelAndView modelAndView = new ModelAndView("anatomy-item.page", LookupStrings.FORM_BEAN, form);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, term.getName());

        return modelAndView;
    }

    private AnatomyItem retrieveAnatomyTermData(AnatomySearchBean form) {
        AnatomyItem ai ; 
        try{
            ai = anatomyRepository.getAnatomyTermByID(form.getAnatomyItem().getZdbID());
        }
        catch(Exception e){
            LOG.error("failed to get anatomy term from form["+form+"]");
            LOG.error("anatomyItem["+form.getAnatomyItem()+"]");
            LOG.error("zdbID["+form.getAnatomyItem().getZdbID()+"]");
            LOG.error("error",e);
            ai = null ;
        }
        if (ai == null){
            return null;
        }
        List<AnatomyRelationship> relationships = anatomyRepository.getAnatomyRelationships(ai);
        ai.setRelatedItems(relationships);
        form.setAnatomyItem(ai);
        return ai;
    }

    private void retrieveHighQualityProbeData(AnatomyItem anatomyTerm, AnatomySearchBean form) {
        PaginationResult<HighQualityProbe> hqp = publicationRepository.getHighQualityProbeNames(anatomyTerm, AnatomySearchBean.MAX_NUMBER_PROBES);
        form.setHighQualityProbeGenes(hqp.getPopulatedResults());
        createHQPStatistics(hqp.getPopulatedResults(), anatomyTerm);

        int numberOfHighQualityProbes = hqp.getTotalCount() ;
        form.setNumberOfHighQualityProbes(numberOfHighQualityProbes);
    }

    private void retrieveExpressedGenesData(AnatomyItem anatomyTerm, AnatomySearchBean form) {

        PaginationResult<MarkerStatistic> expresionMarkersResult =
                publicationRepository.getAllExpressedMarkers(anatomyTerm, 0, AnatomySearchBean.MAX_NUMBER_EPRESSED_GENES);

        List<MarkerStatistic> markers = expresionMarkersResult.getPopulatedResults() ;

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
        form.setTotalNumberOfExpressedGenes(expresionMarkersResult.getTotalCount());

        AnatomyStatistics statistics = anatomyRepository.getAnatomyStatistics(anatomyTerm.getZdbID());
        form.setAnatomyStatistics(statistics);
    }

    private void retrieveMutantData(AnatomyItem ai, AnatomySearchBean form) {
        PaginationResult<Genotype> genotypeResult = mutantRepository.getGenotypesByAnatomyTerm(ai,false,
                AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        form.setGenotypeCount(genotypeResult.getTotalCount());

        List<Genotype> genotypes = genotypeResult.getPopulatedResults() ;
        form.setGenotypes(genotypes);
        List<GenotypeStatistics> genoStats = createGenotypeStats(genotypes, ai);
        form.setGenotypeStatistics(genoStats);

        AnatomyStatistics statistics = anatomyRepository.getAnatomyStatisticsForMutants(ai.getZdbID());
        form.setAnatomyStatisticsMutant(statistics);
    }

    /**
     *  Note: method 1 - very slow to do one query and then split
     *  because you need to rehydrate each instance
     *  in order to compare. So instead did as two separate queries.
     * @param ai ao term
     * @param form form bean
     */
    private void retrieveMorpholinoData(AnatomyItem ai, AnatomySearchBean form) {


        // same
        int wildtypeCount = 0 ;
        int nonWildtypeCount = 0 ;
        List<GenotypeExperiment> wildtypeExperiments ;
        List<GenotypeExperiment> nonWildtypeExperiments ;


        PaginationResult<GenotypeExperiment> wildtypeMorphResults =
                mutantRepository.getGenotypeExperimentMorhpolinosByAnatomy(ai, true, AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        wildtypeCount = wildtypeMorphResults.getTotalCount() ;
        wildtypeExperiments = wildtypeMorphResults.getPopulatedResults() ;


        PaginationResult<GenotypeExperiment> nonWildtypeMorphResults =
                mutantRepository.getGenotypeExperimentMorhpolinosByAnatomy(ai, false, AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        nonWildtypeCount = nonWildtypeMorphResults.getTotalCount() ;
        nonWildtypeExperiments = nonWildtypeMorphResults.getPopulatedResults() ;

        // same
        form.setWildtypeMorpholinoCount(wildtypeCount);
        form.setMutantMorpholinoCount(nonWildtypeCount);

        List<MorpholinoStatistics> morpholinoStats = createMorpholinoStats(wildtypeExperiments, ai);
        form.setAllMorpholinos(morpholinoStats);


        List<MorpholinoStatistics> mutantMorphStats = createMorpholinoStats(nonWildtypeExperiments, ai);
        form.setNonWildtypeMorpholinos(mutantMorphStats);

    }

    private void retrieveAntibodyData(AnatomyItem aoTerm, AnatomySearchBean form) {

        int antibodyCount = antibodyRepository.getAntibodiesByAOTermCount(aoTerm);
        form.setAntibodyCount(antibodyCount);

        PaginationBean pagination = new PaginationBean();
        pagination.setMaxDisplayRecords(AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        List<Antibody> antibodies = antibodyRepository.getAntibodiesByAOTerm(aoTerm, pagination);
        List<AntibodyStatistics> abStats = createAntibodyStatistics(antibodies, aoTerm);
        form.setAntibodyStatistics(abStats);
    }

    private List<AntibodyStatistics> createAntibodyStatistics(List<Antibody> antibodies, AnatomyItem aoTerm) {
        if (antibodies == null)
            return null;

        List<AntibodyStatistics> stats = new ArrayList<AntibodyStatistics>();
        for (Antibody antibody : antibodies) {
            AntibodyStatistics stat = new AntibodyStatistics(antibody, aoTerm);
            stats.add(stat);
        }
        return stats;
    }

    /**
     * This method adds the figures and publications to the HighQualityProbe objects.
     *
     * @param hqp  list of HighQualityProbe
     * @param term Anatomical Structure
     */
    private void createHQPStatistics(List<HighQualityProbe> hqp, AnatomyItem term) {
        if (hqp == null)
            return;

        for (HighQualityProbe probe : hqp) {
            PublicationRepository pr = RepositoryFactory.getPublicationRepository();
            List<Figure> figs = pr.getFiguresPerProbeAndAnatomy(probe.getGene(), probe.getSubGene(), term);
            if (!CollectionUtils.isEmpty(figs)) {
                probe.setFigures(figs);
                List<Publication> pubs =
                        pr.getPublicationsWithFiguresPerProbeAndAnatomy(probe.getGene(), probe.getSubGene(), term);
                probe.setPublications(pubs);
            }
        }
    }

    private List<GenotypeStatistics> createGenotypeStats(List<Genotype> genotypes, AnatomyItem ai) {
        if (genotypes == null || ai == null)
            return null;

        List<GenotypeStatistics> stats = new ArrayList<GenotypeStatistics>();
        for (Genotype genoType : genotypes) {
            GenotypeStatistics stat = new GenotypeStatistics(genoType, ai);
            stats.add(stat);
        }
        return stats;
    }

    protected static List<MorpholinoStatistics> createMorpholinoStats(List<GenotypeExperiment> morpholinos, AnatomyItem ai) {
        if (morpholinos == null || ai == null)
            return null;

        List<MorpholinoStatistics> stats = new ArrayList<MorpholinoStatistics>();
        for (GenotypeExperiment genoExp : morpholinos) {
            MorpholinoStatistics stat = new MorpholinoStatistics(genoExp, ai);
            stats.add(stat);
        }
        return stats;
    }

    public void setAnatomyRepository(AnatomyRepository anatomyRepository) {
        this.anatomyRepository = anatomyRepository;
    }

    public void setMutantRepository(MutantRepository mutantRepository) {
        this.mutantRepository = mutantRepository;
    }

}
