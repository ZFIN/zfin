package org.zfin.infrastructure.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.infrastructure.captcha.RecaptchaKeys;
import org.zfin.infrastructure.captcha.RecaptchaService;

import java.io.IOException;

@Log4j2
@Controller
@RequestMapping("/captcha")
public class CaptchaController {

    @RequestMapping(value = "/{version}/challenge", method = RequestMethod.GET)
    public String challenge(
            Model model,
            @PathVariable String version,
            @RequestParam(name="redirect") String redirect) throws IOException {
        model.addAttribute("siteKey", RecaptchaKeys.getSiteKey(version));
        model.addAttribute("redirect", redirect);
        return "infrastructure/captcha" + version + "-challenge";
    }

    @RequestMapping(value = "/{version}/challenge", method = RequestMethod.POST)
    public String challengeResponse(
            @PathVariable String version,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        String challengeResponse = request.getParameter("g-recaptcha-response");
        String redirect = request.getParameter("redirect");
        boolean success = RecaptchaService.verifyRecaptcha(version, challengeResponse);
        if (success) {
            RecaptchaService.setSuccessfulCaptchaToken(response);
            if (StringUtils.isEmpty(redirect)) {
                return "infrastructure/captcha-response";
            } else {
                return "redirect:" + redirect;
            }
        }
        return "infrastructure/captcha-failed";
    }
}
