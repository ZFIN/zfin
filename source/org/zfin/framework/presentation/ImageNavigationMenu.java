package org.zfin.framework.presentation;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.Model;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.figure.presentation.FigureExpressionSummary;
import org.zfin.figure.presentation.FigurePhenotypeSummary;
import org.zfin.mutant.Fish;

import java.util.List;
import java.util.Map;

/**
 * This class encapsulates the logic for which navigation items to show on the publication view page,
 * and also some of the properties like showing counts, or borders.
 * This is easier to maintain than having the logic in the JSP file.
 */
@Getter
@Setter
@Log4j
public class ImageNavigationMenu extends NavigationMenu {

    /**
     * Set up the navigation menu.
     * This uses the builder pattern available on the NavigationItem class through lombok
     */
    public ImageNavigationMenu() {
        super(
            title(NavigationMenuOptions.SUMMARY),
            title(NavigationMenuOptions.IMAGE),
            title(NavigationMenuOptions.COMMENTS),
            title(NavigationMenuOptions.FIGURE_CAPTION),
            title(NavigationMenuOptions.DEVELOPMENTAL_STAGE),
            title(NavigationMenuOptions.ORIENTATION),
            title(NavigationMenuOptions.FIGURE_DATA),
            title(NavigationMenuOptions.ACKNOWLEDGEMENT)
        );

        //Turn off counter for all menu items
        getNavigationItems().forEach(item -> item.setShowCount(false));

    }

    /**
     * Pass the page model for image-view into this method.
     * It will set up the logic of which menu items to show based on the
     * contents of the model.
     *
     * @param model The page model for publication-view
     */
    public void setModel(Model model) {
        Image image = (Image)(model.asMap().get("image"));

        this.setHidden(NavigationMenuOptions.FIGURE_CAPTION,image.getFigure() == null || image.getFigure().getCaption() == null);

        this.setHidden(NavigationMenuOptions.COMMENTS, StringUtils.isEmpty(image.getComments()));
        this.setHidden(NavigationMenuOptions.DEVELOPMENTAL_STAGE, image.getImageStage() == null || image.getImageStage().getStart() == null);
        this.setHidden(NavigationMenuOptions.ORIENTATION,
        "not specified".equals(image.getPreparation()) &&
            "not specified".equals(image.getForm()) &&
            "not specified".equals(image.getDirection()) &&
            "not specified".equals(image.getView()));

        this.setHidden(NavigationMenuOptions.FIGURE_DATA, isFigureDataEmpty(image, model));

    }

    /*
    * This method is used to determine if the figure data section should be hidden.
    * There are a few sources of data to check if they are all empty
     */
    public boolean isFigureDataEmpty(Image image, Model model) {
        boolean termsEmpty = CollectionUtils.isEmpty(image.getTerms());

        if (!termsEmpty) {
            return false;
        }

        if (image.getFigure() == null) {
            return true;
        }

        boolean expressionEmpty = false;
        boolean phenotypeEmpty = false;

        try {
            Map<Figure, FigureExpressionSummary> expressionSummaryMap = (Map<Figure, FigureExpressionSummary>) (model.asMap().get("expressionSummaryMap"));
            Map<Figure, FigurePhenotypeSummary> phenotypeSummaryMap = (Map<Figure, FigurePhenotypeSummary>) (model.asMap().get("phenotypeSummaryMap"));

            DevelopmentStage startStage = expressionSummaryMap.get(image.getFigure()).getStartStage();
            List<Fish> fish = phenotypeSummaryMap.get(image.getFigure()).getFish();

            expressionEmpty = startStage == null;
            phenotypeEmpty = CollectionUtils.isEmpty(fish);
        } catch (Exception e) {
            log.error("Error getting expression and phenotype summary maps");
        }

        return expressionEmpty && phenotypeEmpty;
    }

}
