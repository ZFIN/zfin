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
import org.zfin.profile.OrganizationPosition;
import org.zfin.profile.PersonSubmission;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.profile.service.ProfileService;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @RequestMapping(value = "/submit", method = RequestMethod.GET)
    public String newPersonForm(Model model, HttpServletRequest request) {
        //TODO: This would read better if it was an annotation on the method (eg. `@RequiresCaptcha`)
        Optional<String> captchaRedirectUrl = CaptchaService.getRedirectUrlIfNeeded(request);
        if (captchaRedirectUrl.isPresent()) {
            return "redirect:" + captchaRedirectUrl.get();
        }

        List<OrganizationPosition> roleOptions = profileRepository.getLabPositions();
        model.addAttribute("roleOptions", roleOptions);

        PersonSubmission submission = new PersonSubmission();
        model.addAttribute("submission", submission);
        model.addAttribute("countryList", profileService.getCountries());

        return "profile/person-submit";
    }

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public String newPersonFormSubmit(@ModelAttribute PersonSubmission submission, Model model, HttpServletRequest request) {
        if (StringUtils.isNotEmpty(submission.getEmail())) {
            log.error("New Person Submission Flagged as Spam: " + submission);
            return "profile/person-submit-process";
        }
        Optional<String> captchaRedirectUrl = CaptchaService.getRedirectUrlIfNeeded(request);
        if (captchaRedirectUrl.isPresent()) {
            log.error("New Person Submission Flagged as Spam: " + submission);
            return "profile/person-submit-process";
        }
        logSubmissionRequest(submission, request);
        if (flagSpam(submission)) {
            log.error("New Person Submission Flagged as Spam: " + submission);
            return "profile/person-submit-process";
        }

        submission.setEmail(submission.getEmail2());

        //send confirmation email
        boolean error = !sendPersonConfirmationEmails(submission);
        model.addAttribute("error", error);

        return "profile/person-submit-process";
    }

    private boolean flagSpam(PersonSubmission submission) {
        //Look for submissions like: New Person Submission: WsEjiJCJYOXBXrWPWNLAwq KyliyDPBwGnfnAiVOoKCl
        String firstName = submission.getFirstName();
        String lastName = submission.getLastName();

        boolean entirelyUpperCaseCheck = StringUtils.isAllUpperCase(firstName) && StringUtils.isAllUpperCase(lastName);
        if (entirelyUpperCaseCheck) {
            //some legitimate names are all uppercase
            return false;
        }

        int countOfLettersThreshold = 10;

        int upperCaseThreshold = 3;
        if (numberOfUpperCaseLetters(firstName) >= upperCaseThreshold && firstName.length() >= countOfLettersThreshold) {
            return true;
        }
        if (numberOfUpperCaseLetters(lastName) >= upperCaseThreshold && lastName.length() >= countOfLettersThreshold) {
            return true;
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

    private void logSubmissionRequest(PersonSubmission submission, HttpServletRequest request) {
        log.error("New Person Submission: " + submission.toText());
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

    private boolean sendPersonConfirmationEmails(PersonSubmission submission) {
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

    private String getAdminBody(PersonSubmission submission) {
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
                "Please <a href=\"" + getAdminLink(submission) +"\">create the account</a> and notify the submitter.\n\n";

        return body.replaceAll("\\n", "<br/>\n");
    }

    private String getAdminLink(PersonSubmission submission) {
        String domainName = ZfinPropertiesEnum.DOMAIN_NAME.value();
        String baseUrl = "https://" + domainName + "/action/profile/person/create";

        String firstName = URLEncoder.encode(submission.getFirstName(), StandardCharsets.UTF_8);
        String lastName = URLEncoder.encode(submission.getLastName(), StandardCharsets.UTF_8);
        String email = URLEncoder.encode(submission.getEmail(), StandardCharsets.UTF_8);
        return  baseUrl +
                "?firstName=" + firstName +
                "&lastName=" + lastName +
                "&email=" + email;

    }

    private String getAdminSubjectLine(PersonSubmission submission) {
        return "New Person Submission: " + submission.getFirstName() + " " + submission.getLastName();
    }

}
