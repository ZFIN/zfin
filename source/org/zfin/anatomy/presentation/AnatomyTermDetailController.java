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
import org.zfin.expression.Figure;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.MarkerStatistic;
import org.zfin.marker.presentation.ExpressedGeneDisplay;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.Morpholino;
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

    private PublicationRepository pr = RepositoryFactory.getPublicationRepository();
    private AnatomyRepository anatomyRepository;
    private MutantRepository mutantRepository;

    public AnatomyTermDetailController() {
        setCommandClass(AnatomySearchBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        LOG.info("Start Anatomy Term Detail Controller");
        AnatomySearchBean form = (AnatomySearchBean) command;
        AnatomyItem term = retrieveAnatomyTermData(form);

        retrieveExpressedGenesData(term, form);
        retrieveHighQualityProbeData(term, form);
        retrieveMutantData(term, form);
        retrieveMorpholinoData(term, form);
        retrieveNumberOfPublications(term, form);

        ModelAndView modelAndView = new ModelAndView("anatomy-item.page", LookupStrings.FORM_BEAN, form);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, term.getName());

        return modelAndView;
    }

    private void retrieveNumberOfPublications(AnatomyItem ai, AnatomySearchBean form) {
        int numberOfPublications = pr.getNumberOfPublications(ai.getName());
        form.setNumberOfPublications(numberOfPublications);
    }

    private AnatomyItem retrieveAnatomyTermData(AnatomySearchBean form) {
        AnatomyItem ai = anatomyRepository.loadAnatomyItem(form.getAnatomyItem());
        List<AnatomyRelationship> relationships = anatomyRepository.getAnatomyRelationships(ai);
        ai.setRelatedItems(relationships);
        form.setAnatomyItem(ai);
        return ai;
    }

    private void retrieveHighQualityProbeData(AnatomyItem anatomyTerm, AnatomySearchBean form) {
        List<HighQualityProbe> hqp = pr.getHighQualityProbeNames(anatomyTerm, AnatomySearchBean.MAX_NUMBER_PROBES);
        form.setHighQualityProbeGenes(hqp);
        createHQPStatistics(hqp, anatomyTerm);

        int numberOfHighQualityProbes = pr.getNumberOfHighQualityProbes(anatomyTerm);
        form.setNumberOfHighQualityProbes(numberOfHighQualityProbes);
    }

    private void retrieveExpressedGenesData(AnatomyItem anatomyTerm, AnatomySearchBean form) {
        List<MarkerStatistic> markers =
                pr.getAllExpressedMarkers(anatomyTerm, 1, AnatomySearchBean.MAX_NUMBER_EPRESSED_GENES);
        List<ExpressedGeneDisplay> expressedGenes = AllExpressedGenesController.createFigureStatistics(markers);
        form.setAllExpressedMarkers(expressedGenes);
        form.setTotalNumberOfFiguresPerAnatomyItem(pr.getTotalNumberOfFiguresPerAnatomyItem(anatomyTerm));
        form.setTotalNumberOfImagesPerAnatomyItem(pr.getTotalNumberOfImagesPerAnatomyItem(anatomyTerm));

        int markerCount = pr.getAllExpressedMarkersCount(anatomyTerm);
        form.setTotalNumberOfExpressedGenes(markerCount);

        AnatomyStatistics statistics = anatomyRepository.getAnatomyStatistics(anatomyTerm.getZdbID());
        form.setAnatomyStatistics(statistics);
    }

    private void retrieveMutantData(AnatomyItem ai, AnatomySearchBean form) {
        int mutantCount = mutantRepository.getNumberOfMutants(ai.getZdbID(), false);
        form.setGenotypeCount(mutantCount);

        List<Genotype> genotypes =
                mutantRepository.getGenotypesByAnatomyTerm(ai, false, AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        form.setGenotypes(genotypes);
        List<GenotypeStatistics> genoStats = createGenotypeStats(genotypes, ai);
        form.setGenotypeStatistics(genoStats);

        AnatomyStatistics statistics = anatomyRepository.getAnatomyStatisticsForMutants(ai.getZdbID());
        form.setAnatomyStatisticsMutant(statistics);
    }

    private void retrieveMorpholinoData(AnatomyItem ai, AnatomySearchBean form) {
        List<Morpholino> morphs =
                mutantRepository.getPhenotypeMorhpolinosByAnatomy(ai, AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        List<MorpholinoStatistics> morpholinoStats = createMorpholinoStats(morphs, ai);
        form.setAllMorpholinos(morpholinoStats);
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

    private List<MorpholinoStatistics> createMorpholinoStats(List<Morpholino> morpholinos, AnatomyItem ai) {
        if (morpholinos == null || ai == null)
            return null;

        List<MorpholinoStatistics> stats = new ArrayList<MorpholinoStatistics>();
        for (Morpholino genoType : morpholinos) {
            MorpholinoStatistics stat = new MorpholinoStatistics(genoType, ai);
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
