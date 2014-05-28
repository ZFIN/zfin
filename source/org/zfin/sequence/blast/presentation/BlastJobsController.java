package org.zfin.sequence.blast.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.ontology.presentation.OntologyBean;
import org.zfin.sequence.blast.BlastQueryThreadCollection;
import org.zfin.sequence.blast.BlastStatistics;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class BlastJobsController {


    @RequestMapping("/blast-jobs")
    protected String showBlastJobs(@ModelAttribute("formBean") BlastJobsBean blastJobsBean,
                                   Model model) throws Exception {
        blastJobsBean.setBlastThreadCollection(BlastQueryThreadCollection.getInstance());
        blastJobsBean.setBlastStatistics(BlastStatistics.getInstance());

        return "dev-tools/blast-jobs.page";
    }
}
