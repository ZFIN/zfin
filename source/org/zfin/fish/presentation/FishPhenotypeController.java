package org.zfin.fish.presentation;


import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.feature.presentation.GenotypeBean;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.expression.Figure;
import org.zfin.infrastructure.ZfinFigureEntity;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.zfin.repository.RepositoryFactory.getFishRepository;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;
import org.apache.commons.lang.StringUtils;
import org.zfin.mutant.Fish;

/**
 * This class serves the phenotype summary page.
 */
@Controller
@RequestMapping(value = "/fish")
public class FishPhenotypeController {

    @ModelAttribute("formBean")
    private FishSearchFormBean getDefaultSearchForm() {
        return new FishSearchFormBean();
    }


    private static Logger LOG = Logger.getLogger(FishPhenotypeController.class);

    @RequestMapping("/fish-publication-list")
    public String fishCitationList(@RequestParam(value = "fishID", required = true) String fishID,
                                   @RequestParam(value = "orderBy", required = false) String orderBy,
                                       Model model) throws Exception {

        Fish fish = getMutantRepository().getFish(fishID);
        if (fish == null)
            return LookupStrings.idNotFound(model, fishID);
        FishPublicationBean bean = new FishPublicationBean();
        bean.setFish(fish);
        if (StringUtils.isNotEmpty(orderBy))
            bean.setOrderBy(orderBy);
        model.addAttribute("formBean", bean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "publication list");
        return "fish/fish-publication-list.page";
    }

    @RequestMapping(value = "/phenotype/figures", method = RequestMethod.GET)
    public String getFishPhenotypeFigsPopup(@RequestParam(value = "fishID", required = true) String fishID,
                                            @ModelAttribute("formBean") FishSearchFormBean formBean,
                                            Model model) throws Exception {
		LOG.info("MartFish phenotype figures");
		FishSearchCriteria criteria = new FishSearchCriteria(formBean);
        if (criteria == null)
            criteria = new FishSearchCriteria();

        Set<ZfinFigureEntity> zfinFigureEntities = FishService.getFiguresByFishAndTerms(fishID, criteria.getPhenotypeAnatomyCriteria().getValues());

        if (zfinFigureEntities == null)
            return "record-not-found.popup";

        SortedSet<Figure> figures = new TreeSet<Figure>();
        for (ZfinFigureEntity figureEntity : zfinFigureEntities) {
           Figure figure = getPublicationRepository().getFigure(figureEntity.getID());
           figures.add(figure);
        }
        model.addAttribute("phenotypeFigures", figures);
        return "fish/fish-phenotype-figures.insert";
    }
}