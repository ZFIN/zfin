package org.zfin.zirc.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/zirc")
public class ZircSubmissionController {

    @RequestMapping("/submit")
    public String showSubmissionForm() {
        return "zirc/submission";
    }

}
