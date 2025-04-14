package org.zfin.infrastructure.presentation;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.featureflag.FeatureFlagEnum;
import org.zfin.framework.featureflag.FeatureFlags;
import org.zfin.infrastructure.captcha.CaptchaKeys;
import org.zfin.infrastructure.captcha.CaptchaService;

import java.io.IOException;

@Log4j2
@Controller
@RequestMapping("/captcha")
public class CaptchaController {

    @GetMapping("/challenge")
    public String challenge(
            Model model,
            @RequestParam(name="redirect", required = false) String redirect
    ) throws IOException {
        model.addAttribute("siteKey", CaptchaKeys.getSiteKey());
        model.addAttribute("redirect", redirect);

        //if we somehow got to this challenge page without captcha's enabled, just redirect back
        if (!FeatureFlags.isFlagEnabled(FeatureFlagEnum.ENABLE_CAPTCHA)) {
            if (StringUtils.isEmpty(redirect)) {
                return "redirect:/";
            }
            return "redirect:" + redirect;
        }
        return "infrastructure/captcha-" + CaptchaService.getCurrentVersion() + "-challenge";
    }

    @PostMapping("/challenge")
    public String challengeResponse(
            HttpServletRequest request
    ) throws IOException {
        String redirect = request.getParameter("redirect");
        String challengeResponse = request.getParameter("g-recaptcha-response");
        if (CaptchaService.getCurrentVersion().equals(CaptchaKeys.Version.altcha)) {
            challengeResponse = request.getParameter("altcha");
        }
        if (CaptchaService.verifyCaptcha(challengeResponse)) {
            if (StringUtils.isEmpty(redirect)) {
                return "infrastructure/captcha-response";
            }
            return "redirect:" + redirect;
        }
        return "infrastructure/captcha-failed";
    }

}
