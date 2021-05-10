package org.zfin.infrastructure.presentation;

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
import org.zfin.properties.ZfinPropertiesEnum;


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
                                                            @RequestParam("email") String hiddenEmail,
                                                            @RequestHeader(value = "referer", defaultValue = "<none>") String referer) {
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

}
