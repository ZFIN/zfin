package org.zfin.fish.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.presentation.ExpressionDisplay;
import org.zfin.expression.presentation.GeneCentricExpressionData;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.expression.service.ExpressionService;
import org.zfin.feature.presentation.GenotypeDetailController;
import org.zfin.fish.FeatureGene;
import org.zfin.fish.MutationType;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.ExpressedGene;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.DiseaseModelDisplay;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;
import static org.zfin.repository.RepositoryFactory.getPhenotypeRepository;

/**
 * Controller that serves the fish detail page.
 */
@Controller
@RequestMapping(value = "/fish")
public class FishDetailController {

    private static final Logger LOG = Logger.getLogger(FishDetailController.class);

    @Autowired
    GenotypeDetailController genotypeDetailController;


    @RequestMapping(value = "/fish-detail/{ID}")
    protected String showCuratedFish(@PathVariable("ID") String fishZdbId, Model model, HttpServletResponse response) {

        Fish fish = RepositoryFactory.getMutantRepository().getFish(fishZdbId);

        if (fish == null) {
            String newZdbID = RepositoryFactory.getInfrastructureRepository().getNewZdbID(fishZdbId);
            if (newZdbID != null) {
                LOG.debug("found a replaced zdbID for: " + fishZdbId + "->" + newZdbID);
                return "redirect:/" + newZdbID;
            }
            else{
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return LookupStrings.idNotFound(model, fishZdbId);
            }
        }

        if (fish.isWildtypeWithoutReagents()) {
            return "redirect:/" + fish.getGenotype().getZdbID();
        }

        model.addAttribute("fish", fish);


        // phenotype
        List<PhenotypeStatementWarehouse> phenotypeStatements = getMutantRepository().getPhenotypeStatementWarehousesByFish(fish);
        model.addAttribute("phenotypeStatements", phenotypeStatements);
        model.addAttribute("phenotypeDisplays", PhenotypeService.getPhenotypeDisplays(phenotypeStatements, "condition", "phenotypeStatement"));

        // disease model
        List<DiseaseAnnotationModel> diseaseAnnotations = getPhenotypeRepository().getHumanDiseaseModelsByFish(fishZdbId);
        model.addAttribute("diseases", getDiseaseModelDisplay(diseaseAnnotations));

        // Expression data
        ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();
        List<ExpressionResult> fishExpressionResults = expressionRepository.getExpressionResultsByFish(fish);
        List<String> fishExpressionFigureIDs = expressionRepository.getExpressionFigureIDsByFish(fish);
        List<String> fishExpressionPublicationIDs = expressionRepository.getExpressionPublicationIDsByFish(fish);
        List<ExpressionDisplay> fishExpressionDisplays = ExpressionService.createExpressionDisplays(fish.getZdbID(), fishExpressionResults, fishExpressionFigureIDs, fishExpressionPublicationIDs, true);
        model.addAttribute("geneCentricExpressionDataList", fishExpressionDisplays);


        model.addAttribute("totalNumberOfPublications", FishService.getCitationCount(fish));
        model.addAttribute("fishIsWildtypeWithoutReagents", fish.isWildtypeWithoutReagents());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Fish: " + getTitle(fish.getName()));

        return "fish/fish-detail.page";
    }

    private String getTitle(String fishName) {
        fishName = fishName.replaceAll("<sup>", "^");
        fishName = fishName.replaceAll("</sup>", "");
        return fishName;
    }


 @RequestMapping(value = "/fish-detail-popup/{ID}")

    protected String showFishDetailPopup(Model model, @PathVariable("ID") String fishZdbId) {
        Fish fish = RepositoryFactory.getMutantRepository().getFish(fishZdbId);

        List<FeatureGene> genomicFeatures = new ArrayList<>();
        // remove any featureGenes that have an STR mutation type and use the resulting list
        // to populate the form's genomicFeatures field
        CollectionUtils.select(FishService.getFeatureGenes(fish, false), new Predicate() {
            @Override
            public boolean evaluate(Object featureGene) {
                MutationType m = ((FeatureGene) featureGene).getMutationTypeDisplay();
                return m != MutationType.MORPHOLINO && m != MutationType.CRISPR && m != MutationType.TALEN;
            }
        }, genomicFeatures);

        model.addAttribute("fish", fish);
        model.addAttribute("fishGenomicFeatures", genomicFeatures);
        return "fish/fish-detail-popup.popup";
    }

    private static Collection<DiseaseModelDisplay> getDiseaseModelDisplay(Collection<DiseaseAnnotationModel> models) {
        MultiKeyMap map = new MultiKeyMap();
       for (DiseaseAnnotationModel model : models) {
            if (!map.containsKey(model.getDiseaseAnnotation().getDisease(), model.getFishExperiment())) {
                map.put(model.getDiseaseAnnotation().getDisease(), model.getFishExperiment(), new ArrayList<Publication>());
            }
            if (!((Collection<Publication>) map.get(model.getDiseaseAnnotation().getDisease(), model.getFishExperiment())).contains(model.getDiseaseAnnotation().getPublication()))
                 ((Collection<Publication>) map.get(model.getDiseaseAnnotation().getDisease(), model.getFishExperiment())).add(model.getDiseaseAnnotation().getPublication());
        }

        List<DiseaseModelDisplay> modelDisplays = new ArrayList<>();
        MapIterator it = map.mapIterator();
        while (it.hasNext()) {
            it.next();
            MultiKey key = (MultiKey) it.getKey();
            DiseaseModelDisplay display = new DiseaseModelDisplay();
            display.setDisease((GenericTerm) key.getKey(0));
            display.setExperiment((FishExperiment) key.getKey(1));
            display.setPublications((Collection<Publication>) it.getValue());
            modelDisplays.add(display);
        }
        Collections.sort(modelDisplays);
        return modelDisplays;
    }



    private class MarkerCentricOrdering implements Comparator<GeneCentricExpressionData> {

        @Override
        public int compare(GeneCentricExpressionData leftsummary, GeneCentricExpressionData rightSummary) {
            ExpressedGene geneLeft = leftsummary.getExpressedGene();
            ExpressedGene geneRight = rightSummary.getExpressedGene();
            return geneLeft.getGene().compareTo(geneRight.getGene());
        }
    }
}

