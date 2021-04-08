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
        List<String> yearStrings = new ArrayList<>();
        for (Date date : dates) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int yearNum = cal.get(Calendar.YEAR);
            String year = null;
            if (yearNum > 2015) {
                year = yearNum - 1 + "";
            } else {
                year = yearNum + "";
            }
            if (!yearStrings.contains(year)) {
                yearStrings.add(year);
            }
        }

        List<AnnualStats> annualStatsList = RepositoryFactory.getInfrastructureRepository().getAnnualStats();

        Calendar cal = Calendar.getInstance();
        // category, type, list of stats
        Map<String, Map<String, List<AnnualStats>>> categoryTypeStats = annualStatsList.stream()
                .filter(stat -> {
                    cal.setTime(stat.getDate());
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    return year != 2015 && ((month == 0 && day == 1) || (month == 0 && day == 31) || (month == 11 && day == 31)) && !stat.getType().equals("Full length cDNA clones (ZGC)");
                })
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
        int numberOfYears = categoryTypeStats.entrySet().iterator().next().getValue().entrySet().iterator().next().getValue().size();
        if(numberOfYears != yearStrings.size()) {
            yearStrings.remove(yearStrings.size() - 1);
        }
        model.addAttribute("years", yearStrings);

        return "infrastructure/annual-stats-view";
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

