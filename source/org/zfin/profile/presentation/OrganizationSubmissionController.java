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
import org.zfin.profile.OrganizationSubmission;
import org.zfin.profile.PersonSubmission;
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

    @RequestMapping(value = "/submit", method = RequestMethod.GET)
    public String newPersonForm(Model model, HttpServletRequest request) {
        //TODO: This would read better if it was an annotation on the method (eg. `@RequiresCaptcha`)
        Optional<String> captchaRedirectUrl = CaptchaService.getRedirectUrlIfNeeded(request);
        if (captchaRedirectUrl.isPresent()) {
            return "redirect:" + captchaRedirectUrl.get();
        }

        OrganizationSubmission submission = new OrganizationSubmission();
        model.addAttribute("submission", submission);

        return "profile/organization-submit";
    }

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public String newOrganizationFormSubmit(@ModelAttribute OrganizationSubmission submission, Model model, HttpServletRequest request) {
        if (StringUtils.isNotEmpty(submission.getEmail())) {
            log.error("New Organization Submission Flagged as Spam: " + submission);
            return "profile/organization-submit-process";
        }
        Optional<String> captchaRedirectUrl = CaptchaService.getRedirectUrlIfNeeded(request);
        if (captchaRedirectUrl.isPresent()) {
            log.error("New Organization Submission Flagged as Spam: " + submission);
            return "profile/organization-submit-process";
        }
        logSubmissionRequest(submission, request);
        if (flagSpam(submission)) {
            log.error("New Person Submission Flagged as Spam: " + submission);
            return "profile/person-submit-process";
        }
        submission.setEmail(submission.getEmail2());

        //send confirmation email
        boolean error = !sendConfirmationEmails(submission);
        model.addAttribute("error", error);

        return "profile/organization-submit-process";
    }

    private void logSubmissionRequest(OrganizationSubmission submission, HttpServletRequest request) {
        log.error("New Organization Submission: " + submission.toText());
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        log.error("Submission IP Address: " + ipAddress);

        //get cookies:
        StringBuilder cookies = new StringBuilder();
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                cookies.append(cookie.getName()).append("=").append(cookie.getValue()).append(";\n");
            }
        }
        log.error("Submission Cookies: " + cookies.toString());
    }

    private boolean flagSpam(OrganizationSubmission submission) {
        //Look for submissions like: New Person Submission: WsEjiJCJYOXBXrWPWNLAwq KyliyDPBwGnfnAiVOoKCl
        String contactPerson = submission.getContactPerson();
        int countOfLettersThreshold = 10;

        int upperCaseThreshold = 3;
        if (numberOfUpperCaseLetters(contactPerson) >= upperCaseThreshold && contactPerson.length() >= countOfLettersThreshold) {
            //count the number of spaces
            if (StringUtils.countMatches(contactPerson, " ") == 0) {
                return true;
            }
        }
        return false;
    }

    private int numberOfUpperCaseLetters(String firstName) {
        int count = 0;
        for (char c : firstName.toCharArray()) {
            if (Character.isUpperCase(c)) {
                count++;
            }
        }
        return count;
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
