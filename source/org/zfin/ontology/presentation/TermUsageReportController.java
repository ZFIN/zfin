package org.zfin.ontology.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.repository.RepositoryFactory;

import java.util.Map;

/**
 * Controller that serves meta information about the ontologies.
 */
@Controller
@RequestMapping("/ontology")
public class TermUsageReportController {

    @RequestMapping("/term-usage-report")
    private String createTermUsageReport(Model model) {
        OntologyBean form = new OntologyBean();
        model.addAttribute("formBean", form);
        Map<TermHistogramBean, Long> phenotypeUsage = RepositoryFactory.getMutantRepository().getTermPhenotypeUsage();
            model.addAttribute("phenotypeUsage", phenotypeUsage);
        return "ontology/term-usage-report.page";
    }


}