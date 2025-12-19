package org.zfin.infrastructure.presentation;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.mail.AbstractZfinMailSender;
import org.zfin.framework.mail.MailSender;
import org.zfin.profile.OrganizationSubmission;
import org.zfin.profile.PersonSubmission;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.IOException;

import static org.zfin.infrastructure.captcha.CaptchaService.isSuccessfulCaptchaToken;
import static org.zfin.infrastructure.captcha.CaptchaService.verifyCaptcha;


@Controller
public class UserCommentController {

    private static final Logger log = LogManager.getLogger(UserCommentController.class);

    private static final String ADMIN_EMAIL_TEMPLATE = "" +
            "USER INPUT:\n" +
            "\n" +
            "Name: %s\n" +
            "Contact Email: %s\n" +
            "Institution: %s\n" +
            "Originating Page: %s\n" +
            "\n" +
            "Comments: %s\n";

    @RequestMapping(value = "user-comment", method = RequestMethod.POST)
    public ResponseEntity<JSONStatusResponse> submitComment(@RequestParam("yiw-name") String name,
                                                            @RequestParam("yiw-institution") String institution,
                                                            @RequestParam("yiw-email") String email,
                                                            @RequestParam("yiw-subject") String subject,
                                                            @RequestParam("yiw-comments") String comments,
                                                            @RequestParam("altcha") String altcha,
                                                            @RequestParam("email") String hiddenEmail,
                                                            @RequestHeader(value = "referer", defaultValue = "<none>") String referer,
                                                            HttpServletRequest request
                                                            ) {
        MailSender mailer = AbstractZfinMailSender.getInstance();

        // none of the regular fields should be blank. client-side validation should have prevented that. if any of them
        // are blank or the *hidden* email input is not blank then this was probably a spammy request, so just stop
        // here.
        if (StringUtils.isEmpty(name) ||
                StringUtils.isEmpty(institution) ||
                StringUtils.isEmpty(email) ||
                StringUtils.isEmpty(subject) ||
                StringUtils.isEmpty(comments) ||
                !StringUtils.isEmpty(hiddenEmail)) {
            return new ResponseEntity<>(new JSONStatusResponse("Error", "Invalid field"), HttpStatus.BAD_REQUEST);
        }

        boolean isCaptchaValid = true; //default to true if exception occurs
        try {
            boolean alreadyCaptchaValidated = isSuccessfulCaptchaToken(request);
            isCaptchaValid = (!StringUtils.isEmpty(altcha) && verifyCaptcha(altcha)) || alreadyCaptchaValidated;
        } catch (IOException e) {}

        if (!isCaptchaValid) {
            return new ResponseEntity<>(new JSONStatusResponse("Error", "Invalid Captcha Response"), HttpStatus.BAD_REQUEST);
        }

        logSubmissionRequest(request, name, institution, email, subject);
        if (flagSpam(name, institution)) {
            log.error("New Person Submission Flagged as Spam: ");
            return new ResponseEntity<>(new JSONStatusResponse("Error", "Invalid Form Data"), HttpStatus.BAD_REQUEST);
        }


        // send mail to admin
        boolean sent = mailer.sendMail(subject,
                String.format(ADMIN_EMAIL_TEMPLATE, name, email, institution, referer, comments),
                false,
                email,
                ZfinPropertiesEnum.JSD_EMAIL.value().split(" "));
        if (sent) {
            return new ResponseEntity<>(new JSONStatusResponse("OK", ""), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new JSONStatusResponse("Error", "Internal error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private boolean flagSpam(String name, String institution) {
        //Look for submissions like: WsEjiJCJYOXBXrWPWNLAwq KyliyDPBwGnfnAiVOoKCl
        boolean entirelyUpperCaseCheck = StringUtils.isAllUpperCase(name) && StringUtils.isAllUpperCase(institution);
        if (entirelyUpperCaseCheck) {
            //some legitimate names are all uppercase
            return false;
        }

        int countOfLettersThreshold = 10;

        int upperCaseThreshold = 3;
        if (numberOfUpperCaseLetters(name) >= upperCaseThreshold && name.length() >= countOfLettersThreshold) {
            if (StringUtils.countMatches(name, " ") == 0) {
                return true;
            }
        }
        if (numberOfUpperCaseLetters(institution) >= upperCaseThreshold && institution.length() >= countOfLettersThreshold) {
            if (StringUtils.countMatches(institution, " ") == 0) {
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

    private void logSubmissionRequest(HttpServletRequest request, String name, String institution, String email, String subject) {
        log.error("New Feedback Submission: name: %s, institution: %s, email: %s, subject: %s".formatted(name, institution, email, subject));
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


}
