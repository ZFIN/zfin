package org.zfin.nomenclature.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.mail.AbstractZfinMailSender;
import org.zfin.framework.mail.MailSender;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.nomenclature.*;
import org.zfin.properties.ZfinPropertiesEnum;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping("/nomenclature")
public class NomenclatureSubmissionController {

    @RequestMapping(value = "/gene-name", method = RequestMethod.GET)
    public String newGeneNameForm(Model model) {
        GeneNameSubmission submission = new GeneNameSubmission();
        submission.setHomologyInfoList(Arrays.asList(new HomologyInfo()));
        model.addAttribute("submission", submission);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Submit a Proposed Gene Name");
        return "nomenclature/gene-name-form.page";
    }

    @RequestMapping(value = "/gene-name", method = RequestMethod.POST)
    public String newGeneNameSubmit(@ModelAttribute GeneNameSubmission submission, Model model) {
        model.addAttribute("submission", submission);

        // get rid of any blank rows from homology table
        removeEmptyRows(submission.getHomologyInfoList());

        // send email to nomenclature coordinator if decoy email field is empty
        boolean sent = false;
        if (StringUtils.isEmpty(submission.getEmail())) {
            MailSender mailer = AbstractZfinMailSender.getInstance();
            sent = mailer.sendMail("Gene Submission: " + submission.getGeneSymbol(),
                    submission.toString(),
                    false,
                    submission.getEmail2(),
                    ZfinPropertiesEnum.NOMEN_COORDINATOR.value().split(" "));
        }
        model.addAttribute("sent", sent);

        return "nomenclature/gene-name-submit.page";
    }

    @RequestMapping(value = "/line-name", method = RequestMethod.GET)
    public String newLineNameForm(Model model) {
        LineNameSubmission submission = new LineNameSubmission();
        submission.setLineDetails(Arrays.asList(new LineInfo()));
        model.addAttribute("submission", submission);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Submit a Proposed Mutant/Transgenic Line Name");
        return "nomenclature/line-name-form.page";
    }

    @RequestMapping(value = "/line-name", method = RequestMethod.POST)
    public String newLineNameSubmit(@ModelAttribute LineNameSubmission submission, Model model) {
        model.addAttribute("submission", submission);

        // get rid of any blank rows from line information table
        removeEmptyRows(submission.getLineDetails());

        // send email to nomenclature coordinator if decoy email field is empty
        boolean sent = false;
        if (StringUtils.isEmpty(submission.getEmail())) {
            MailSender mailer = AbstractZfinMailSender.getInstance();
            List<LineInfo> details = submission.getLineDetails();
            sent = mailer.sendMail(
                    "Mutant Submission: " + (details.size() > 0 ? details.get(0).getDesignation() : ""),
                    submission.toString(),
                    false,
                    submission.getEmail2(),
                    ZfinPropertiesEnum.NOMEN_COORDINATOR.value().split(" "));
        }
        model.addAttribute("sent", sent);

        return "nomenclature/line-name-submit.page";
    }

    @ModelAttribute("pubStatusOptions")
    public List<String> getPubStatusOptions() {
        return Arrays.asList("Published", "In Press", "Submitted", "In Preparation", "Unpublished");
    }

    @ModelAttribute("reserveTypeOptions")
    public List<String> getReserveTypeOptions() {
        return Arrays.asList("in my name", "as an anonymous submission");
    }

    private static void removeEmptyRows(Collection<? extends EmptyTestable> collection) {
        CollectionUtils.filter(collection, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                return (o instanceof EmptyTestable) && !((EmptyTestable) o).isEmpty();
            }
        });
    }

}
