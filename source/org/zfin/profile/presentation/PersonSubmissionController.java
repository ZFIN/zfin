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
import org.zfin.profile.PersonSubmission;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.profile.service.ProfileService;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.OrcidUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/profile/person")
@Log4j2
public class PersonSubmissionController {

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    ProfileService profileService;

    @RequiresCaptcha
    @RequestMapping(value = "/submit", method = RequestMethod.GET)
    public String newPersonForm(Model model) {
        populateFormModel(model, new PersonSubmission());
        return "profile/person-submit";
    }

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public String newPersonFormSubmit(@ModelAttribute PersonSubmission submission, Model model, HttpServletRequest request) {
        SubmissionLog logEntry = SubmissionLogService.build(SubmissionType.PERSON, request);

        // The real address is submitted as email2; a value in the decoy "email" field means a bot
        // filled in everything it saw. Keep it out of the submission so that re-rendering the form
        // after a validation error does not populate the decoy for the submitter.
        String submitterEmail = StringUtils.trimToEmpty(submission.getEmail2());
        String submitterName = StringUtils.normalizeSpace(
                StringUtils.trimToEmpty(submission.getFirstName()) + " " + StringUtils.trimToEmpty(submission.getLastName()));
        SubmissionLogService.setSubmitter(logEntry, submitterName, submitterEmail, submission.getOrcid());
        SubmissionLogService.setDetails(logEntry, submission.toText());
        SubmissionLogService.setPersonFields(logEntry, submission.getFirstName(), submission.getLastName(),
                submission.getAddress(), submission.getCountry(), submission.getPhone(), submission.getLab(),
                submission.getRole(), submission.getUrl(), submission.getComments());

        if (StringUtils.isNotEmpty(submission.getEmail())) {
            return rejectSubmission(logEntry, SubmissionOutcome.REJECTED_HONEYPOT, "decoy email field filled in", model);
        }

        Optional<String> captchaRedirectUrl = CaptchaService.getRedirectUrlIfNeeded(request);
        if (captchaRedirectUrl.isPresent()) {
            return rejectSubmission(logEntry, SubmissionOutcome.REJECTED_CAPTCHA, "no valid captcha", model);
        }

        logSubmissionRequest(submission, request);

        SpamAssessment assessment = assessSpam(submission);
        SubmissionLogService.setAssessment(logEntry, assessment);
        if (assessment.isSpam()) {
            return rejectSubmission(logEntry, SubmissionOutcome.REJECTED_SPAM, assessment.describe(), model);
        }

        // A human who left ORCID out, or mistyped it, gets a visible error and their data back.
        List<String> validationErrors = validate(submission);
        if (!validationErrors.isEmpty()) {
            log.info("New Person Submission returned for correction: " + validationErrors);
            SubmissionLogService.setValidationErrors(logEntry, validationErrors);
            SubmissionLogService.save(logEntry, SubmissionOutcome.RETURNED_INVALID);
            populateFormModel(model, submission);
            model.addAttribute("validationErrors", validationErrors);
            return "profile/person-submit";
        }

        submission.setOrcid(OrcidUtil.normalize(submission.getOrcid()));
        submission.setEmail(submitterEmail);
        logEntry.setOrcid(submission.getOrcid());

        // Save before mailing: the coordinator's "create the account" link points at this entry, so
        // it needs an id. If the mail then fails, the outcome is corrected below.
        SubmissionLogService.save(logEntry, SubmissionOutcome.ACCEPTED);

        //send confirmation email
        boolean error = !sendPersonConfirmationEmails(submission, logEntry.getId());
        model.addAttribute("error", error);
        if (error) {
            SubmissionLogService.save(logEntry, SubmissionOutcome.ERROR_SENDING);
        }

        return "profile/person-submit-process";
    }

    private void populateFormModel(Model model, PersonSubmission submission) {
        model.addAttribute("roleOptions", profileRepository.getLabPositions());
        model.addAttribute("submission", submission);
        model.addAttribute("countryList", profileService.getCountries());
    }

    /**
     * Drops the submission and records why. Scored and captcha rejections tell the submitter the
     * request was not submitted: that thank you was indistinguishable from success, so a human
     * caught by either walked away believing a request had been filed that nobody would ever see.
     * A filled in honeypot has no such human behind it, so it still gets the thank you page and
     * the bot learns nothing.
     */
    private String rejectSubmission(SubmissionLog logEntry, SubmissionOutcome outcome, String reason, Model model) {
        log.error("New Person Submission Flagged as Spam (" + outcome + "): " + reason
                + "\n" + logEntry.getDetails());
        SubmissionLogService.save(logEntry, outcome);
        if (outcome != SubmissionOutcome.REJECTED_HONEYPOT) {
            model.addAttribute("submissionRejected", true);
        }
        return "profile/person-submit-process";
    }

    /**
     * Scores every free text field, not just the name: bots fill the whole form with random
     * strings, and the giveaway is as likely to be in the lab or comments field as in the name.
     */
    private SpamAssessment assessSpam(PersonSubmission submission) {
        return SpamDetector.examine()
                .name("firstName", submission.getFirstName())
                .name("lastName", submission.getLastName())
                .text("address", submission.getAddress())
                .name("lab", submission.getLab())
                .url("url", submission.getUrl())
                .orcid("orcid", submission.getOrcid())
                .freeText("comments", submission.getComments())
                .assess();
    }

    /**
     * Server side validation, so the requirements hold even if the browser checks are bypassed.
     */
    private List<String> validate(PersonSubmission submission) {
        List<String> errors = new ArrayList<>();
        if (StringUtils.isBlank(submission.getFirstName())) {
            errors.add("First name is required.");
        }
        if (StringUtils.isBlank(submission.getLastName())) {
            errors.add("Last name is required.");
        }
        if (StringUtils.isBlank(submission.getEmail2())) {
            errors.add("Email is required.");
        }
        if (OrcidUtil.isBlankOrPlaceholder(submission.getOrcid())) {
            errors.add("An ORCID iD is required. If you do not have one yet, you can register for " +
                    "a free ORCID iD at https://orcid.org/register.");
        } else if (!OrcidUtil.isValid(submission.getOrcid())) {
            errors.add("ORCID iD must be 16 digits in the form 0000-0002-1825-0097.");
        }
        return errors;
    }

    private void logSubmissionRequest(PersonSubmission submission, HttpServletRequest request) {
        log.error("New Person Submission: " + submission.toText());
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

    private boolean sendPersonConfirmationEmails(PersonSubmission submission, Long submissionId) {
        String submitterEmail = submission.getEmail();
        String coordinatorEmail = ZfinPropertiesEnum.ZFIN_ADMIN.value();

        boolean success = false;
        success = AbstractZfinMailSender.getInstance().sendHtmlMail(
                this.getAdminSubjectLine(submission),
                this.getAdminBody(submission, submissionId),
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

    private String getConfirmationBody(PersonSubmission submission) {
        return "This is a confirmation that we have received your request to create a ZFIN account.\n\n" +
                "Details:\n\n" +
                "First Name: " + submission.getFirstName() + "\n" +
                "Last Name: " + submission.getLastName() + "\n" +
                "Email: " + submission.getEmail() + "\n" +
                "Address: " + submission.getAddress() + "\n" +
                "Country: " + profileService.getCountryDisplayName(submission.getCountry()) + "\n" +
                "Phone: " + submission.getPhone() + "\n" +
                "Lab: " + submission.getLab() + "\n" +
                "URL: " + submission.getUrl() + "\n" +
                "ORCID: " + submission.getOrcid() + "\n" +
                "Comments: " + submission.getComments() + "\n\n" +
                "Thank you for your request.  We will notify you as soon as your account is created.\n\n";
    }

    private String getConfirmationSubjectLine(PersonSubmission submission) {
        return "ZFIN: Account Request Confirmation";
    }

    private String getAdminBody(PersonSubmission submission, Long submissionId) {
        String body = "We have received a new request to create an account.\n\nDetails:\n\n" +
                "First Name: " + submission.getFirstName() + "\n" +
                "Last Name: " + submission.getLastName() + "\n" +
                "Email: " + submission.getEmail() + "\n" +
                "Address: " + submission.getAddress() + "\n" +
                "Country: " + profileService.getCountryDisplayName(submission.getCountry()) + "\n" +
                "Phone: " + submission.getPhone() + "\n" +
                "Lab: " + submission.getLab() + "\n" +
                "URL: " + submission.getUrl() + "\n" +
                "ORCID: " + submission.getOrcid() + "\n" +
                "Comments: " + submission.getComments() + "\n\n\n" +
                "Please <a href=\"" + getAdminLink(submission, submissionId) +"\">create the account</a> and notify the submitter.\n\n";

        return body.replaceAll("\\n", "<br/>\n");
    }

    /**
     * Link the coordinator follows to create the account. Where the submission was recorded, point
     * at that record so the whole request prefills the form; otherwise fall back to passing the few
     * fields that fit in a query string.
     */
    private String getAdminLink(PersonSubmission submission, Long submissionId) {
        String domainName = ZfinPropertiesEnum.DOMAIN_NAME.value();
        String baseUrl = "https://" + domainName + "/action/profile/person/create";

        if (submissionId != null) {
            return baseUrl + "?prefill_from_submission=" + submissionId;
        }

        String firstName = URLEncoder.encode(StringUtils.trimToEmpty(submission.getFirstName()), StandardCharsets.UTF_8);
        String lastName = URLEncoder.encode(StringUtils.trimToEmpty(submission.getLastName()), StandardCharsets.UTF_8);
        String email = URLEncoder.encode(StringUtils.trimToEmpty(submission.getEmail()), StandardCharsets.UTF_8);
        return  baseUrl +
                "?firstName=" + firstName +
                "&lastName=" + lastName +
                "&email=" + email;

    }

    private String getAdminSubjectLine(PersonSubmission submission) {
        return "New Person Submission: " + submission.getFirstName() + " " + submission.getLastName();
    }

}
