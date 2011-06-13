package org.zfin.ontology.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller that serves meta information about the ontologies.
 */
@Controller
public class TermUsageReportController {

    private static final Logger log = Logger.getLogger(TermUsageReportController.class);

    @RequestMapping("/term-usage-report")
    private String createTermUsageReport(Model model) {
        OntologyBean form = new OntologyBean();
        model.addAttribute("formBean", form);
        Map<TermHistogramBean, Long> phenotypeUsage = RepositoryFactory.getMutantRepository().getTermPhenotypeUsage();
            model.addAttribute("phenotypeUsage", phenotypeUsage);
        return "ontology/term-usage-report.page";
    }


}