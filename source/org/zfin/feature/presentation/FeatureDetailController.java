package org.zfin.feature.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.feature.repository.FeatureService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerService;
import org.zfin.mutant.Feature;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.presentation.FeatGenoStatistics;
import org.zfin.mutant.presentation.GenoExpStatistics;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;


public class FeatureDetailController extends AbstractCommandController {
    private static final Logger LOG = Logger.getLogger(FeatureDetailController.class);

    private FeatureRepository featureRepository = RepositoryFactory.getFeatureRepository();
    private MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();

    public FeatureDetailController() {
        setCommandClass(FeatureBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        LOG.info("Start Feature Detail Controller");
        FeatureBean form = (FeatureBean) command;
        Feature feature = featureRepository.getFeatureByID(form.getFeature().getZdbID());
        if (feature == null)
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, form.getFeature().getZdbID());

        FeatureService featStat = new FeatureService(feature);
        form.setFeatureStat(featStat);
        form.setFeature(feature);
        retrieveGenoData(feature, form);
        retrievePubData(feature, form);
        retrieveMarkerData(feature, form);

        ModelAndView modelAndView;
        modelAndView = new ModelAndView("feature-detail.page", LookupStrings.FORM_BEAN, form);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, feature.getName());

        return modelAndView;
    }

    private void retrieveGenoData(Feature fr, FeatureBean form) {
        List<Genotype> genotypes = mutantRepository.getGenotypesByFeature(fr);
        form.setGenotypes(genotypes);
        List<FeatGenoStatistics> featgenoStats = createGenotypeStats(genotypes, fr);
        form.setFeatgenoStats(featgenoStats);
        List<GenoExpStatistics> genoexpStats = createGenotypeExpStats(genotypes, fr);
        form.setGenoexpStats(genoexpStats);
    }


    private void retrievePubData(Feature fr, FeatureBean form) {
        form.setNumPubs(RepositoryFactory.getPublicationRepository().getAllAssociatedPublicationsForFeature(fr, 0).getTotalCount());
    }

    //this is to get the affected marker (markers that have 'is allele of relationships' with features. We need this to get Map locations
    private void retrieveMarkerData(Feature fr, FeatureBean form) {
        Marker marker = RepositoryFactory.getMutantRepository().getMarkerbyFeature(fr);
        if (marker != null) {
            form.setMappedMarkerBean(MarkerService.getMappedMarkers(marker));
        }
    }

    private List<FeatGenoStatistics> createGenotypeStats(List<Genotype> genotypes, Feature fr) {
        if (genotypes == null || fr == null)
            return null;
        List<FeatGenoStatistics> stats = new ArrayList<FeatGenoStatistics>();
        for (Genotype genoType : genotypes) {
            FeatGenoStatistics stat = new FeatGenoStatistics(genoType, fr);
            stats.add(stat);
        }
        return stats;
    }

    private List<GenoExpStatistics> createGenotypeExpStats(List<Genotype> genotypes, Feature fr) {
        if (genotypes == null || fr == null)
            return null;

        List<GenoExpStatistics> stats = new ArrayList<GenoExpStatistics>();
        for (Genotype genoType : genotypes) {
            GenoExpStatistics stat = new GenoExpStatistics(genoType, fr);
            stats.add(stat);
        }
        return stats;
    }

}

