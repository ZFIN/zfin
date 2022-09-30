package org.zfin.infrastructure.presentation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.infrastructure.AnnualStats;
import org.zfin.repository.RepositoryFactory;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Controller
@RequestMapping("/infrastructure")
public class AnnualStatsController {

    private Logger logger = LogManager.getLogger(AnnualStatsController.class);

    @RequestMapping(value = "/annual-stats-view")
    public String getAnnualStats(Model model) throws Exception {
        List<Date> dates = RepositoryFactory.getInfrastructureRepository().getDistinctDatesFromAnnualStats();
        List<AnnualStats> annualStatsList = RepositoryFactory.getInfrastructureRepository().getAnnualStats();

        Calendar cal = Calendar.getInstance();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        // find stat dates to include for each year
        List<Date> keyDates = new ArrayList<>();
        List<Integer> statsForYear = new ArrayList<>();

        // go through all years and determine the key date
        for (int indexYear = 1997; indexYear < currentYear + 1; indexYear++) {
            if (indexYear == 2015)
                continue;
            for (Date date : dates) {
                cal.setTime(date);
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                if ((year == indexYear) && isEndOfYear(month, day)) {
                    keyDates.add(date);
                    statsForYear.add(indexYear);
                    break;
                } else if ((year == indexYear + 1) && isStartOfNextYear(month, day)) {
                    keyDates.add(date);
                    statsForYear.add(indexYear);
                    break;
                }
            }
        }
        // for the current year we take the latest series.
        keyDates.add(dates.get(dates.size() - 1));
        statsForYear.add(currentYear);

        int startYear = 1998;
        // category, type, list of stats
        Map<String, Map<String, List<AnnualStats>>> categoryTypeStats = annualStatsList.stream()
            .filter(stat -> (keyDates.contains(stat.getDate()) && !stat.getType().equals("Full length cDNA clones (ZGC)")))
            // sort by section first
            // then by type
            // to have the groupings sorted
            .sorted(Comparator.comparing((AnnualStats stat) -> titles.indexOf(stat.getSection()))
                .thenComparing((AnnualStats stat) -> categories.indexOf(stat.getType())))
            .collect(groupingBy(AnnualStats::getSection, LinkedHashMap::new,
                groupingBy(AnnualStats::getType, LinkedHashMap::new, Collectors.collectingAndThen(toList(), annualStats -> {
                    annualStats.sort(Comparator.comparing(AnnualStats::getDate));
                    return annualStats;
                }))));


        model.addAttribute("statsMap", categoryTypeStats);
        model.addAttribute("years", statsForYear);
        return "infrastructure/annual-stats-view";
    }

    // Dec 31st or Jan 1st or Jan 31st
    private boolean isEndOfYearOrStartOfNextYear(int month, int day) {
        return isStartOfNextYear(month, day) || isEndOfYear(month, day);
    }

    // Dec 31st
    private boolean isEndOfYear(int month, int day) {
        return (month == 11 && day == 31);
    }

    // Jan 1st or Jan 31st
    private boolean isStartOfNextYear(int month, int day) {
        return (month == 0 && day == 1) || (month == 0 && day == 31);
    }

    static List<String> categories = List.of("Genes",
        "Genes on Assembly",
        "Transcripts",
        "EST/cDNAs",
        "Features",
        "Transgenic Features",
        "Transgenic Constructs",
        "Transgenic Genotypes",
        "Genotypes",
        "Genes with Any GO annotations",
        "Genes with IEA GO annotations",
        "Genes with Non-IEA GO Annotation",
        "Total GO Annotations",
        "Genes with OMIM phenotypes",
        "Morpholinos",
        "TALEN",
        "CRISPR",
        "Antibodies",
        "Gene expression patterns",
        "Gene expression experiments",
        "Phenotype statements",
        "Images",
        "Anatomical structures",
        "Genes with expression data",
        "Genes with a phenotype",
        "Expression Phenotype",
        "Diseases with Models",
        "Disease Models",
        "Mapped markers",
        "Links to other databases",
        "All Publications",
        "Journal Publications",
        "Researchers",
        "Laboratories",
        "Companies",
        "Genes w/Human Orthology",
        "Genes w/Mouse Orthology"
    );

    static List<String> titles = List.of("Genes",
        "Genetics",
        "Functional Annotation",
        "Reagents",
        "Expression & Phenotype",
        "Genomics",
        "Community information",
        "Orthology");
}

