package org.zfin.marker.presentation;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.NavigationMenuOptions;
import org.zfin.marker.Marker;

import java.util.List;

/**
 *
 */
@Controller
@RequestMapping("/gene/stats")
public class GeneStatisController {

    @RequestMapping("")
    public String viewPublicationUberTable(Model model) {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Marker View Statistics " );
        return "marker/stats/marker-view-stats";
    }

    @RequestMapping("/view")
    public String getPhenotypeSelectNote(@RequestParam String section,
                                         Model model) {
        StatisticReference statsRef = StatisticReference.getStatisticReference(section);
        if (statsRef != null) {
            model.addAttribute("title", statsRef.getTitle());
            model.addAttribute("category", statsRef.getCategory());
        }
        return "marker/stats/view";
    }

    @Getter
    enum StatisticReference {
        DATASETS("Transcript", "transcripts"),
        ZEBRASHARE(NavigationMenuOptions.ZEBRASHARE.value, "zebrashare");

        StatisticReference(String title, String category) {
            this.category = category;
            this.title = title;
        }

        private String category;
        private String title;

        public static StatisticReference getStatisticReference(String title) {
            for (StatisticReference val : values()) {
                if (val.getTitle().equals(title))
                    return val;
            }
            return null;
        }
    }
}

