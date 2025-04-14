package org.zfin.infrastructure.presentation;

import lombok.extern.log4j.Log4j2;
import org.altcha.altcha.Altcha;
import org.altcha.altcha.Altcha.Algorithm;
import org.altcha.altcha.Altcha.Challenge;
import org.altcha.altcha.Altcha.ChallengeOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.infrastructure.captcha.CaptchaKeys;

@Log4j2
@RestController
@RequestMapping("/altcha")
public class AltchaApiController {

    @GetMapping("/challenge")
    public Challenge challenge() {
        try {
            ChallengeOptions options = new ChallengeOptions();
            options.algorithm = Algorithm.SHA256;
            options.hmacKey = CaptchaKeys.getSecretKey();

            return Altcha.createChallenge(options);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating challenge", e);
        }
    }

}
