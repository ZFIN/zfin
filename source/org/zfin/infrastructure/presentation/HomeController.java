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
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class HomeController {

    private static final int countOfDesiredRecentImages = 2;
    private static final int recentPublicationsWindowDays = 30;

    @Autowired
    private ZebrashareRepository zebrashareRepository;

    @Autowired
    private FigureRepository figureRepository;

    @RequestMapping(method = RequestMethod.GET)
    public String index(Model model) {
        List<Image> recentlyCuratedImages = figureRepository.getRecentlyCuratedImages();
        List<Image> carouselImages = reorderWithSomeRandomness(recentlyCuratedImages);

        List<String> sanitizedCaptions = carouselImages.stream()
                .map(Image::getFigure)
                .map(Figure::getCaption)
                .map(caption -> caption.replaceAll("<[^>]*>", ""))
                .collect(Collectors.toList());
        model.addAttribute("carouselImages", carouselImages);
        model.addAttribute("sanitizedCaptions", sanitizedCaptions);

        model.addAttribute("searchCategories", Category.getCategoryDisplayList());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "The Zebrafish Information Network");

        return "infrastructure/home/home";
    }

    /**
     * This method reorders the carousel images with some randomness while ensuring that we have a set of recent images.
     * It will try to find at least `countOfDesiredRecentImages` images within a window of `recentPublicationsWindowDays`.
     * If not enough recent images are found, it will expand the window until it finds enough or exhausts the search.
     * The remaining images are shuffled to provide some randomness.
     *
     * @param carouselImagesInput the input list of images
     * @return a list of reordered images for the carousel
     */
    private static List<Image> reorderWithSomeRandomness(List<Image> carouselImagesInput) {
        long seed = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toEpochSecond();
        List<Image> recentImages = getPrioritizedRecentImages(carouselImagesInput, seed);

        //remove recentImages
        carouselImagesInput.removeAll(recentImages);

        // shuffling with this seed causes the carousel image set to change only once a day
        Collections.shuffle(carouselImagesInput, new Random(seed));

        ArrayList<Image> combinedImages = new ArrayList<>(recentImages);
        combinedImages.addAll(carouselImagesInput);

        //truncate to 5 images
        if (combinedImages.size() > 5) {
            combinedImages = new ArrayList<>(combinedImages.subList(0, 5));
        }

        return combinedImages;
    }

    @RequestMapping(path = "/submit-data")
    public String submitDataLandingPage(Model model) {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Ways to Submit Data");
        return "infrastructure/submit-data-landing-page";
    }

    /**
     * This method filters the carousel images to ensure that we have a set of recent images.
     * It will try to find at least `countOfDesiredRecentImages` images within a window of `recentPublicationsWindowDays`.
     * If not enough recent images are found, it will expand the window until it finds enough or exhausts the search.
     * This is so our carousel always has some recent images.
     *
     * @param carouselImagesInput the input list of images
     * @param seed                the seed for randomization (applied at the end)
     * @return a list of prioritized recent images
     */
    private static List<Image> getPrioritizedRecentImages(List<Image> carouselImagesInput, long seed) {
        List<Image> recentImages = new ArrayList<>();

        for(int month = 1; month <= 12; month++) {
            recentImages = filterImagesToRecentDays(carouselImagesInput, recentPublicationsWindowDays * month);
            if (recentImages.size() >= countOfDesiredRecentImages) {
                break;
            }
        }

        if (recentImages.size() > countOfDesiredRecentImages) {
            Collections.shuffle(recentImages, new Random(seed));
            recentImages = recentImages.subList(0, countOfDesiredRecentImages);
        }
        return recentImages;
    }

    /**
     * Filters the input list of images to only include those published within the last `days` days.
     *
     * @param carouselImagesInput the input list of images
     * @param days                the number of days to look back
     * @return a list of images published within the last `days` days
     */
    private static List<Image> filterImagesToRecentDays(List<Image> carouselImagesInput, int days) {
        return carouselImagesInput.stream()
                .filter(image -> {
                    GregorianCalendar pubDate = image.getFigure().getPublication().getPublicationDate();
                    GregorianCalendar cutoff = new GregorianCalendar();
                    cutoff.add(Calendar.DAY_OF_YEAR, -days);
                    return pubDate.after(cutoff);
                })
                .collect(Collectors.toList());
    }

}
