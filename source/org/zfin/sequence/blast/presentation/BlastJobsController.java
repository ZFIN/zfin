package org.zfin.sequence.blast.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.sequence.blast.BlastQueryThreadCollection;
import org.zfin.sequence.blast.BlastStatistics;

@Controller
@RequestMapping(value = "/devtool")
public class BlastJobsController {


    @RequestMapping("/blast-jobs")
    protected String showBlastJobs(@ModelAttribute("formBean") BlastJobsBean blastJobsBean,
                                   Model model) throws Exception {
        blastJobsBean.setBlastThreadCollection(BlastQueryThreadCollection.getInstance());
        blastJobsBean.setBlastStatistics(BlastStatistics.getInstance());

        return "dev-tools/blast-jobs";
    }
}
