package org.zfin.infrastructure.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.figure.repository.FigureRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.search.Category;
import org.zfin.zebrashare.repository.ZebrashareRepository;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired
    private ZebrashareRepository zebrashareRepository;

    @Autowired
    private FigureRepository figureRepository;

    @RequestMapping(method = RequestMethod.GET)
    public String index(Model model) {
        List<Image> recentlyCuratedImages = figureRepository.getRecentlyCuratedImages();

        // shuffling with this seed causes the carousel image set to change only once a day
        long seed = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toEpochSecond();
        Collections.shuffle(recentlyCuratedImages, new Random(seed));
        List<Image> carouselImages = recentlyCuratedImages.subList(0, 10);
        List<String> sanitizedCaptions = carouselImages.stream()
                .map(Image::getFigure)
                .map(Figure::getCaption)
                .map(caption -> caption.replaceAll("<[^>]*>", ""))
                .collect(Collectors.toList());
        model.addAttribute("carouselImages", carouselImages);
        model.addAttribute("sanitizedCaptions", sanitizedCaptions);

        model.addAttribute("searchCategories", Category.getCategoryDisplayList());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "The Zebrafish Information Network");

        return "infrastructure/home/home.page";
    }

    @RequestMapping(path = "/submit-data")
    public String submitDataLandingPage(Model model) {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Ways to Submit Data");
        return "infrastructure/submit-data-landing-page.page";
    }

}
