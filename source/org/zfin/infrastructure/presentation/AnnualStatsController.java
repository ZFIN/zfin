package org.zfin.infrastructure.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.infrastructure.AnnualStats;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationListAdapter;
import org.zfin.publication.presentation.PublicationListBean;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

@Controller
@RequestMapping("/infrastructure")
public class AnnualStatsController {

    private Logger logger = Logger.getLogger(AnnualStatsController.class);

    @RequestMapping(value = "/annual-stats-view")
    public String getAnnualStats(Model model) throws Exception {
        List<Date> dates = RepositoryFactory.getInfrastructureRepository().getDistinctDatesFromAnnualStats();
        List<String> yearStrings = new ArrayList<>();
        for (Date date : dates) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            String year = cal.get(Calendar.YEAR) - 1 + "";
            if (!yearStrings.contains(year)) {
                yearStrings.add(year);
            }
        }
        model.addAttribute("years", yearStrings);
        int numerOfYears = yearStrings.size();
        model.addAttribute("numberOfYears", numerOfYears);

        List<AnnualStats> annualStatsList = RepositoryFactory.getInfrastructureRepository().getAnnualStats();
        List<AnnualStatsDisplay> genesStats = new ArrayList<>();
        List<AnnualStatsDisplay> geneticsStats = new ArrayList<>();
        List<AnnualStatsDisplay> faStats = new ArrayList<>();
        List<AnnualStatsDisplay> reagentsStats = new ArrayList<>();
        List<AnnualStatsDisplay> xpPhenoStats = new ArrayList<>();
        List<AnnualStatsDisplay> genomicsStats = new ArrayList<>();
        List<AnnualStatsDisplay> communityStats = new ArrayList<>();
        List<AnnualStatsDisplay> orthStats = new ArrayList<>();
        for (AnnualStats stat : annualStatsList) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(stat.getDate());
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            if (month == 0 && day == 1 && !stat.getType().equals("Full length cDNA clones (ZGC)")) {
                AnnualStatsDisplay annualStatsDisplay = new AnnualStatsDisplay();
                int ct = stat.getCount();
                if (ct == 0) {
                    annualStatsDisplay.setCount("");
                } else {
                    annualStatsDisplay.setCount(ct + "");
                }
                if (stat.getType().equals("Genes")) {
                    annualStatsDisplay.setCategory("Gene Records");
                    annualStatsDisplay.setOrder(0);
                    annualStatsDisplay.setAnnualStats(stat);
                    genesStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Genes on Assembly")) {
                    annualStatsDisplay.setCategory("Genes on Assembly");
                    annualStatsDisplay.setOrder(1);
                    annualStatsDisplay.setAnnualStats(stat);
                    genesStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Transcripts")) {
                    annualStatsDisplay.setCategory("Transcripts");
                    annualStatsDisplay.setOrder(2);
                    annualStatsDisplay.setAnnualStats(stat);
                    genesStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("EST/cDNAs")) {
                    annualStatsDisplay.setCategory("EST/cDNAs");
                    annualStatsDisplay.setOrder(3);
                    annualStatsDisplay.setAnnualStats(stat);
                    genesStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Features")) {
                    annualStatsDisplay.setCategory("Genomic Features");
                    annualStatsDisplay.setOrder(0);
                    annualStatsDisplay.setAnnualStats(stat);
                    geneticsStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Transgenic Features")) {
                    annualStatsDisplay.setCategory("Transgenic Insertions");
                    annualStatsDisplay.setOrder(1);
                    annualStatsDisplay.setAnnualStats(stat);
                    geneticsStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Transgenic Constructs")) {
                    annualStatsDisplay.setCategory("Transgenic Constructs");
                    annualStatsDisplay.setOrder(2);
                    annualStatsDisplay.setAnnualStats(stat);
                    geneticsStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Transgenic Genotypes")) {
                    annualStatsDisplay.setCategory("Transgenic Genotypes");
                    annualStatsDisplay.setOrder(3);
                    annualStatsDisplay.setAnnualStats(stat);
                    geneticsStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Genotypes")) {
                    annualStatsDisplay.setCategory("Genotypes");
                    annualStatsDisplay.setOrder(4);
                    annualStatsDisplay.setAnnualStats(stat);
                    geneticsStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Genes with GO annotations")) {
                    annualStatsDisplay.setCategory("Genes with Any GO annotations");
                    annualStatsDisplay.setOrder(0);
                    annualStatsDisplay.setAnnualStats(stat);
                    faStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Genes with IEA GO annotations")) {
                    annualStatsDisplay.setCategory("Genes with Automated GO Annotation");
                    annualStatsDisplay.setOrder(1);
                    annualStatsDisplay.setAnnualStats(stat);
                    faStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Genes with Non-IEA GO Annotation")) {
                    annualStatsDisplay.setCategory("Genes with Curated GO");
                    annualStatsDisplay.setOrder(2);
                    annualStatsDisplay.setAnnualStats(stat);
                    faStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Total GO Annotations")) {
                    annualStatsDisplay.setCategory("Total GO Annotations");
                    annualStatsDisplay.setOrder(3);
                    annualStatsDisplay.setAnnualStats(stat);
                    faStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Genes with OMIM phenotypes")) {
                    annualStatsDisplay.setCategory("Genes with OMIM phenotypes");
                    annualStatsDisplay.setOrder(4);
                    annualStatsDisplay.setAnnualStats(stat);
                    faStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Morpholinos")) {
                    annualStatsDisplay.setCategory("Morpholinos");
                    annualStatsDisplay.setOrder(0);
                    annualStatsDisplay.setAnnualStats(stat);
                    reagentsStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("TALEN")) {
                    annualStatsDisplay.setCategory("TALEN");
                    annualStatsDisplay.setOrder(1);
                    annualStatsDisplay.setAnnualStats(stat);
                    reagentsStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("CRISPR")) {
                    annualStatsDisplay.setCategory("CRISPR");
                    annualStatsDisplay.setOrder(2);
                    annualStatsDisplay.setAnnualStats(stat);
                    reagentsStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Antibodies")) {
                    annualStatsDisplay.setCategory("Antibodies");
                    annualStatsDisplay.setOrder(3);
                    annualStatsDisplay.setAnnualStats(stat);
                    reagentsStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Gene expression patterns")) {
                    annualStatsDisplay.setCategory("Gene Expression Annotations");
                    annualStatsDisplay.setOrder(0);
                    annualStatsDisplay.setAnnualStats(stat);
                    xpPhenoStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Phenotype statements")) {
                    annualStatsDisplay.setCategory("Phenotype Annotations");
                    annualStatsDisplay.setOrder(1);
                    annualStatsDisplay.setAnnualStats(stat);
                    xpPhenoStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Images")) {
                    annualStatsDisplay.setCategory("Images");
                    annualStatsDisplay.setOrder(2);
                    annualStatsDisplay.setAnnualStats(stat);
                    xpPhenoStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Anatomical structures")) {
                    annualStatsDisplay.setCategory("Anatomical Structures");
                    annualStatsDisplay.setOrder(3);
                    annualStatsDisplay.setAnnualStats(stat);
                    xpPhenoStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Genes with expression data")) {
                    annualStatsDisplay.setCategory("Genes with Expression Data");
                    annualStatsDisplay.setOrder(4);
                    annualStatsDisplay.setAnnualStats(stat);
                    xpPhenoStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Genes with a phenotype")) {
                    annualStatsDisplay.setCategory("Genes with Curated Phenotype");
                    annualStatsDisplay.setOrder(5);
                    annualStatsDisplay.setAnnualStats(stat);
                    xpPhenoStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Mapped markers")) {
                    annualStatsDisplay.setCategory("Mapped Markers");
                    annualStatsDisplay.setOrder(0);
                    annualStatsDisplay.setAnnualStats(stat);
                    genomicsStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Links to other databases")) {
                    annualStatsDisplay.setCategory("Links to Other Databases");
                    annualStatsDisplay.setOrder(1);
                    annualStatsDisplay.setAnnualStats(stat);
                    genomicsStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("All Publications")) {
                    annualStatsDisplay.setCategory("All Publications");
                    annualStatsDisplay.setOrder(0);
                    annualStatsDisplay.setAnnualStats(stat);
                    communityStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Journal Publications")) {
                    annualStatsDisplay.setCategory("Journal Publications");
                    annualStatsDisplay.setOrder(1);
                    annualStatsDisplay.setAnnualStats(stat);
                    communityStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Researchers")) {
                    annualStatsDisplay.setCategory("Researchers");
                    annualStatsDisplay.setOrder(2);
                    annualStatsDisplay.setAnnualStats(stat);
                    communityStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Laboratories")) {
                    annualStatsDisplay.setCategory("Laboratories");
                    annualStatsDisplay.setOrder(3);
                    annualStatsDisplay.setAnnualStats(stat);
                    communityStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Companies")) {
                    annualStatsDisplay.setCategory("Companies");
                    annualStatsDisplay.setOrder(4);
                    annualStatsDisplay.setAnnualStats(stat);
                    communityStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Genes w/Human Orthology")) {
                    annualStatsDisplay.setCategory("Genes with Curated Human Orthology");
                    annualStatsDisplay.setOrder(0);
                    annualStatsDisplay.setAnnualStats(stat);
                    orthStats.add(annualStatsDisplay);
                } else if (stat.getType().equals("Genes w/Mouse Orthology")) {
                    annualStatsDisplay.setCategory("Genes with Curated Mouse Orthology");
                    annualStatsDisplay.setOrder(1);
                    annualStatsDisplay.setAnnualStats(stat);
                    orthStats.add(annualStatsDisplay);
                }
            }
        }

        Collections.sort(genesStats);
        model.addAttribute("genesStats", genesStats);
        model.addAttribute("totalNumGenesStats", genesStats.size());

        Collections.sort(geneticsStats);
        model.addAttribute("geneticsStats", geneticsStats);
        model.addAttribute("totalNumGeneticsStats", geneticsStats.size());

        Collections.sort(faStats);
        model.addAttribute("faStats", faStats);
        model.addAttribute("totalNumFAstats", faStats.size());

        Collections.sort(reagentsStats);
        model.addAttribute("reagentsStats", reagentsStats);
        model.addAttribute("totalNumReagentStats", reagentsStats.size());

        Collections.sort(xpPhenoStats);
        model.addAttribute("xpPhenoStats", xpPhenoStats);
        model.addAttribute("totalNumXpPhenoStats", xpPhenoStats.size());

        Collections.sort(genomicsStats);
        model.addAttribute("genomicsStats", genomicsStats);
        model.addAttribute("totalNumGenomicsStats", genomicsStats.size());

        Collections.sort(communityStats);
        model.addAttribute("communityStats", communityStats);
        model.addAttribute("totalNumCommStats", communityStats.size());

        Collections.sort(orthStats);
        model.addAttribute("orthStats", orthStats);
        model.addAttribute("totalNumOrthStats", orthStats.size());

        return "infrastructure/annual-stats-view.page";
    }
}
