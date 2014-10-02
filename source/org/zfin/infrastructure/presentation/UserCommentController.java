package org.zfin.infrastructure.presentation;

import org.apache.log4j.Logger;
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

    private static final Logger log = Logger.getLogger(UserCommentController.class);

    private static final String ADMIN_EMAIL_TEMPLATE = "" +
            "USER INPUT:\n" +
            "\n" +
            "Name: %s\n" +
            "Contact Email: %s\n" +
            "Institution: %s\n" +
            "Originating Page: %s\n" +
            "\n" +
            "Comments: %s\n";

    private static final String SUBMITTER_EMAIL_TEMPLATE = "" +
            "Dear %s:\n" +
            "\n" +
            "Thank you for using ZFIN. Your comments and suggestions are very important to us.\n" +
            "We will respond to them as soon as possible.\n" +
            "\n" +
            "Your comments or suggestions are as follows: \n" +
            "-----------------------------------------------------------------\n" +
            "%s\n" +
            "-----------------------------------------------------------------\n" +
            "\n" +
            "Regards,\n" +
            "Zebrafish Model Organism Database\n";


    @RequestMapping(value = "user-comment", method = RequestMethod.POST)
    public ResponseEntity<JSONStatusResponse> submitComment(@RequestParam("name") String name,
                                                            @RequestParam("institution") String institution,
                                                            @RequestParam("email") String email,
                                                            @RequestParam("subject") String subject,
                                                            @RequestParam("comments") String comments,
                                                            @RequestHeader(value = "referer", defaultValue = "<none>") String referer) {
        MailSender mailer = AbstractZfinMailSender.getInstance();

        // send mail to admin
        mailer.sendMail("Your Input Welcome - " + subject,
                String.format(ADMIN_EMAIL_TEMPLATE, name, email, institution, referer, comments),
                false,
                email,
                ZfinPropertiesEnum.ZFIN_ADMIN.value().split(" "));

        // send email to submitter
        mailer.sendMail("ZFIN Thanks You For Your Input",
                String.format(SUBMITTER_EMAIL_TEMPLATE, name, comments),
                false,
                ZfinPropertiesEnum.CURATORS_AT_ZFIN.value(),
                new String[] {email});

        return new ResponseEntity<>(new JSONStatusResponse("OK", ""), HttpStatus.OK);
    }

}
