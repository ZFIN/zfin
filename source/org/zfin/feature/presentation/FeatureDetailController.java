package org.zfin.feature.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.feature.Feature;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.feature.repository.FeatureService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.presentation.FeatGenoStatistics;
import org.zfin.mutant.presentation.GenoExpStatistics;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;


@Controller
//@RequestMapping("/feature") // already provided in the context
public class FeatureDetailController {
    private static final Logger LOG = Logger.getLogger(FeatureDetailController.class);

    private FeatureRepository featureRepository = RepositoryFactory.getFeatureRepository();
    private MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();

    @RequestMapping( value={
//            "/detail/{zdbID}", // TODO: move to new one once entire context is moved
//            "/detail?feature.zdbID={zdbID}" // using old link
            "/detail" // using old link
    }
    )
    protected String getOldFeatureDetailPage(
            @RequestParam(value="feature.zdbID",required = false) String featureZdbID,
            @RequestParam(value="genotype.zdbID",required = false) String genotypeZdbID,
                                             Model model){
        if(featureZdbID!=null){
            return "redirect:/action/feature/feature-detail?zdbID="+featureZdbID;
        }
        else
        if(genotypeZdbID!=null){
            return "redirect:/action/genotype/genotype-detail?zdbID="+genotypeZdbID;
        }
        else{
            model.addAttribute(LookupStrings.ZDB_ID, featureZdbID) ;
            return LookupStrings.RECORD_NOT_FOUND_PAGE ;
        }
    }


    @RequestMapping( value={
//            "/detail/{zdbID}", // TODO: move to new one once entire context is moved
//            "/detail?feature.zdbID={zdbID}" // using old link
            "/feature-detail" // using old link
    }
    )
    protected String getFeatureDetail(@RequestParam String zdbID, Model model) {
        LOG.info("Start Feature Detail Controller");
        Feature feature = featureRepository.getFeatureByID(zdbID);
        if (feature == null){
            model.addAttribute(LookupStrings.ZDB_ID, zdbID) ;
            return LookupStrings.RECORD_NOT_FOUND_PAGE ;
        }

        FeatureBean form = new FeatureBean();
        form.setZdbID(zdbID);
        form.setFeature(feature);
        form.setSortedMarkerRelationships(FeatureService.getSortedMarkerRelationships(feature));
        form.setSortedConstructRelationships(FeatureService.getSortedConstructRelationships(feature));
        form.setFeatureTypeAttributions(FeatureService.getFeatureTypeAttributions(feature));
        form.setSinglePublication(FeatureService.getSinglePublication(feature));
        form.setFeatureMap(FeatureService.getFeatureMap(feature));
        form.setFeatureLocations(FeatureService.getFeatureLocations(feature));

        retrieveGenoData(feature, form);
        retrievePubData(feature, form);

        model.addAttribute(LookupStrings.FORM_BEAN, form);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, feature.getName());

        return "feature/feature-detail.page";
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
        form.setNumPubs(RepositoryFactory.getPublicationRepository().getNumberAssociatedPublicationsForZdbID(fr.getZdbID()));
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

