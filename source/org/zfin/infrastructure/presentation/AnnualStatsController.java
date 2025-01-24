package org.zfin.infrastructure.presentation;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.infrastructure.AnnualStats;
import org.zfin.repository.RepositoryFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Log4j2
@Controller
@RequestMapping("/infrastructure")
public class AnnualStatsController {


    private static final int STATS_BEGIN_YEAR = 1997;

    @RequestMapping(value = "/annual-stats-view")
    public String getAnnualStats(Model model, @RequestParam(required = false) Integer beginYear, @RequestParam(required = false) Integer endYear) {
        if (beginYear == null) {
            beginYear = currentYear() - 10;
        }
        if (endYear == null) {
            endYear = currentYear();
        }

        Map<String, Map<String, List<AnnualStats>>> categoryTypeStats = getAnnualStatsData(beginYear, endYear);
        model.addAttribute("statsMap", categoryTypeStats);
        model.addAttribute("years", yearRange(beginYear, endYear));
        model.addAttribute("allYears", yearRange(STATS_BEGIN_YEAR, currentYear()));
        model.addAttribute("beginYear", beginYear);
        model.addAttribute("endYear", endYear);
        return "infrastructure/annual-stats-view";
    }

    @RequestMapping(value = "/annual-stats-view-download")
    public void getAnnualStatsDownload(HttpServletResponse response) {
        Map<String, Map<String, List<AnnualStats>>> categoryTypeStats = getAnnualStatsData(STATS_BEGIN_YEAR, currentYear());
        response.setContentType("data:text/csv;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=zfin-annual-stats.csv");

        try {
            OutputStream resOs = response.getOutputStream();
            OutputStream buffOs = new BufferedOutputStream(resOs);
            OutputStreamWriter outputwriter = new OutputStreamWriter(buffOs);
            CSVFormat csvFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
            CSVPrinter csvPrinter = new CSVPrinter(outputwriter, csvFormat);

            //print column headers 
            List<String> headerRow = yearRangeForCSV();
            headerRow.add(0, "");
            csvPrinter.printRecord(headerRow);

            Set<Map.Entry<String, Map<String, List<AnnualStats>>>> entrySet = categoryTypeStats.entrySet();
            for(Map.Entry<String, Map<String, List<AnnualStats>>> entry : entrySet) {
                String generalCategory = entry.getKey();
                Map<String, List<AnnualStats>> val = entry.getValue();
                csvPrinter.printRecord(generalCategory);

                Set<Map.Entry<String, List<AnnualStats>>> statRowsInCategory = val.entrySet();
                for(Map.Entry<String, List<AnnualStats>> statRow : statRowsInCategory) {
                    ArrayList<String> csvRow = new ArrayList<>();
                    csvRow.add(statRow.getKey());
                    for(AnnualStats stat : statRow.getValue()) {
                        if (stat.getCount() == 0) {
                            csvRow.add("");
                        } else {
                            csvRow.add("" + stat.getCount());
                        }
                    }
                    csvPrinter.printRecord(csvRow);
                }
            }

            outputwriter.flush();
            outputwriter.close();
        } catch (IOException e) {
            log.error(e);
        }
    }

    private List<String> yearRangeForCSV() {
        return yearRange(STATS_BEGIN_YEAR, currentYear())
                .stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    private List<Integer> yearRange(Integer beginYear, Integer endYear) {
        List<Integer> years = new ArrayList<>();
        for(int i = beginYear; i <= endYear; i++) {
            years.add(i);
        }
        return years;
    }

    private Map<String, Map<String, List<AnnualStats>>> getAnnualStatsData(Integer beginYear, Integer endYear) {
        List<AnnualStats> annualStatsList = RepositoryFactory.getInfrastructureRepository().getAnnualStats();

        Map<Date,List<AnnualStats>> map = annualStatsList.stream().collect(groupingBy(AnnualStats::getDate));
        List<Date> dates = new ArrayList<>(map.keySet());

        // find stat dates to include for each year
        List<Date> keyDates = new ArrayList<>();
        List<Integer> statsForYear = new ArrayList<>();

        // go through all years and determine the key dates that are used to calculate a year's stats
        for (int indexYear = beginYear; indexYear <= endYear; indexYear++) {
            for (Date date : dates) {
                if (isEndOfThisYear(indexYear, date)) {
                    keyDates.add(date);
                    statsForYear.add(indexYear);
                    break;
                } else if (isStartOfNextYear(indexYear, date)) {
                    keyDates.add(date);
                    statsForYear.add(indexYear);
                    break;
                }
            }
        }
        // for the current year we take the latest series.
        keyDates.add(dates.get(dates.size() - 1));
        statsForYear.add(currentYear());

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

        return categoryTypeStats;
    }

    private int currentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    // Dec 31st
    private boolean isEndOfYear(int month, int day) {
        return (month == 11 && day == 31);
    }

    private boolean isStartOfYear(int month, int day) {
        return (month == 0 && day == 1) || (month == 0 && day == 31);
    }

    // Jan 1st or Jan 31st
    private boolean isStartOfNextYear(int indexYear, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return (year == indexYear + 1) && isStartOfYear(month, day);
    }

    private boolean isEndOfThisYear(int indexYear, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return (year == indexYear) && isEndOfYear(month, day);
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

