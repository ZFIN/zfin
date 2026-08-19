package org.zfin.profile.presentation;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.mail.AbstractZfinMailSender;
import org.zfin.infrastructure.captcha.CaptchaService;
import org.zfin.infrastructure.captcha.RequiresCaptcha;
import org.zfin.infrastructure.spam.SpamAssessment;
import org.zfin.infrastructure.spam.SpamDetector;
import org.zfin.infrastructure.submission.SubmissionLog;
import org.zfin.infrastructure.submission.SubmissionLogService;
import org.zfin.infrastructure.submission.SubmissionOutcome;
import org.zfin.infrastructure.submission.SubmissionType;
import org.zfin.profile.OrganizationSubmission;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.properties.ZfinPropertiesEnum;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static java.net.URLEncoder.encode;

@Controller
@RequestMapping("/profile/organization")
@Log4j2
public class OrganizationSubmissionController {

    @Autowired
    ProfileRepository profileRepository;

    @RequiresCaptcha
    @RequestMapping(value = "/submit", method = RequestMethod.GET)
    public String newPersonForm(Model model) {
        OrganizationSubmission submission = new OrganizationSubmission();
        model.addAttribute("submission", submission);

        return "profile/organization-submit";
    }

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public String newOrganizationFormSubmit(@ModelAttribute OrganizationSubmission submission, Model model, HttpServletRequest request) {
        SubmissionLog logEntry = SubmissionLogService.build(SubmissionType.ORGANIZATION, request);
        SubmissionLogService.setSubmitter(logEntry, submission.getContactPerson(),
                StringUtils.trimToEmpty(submission.getEmail2()), null);
        SubmissionLogService.setDetails(logEntry, submission.toText());

        if (StringUtils.isNotEmpty(submission.getEmail())) {
            return discardSilently(logEntry, SubmissionOutcome.REJECTED_HONEYPOT, "decoy email field filled in");
        }
        Optional<String> captchaRedirectUrl = CaptchaService.getRedirectUrlIfNeeded(request);
        if (captchaRedirectUrl.isPresent()) {
            return discardSilently(logEntry, SubmissionOutcome.REJECTED_CAPTCHA, "no valid captcha");
        }
        logSubmissionRequest(submission, request);

        SpamAssessment assessment = assessSpam(submission);
        SubmissionLogService.setAssessment(logEntry, assessment);
        if (assessment.isSpam()) {
            return discardSilently(logEntry, SubmissionOutcome.REJECTED_SPAM, assessment.describe());
        }
        submission.setEmail(submission.getEmail2());

        //send confirmation email
        boolean error = !sendConfirmationEmails(submission);
        model.addAttribute("error", error);
        SubmissionLogService.save(logEntry,
                error ? SubmissionOutcome.ERROR_SENDING : SubmissionOutcome.ACCEPTED);

        return "profile/organization-submit-process";
    }

    /**
     * Renders the same "thank you" page a real submitter sees, so a bot learns nothing, and records
     * why we dropped it.
     */
    private String discardSilently(SubmissionLog logEntry, SubmissionOutcome outcome, String reason) {
        log.error("New Organization Submission Flagged as Spam (" + outcome + "): " + reason
                + "\n" + logEntry.getDetails());
        SubmissionLogService.save(logEntry, outcome);
        return "profile/organization-submit-process";
    }

    private SpamAssessment assessSpam(OrganizationSubmission submission) {
        return SpamDetector.examine()
                .name("name", submission.getName())
                .name("contactPerson", submission.getContactPerson())
                .text("address", submission.getAddress())
                .url("url", submission.getUrl())
                .freeText("comments", submission.getComments())
                .assess();
    }

    private void logSubmissionRequest(OrganizationSubmission submission, HttpServletRequest request) {
        log.error("New Organization Submission: " + submission.toText());
        log.error("Submission IP Address: " + SubmissionLogService.getClientIpAddress(request));

        //get cookies:
        StringBuilder cookies = new StringBuilder();
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                cookies.append(cookie.getName()).append("=").append(cookie.getValue()).append(";\n");
            }
        }
        log.error("Submission Cookies: " + cookies.toString());
    }

    private boolean sendConfirmationEmails(OrganizationSubmission submission) {
        String submitterEmail = submission.getEmail();
        String coordinatorEmail = ZfinPropertiesEnum.ZFIN_ADMIN.value();

        boolean success = false;
        success = AbstractZfinMailSender.getInstance().sendHtmlMail(
                this.getAdminSubjectLine(submission),
                this.getAdminBody(submission),
                false,
                submitterEmail,
                new String[]{coordinatorEmail}
        );

        success = success && AbstractZfinMailSender.getInstance().sendMail(
                this.getConfirmationSubjectLine(submission),
                this.getConfirmationBody(submission),
                false,
                coordinatorEmail,
                new String[]{submitterEmail}
        );

        return success;
    }

    private String getConfirmationBody(OrganizationSubmission submission) {

        return "This is a confirmation that we have received your request to create a ZFIN account.\n\n" +
                "Details:\n\n" +
                "Name: " + submission.getName() + "\n" +
                "Type: " + submission.getType() + "\n" +
                "Contact Name: " + submission.getContactPerson() + "\n" +
                "Contact Email: " + submission.getEmail() + "\n" +
                "Phone: " + submission.getPhone() + "\n" +
                "Fax: " + submission.getFax() + "\n" +
                "URL: " + submission.getUrl() + "\n" +
                "Comments: " + submission.getComments() + "\n\n" +
                "Thank you for your request.  We will notify you as soon as your account is created.\n\n";
    }

    private String getConfirmationSubjectLine(OrganizationSubmission submission) {
        return "ZFIN: Organization Request Confirmation";
    }

    private String getAdminBody(OrganizationSubmission submission) {
        String body = "We have received a new request to create an organization.\n\nDetails:\n\n" +
                "Name: " + submission.getName() + "\n" +
                "Type: " + submission.getType() + "\n" +
                "Contact Name: " + submission.getContactPerson() + "\n" +
                "Contact Email: " + submission.getEmail() + "\n" +
                "Phone: " + submission.getPhone() + "\n" +
                "Fax: " + submission.getFax() + "\n" +
                "URL: " + submission.getUrl() + "\n" +
                "Comments: " + submission.getComments() + "\n\n\n" +
                "Please <a href=\"" + getAdminLink(submission) +"\">create the account</a> and notify the submitter.\n\n";

        return body.replaceAll("\\n", "<br/>\n");
    }

    private String getAdminLink(OrganizationSubmission submission) {
        String domainName = ZfinPropertiesEnum.DOMAIN_NAME.value();
        String organizationType = "Company".equals(submission.getType()) ? "company" : "lab";
        String baseUrl = "https://" + domainName + "/action/profile/" + organizationType + "/create";

        return  baseUrl +
                "?name=" + encode(submission.getName(), StandardCharsets.UTF_8) +
                "&email=" + encode(submission.getEmail(), StandardCharsets.UTF_8) +
                "&contactName=" + encode(submission.getContactPerson(), StandardCharsets.UTF_8) +
                "&phone=" + encode(submission.getPhone(), StandardCharsets.UTF_8) +
                "&fax=" + encode(submission.getFax(), StandardCharsets.UTF_8) +
                "&url=" + encode(submission.getUrl(), StandardCharsets.UTF_8);

    }

    private String getAdminSubjectLine(OrganizationSubmission submission) {
        return "New Organization Submission: " + submission.getName();
    }

}
