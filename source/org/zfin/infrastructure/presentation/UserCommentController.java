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
import org.zfin.infrastructure.spam.SpamAssessment;
import org.zfin.infrastructure.spam.SpamDetector;
import org.zfin.infrastructure.submission.SubmissionLog;
import org.zfin.infrastructure.submission.SubmissionLogService;
import org.zfin.infrastructure.submission.SubmissionOutcome;
import org.zfin.infrastructure.submission.SubmissionType;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.IOException;

import static org.zfin.infrastructure.captcha.CaptchaService.isCaptchaRequired;
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
                                                            // altcha is empty/missing when this visitor doesn't need to solve a
                                                            // captcha (logged-in, bypassed IP, or already verified). The
                                                            // isCaptchaRequired branch below skips verification entirely in
                                                            // that case; the StringUtils.isEmpty(altcha) guard catches the
                                                            // session-expired path. Required-by-default would 400 the request
                                                            // before either of those checks ran.
                                                            @RequestParam(value = "altcha", required = false, defaultValue = "") String altcha,
                                                            @RequestParam("email") String hiddenEmail,
                                                            @RequestHeader(value = "referer", defaultValue = "<none>") String referer,
                                                            HttpServletRequest request
                                                            ) {
        MailSender mailer = AbstractZfinMailSender.getInstance();

        SubmissionLog logEntry = SubmissionLogService.build(SubmissionType.USER_COMMENT, request);
        SubmissionLogService.setSubmitter(logEntry, name, email, null);
        SubmissionLogService.setDetails(logEntry,
                String.format(ADMIN_EMAIL_TEMPLATE, name, email, institution, referer, comments));

        // none of the regular fields should be blank. client-side validation should have prevented that. if any of them
        // are blank or the *hidden* email input is not blank then this was probably a spammy request, so just stop
        // here.
        if (StringUtils.isEmpty(name) ||
                StringUtils.isEmpty(institution) ||
                StringUtils.isEmpty(email) ||
                StringUtils.isEmpty(subject) ||
                StringUtils.isEmpty(comments) ||
                !StringUtils.isEmpty(hiddenEmail)) {
            SubmissionLogService.save(logEntry, StringUtils.isEmpty(hiddenEmail)
                    ? SubmissionOutcome.RETURNED_INVALID
                    : SubmissionOutcome.REJECTED_HONEYPOT);
            return new ResponseEntity<>(new JSONStatusResponse("Error", "Invalid field"), HttpStatus.BAD_REQUEST);
        }

        // Only enforce captcha when this request actually requires it. Logged-in users, bypassed
        // IPs, and sessions that already passed captcha are exempt (see CaptchaService). If the
        // session expired since the form was opened, captcha may have become required - in that
        // case we return a CaptchaRequired status so the client can surface the widget and let the
        // user resend without losing their comment.
        if (isCaptchaRequired(request)) {
            boolean isCaptchaValid = true; //default to true if verification errors, to avoid blocking legitimate input
            try {
                isCaptchaValid = !StringUtils.isEmpty(altcha) && verifyCaptcha(altcha);
            } catch (IOException e) {
                log.error("Error verifying captcha for user comment submission", e);
            }
            if (!isCaptchaValid) {
                SubmissionLogService.save(logEntry, SubmissionOutcome.REJECTED_CAPTCHA);
                return new ResponseEntity<>(new JSONStatusResponse("CaptchaRequired", "Captcha verification required"), HttpStatus.BAD_REQUEST);
            }
        }

        logSubmissionRequest(request, name, institution, email, subject);

        SpamAssessment assessment = SpamDetector.examine()
                .name("name", name)
                .name("institution", institution)
                .text("subject", subject)
                .freeText("comments", comments)
                .assess();
        SubmissionLogService.setAssessment(logEntry, assessment);
        if (assessment.isSpam()) {
            log.error("User Comment Flagged as Spam: " + assessment.describe());
            SubmissionLogService.save(logEntry, SubmissionOutcome.REJECTED_SPAM);
            return new ResponseEntity<>(new JSONStatusResponse("Error", "Invalid Form Data"), HttpStatus.BAD_REQUEST);
        }

        // send mail to admin
        boolean sent = mailer.sendMail(subject,
                String.format(ADMIN_EMAIL_TEMPLATE, name, email, institution, referer, comments),
                false,
                email,
                ZfinPropertiesEnum.JSD_EMAIL.value().split(" "));
        if (sent) {
            SubmissionLogService.save(logEntry, SubmissionOutcome.ACCEPTED);
            return new ResponseEntity<>(new JSONStatusResponse("OK", ""), HttpStatus.OK);
        } else {
            SubmissionLogService.save(logEntry, SubmissionOutcome.ERROR_SENDING);
            return new ResponseEntity<>(new JSONStatusResponse("Error", "Internal error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void logSubmissionRequest(HttpServletRequest request, String name, String institution, String email, String subject) {
        log.error("New Feedback Submission: name: %s, institution: %s, email: %s, subject: %s".formatted(name, institution, email, subject));
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


}
