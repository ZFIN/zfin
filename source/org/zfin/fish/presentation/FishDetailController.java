package org.zfin.fish.presentation;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.fish.FeatureGene;
import org.zfin.fish.MutationType;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.seo.CanonicalLinkConfig;
import org.zfin.mutant.Fish;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * Controller that serves the fish detail page.
 */
@Controller
@RequestMapping(value = "/fish")
public class FishDetailController {

    private static final Logger LOG = LogManager.getLogger(FishDetailController.class);

    @RequestMapping(value = "/{zdbID}")
    protected String showFish(@PathVariable String zdbID, Model model) {
        CanonicalLinkConfig.addCanonicalIfFound(model);

        Fish fish = RepositoryFactory.getMutantRepository().getFish(zdbID);
        if (fish == null) {
            String replacedZdbID = RepositoryFactory.getInfrastructureRepository().getReplacedZdbID(zdbID);
            if (replacedZdbID != null) {
                LOG.debug("found a replaced zdbID for: " + zdbID + "->" + replacedZdbID);
                Fish replacedFish = RepositoryFactory.getMutantRepository().getFish(zdbID);
                if (replacedFish != null) {
                    fish = replacedFish;
                } else {
                    return "redirect:/" + replacedZdbID;
                }
            }
            replacedZdbID = getInfrastructureRepository().getWithdrawnZdbID(zdbID);
            if (replacedZdbID != null) {
                LOG.debug("found a withdrawn zdbID for: " + zdbID + "->" + replacedZdbID);
                return "redirect:/" + replacedZdbID;
            }
        }

        if (fish == null) {
            model.addAttribute(LookupStrings.ZDB_ID, zdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        if (fish.isWildtypeWithoutReagents()) {
            return "redirect:/" + fish.getGenotype().getZdbID();
        }

        model.addAttribute("fish", fish);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Fish: " + getTitle(fish.getName()));
        return "fish/fish-view";
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
        return "fish/fish-detail-popup";
    }
}

