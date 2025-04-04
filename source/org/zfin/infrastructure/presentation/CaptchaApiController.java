package org.zfin.infrastructure.presentation;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.framework.api.NameValuePairDTO;
import org.zfin.infrastructure.captcha.RecaptchaService;

import java.io.IOException;

//TODO: uncomment below for helpful debugging methods
//@Log4j2
//@RestController
//@RequestMapping("/captcha")
public class CaptchaApiController {

//    @RequestMapping(value = "/challenge-bypass", method = RequestMethod.GET)
//    public String challengeBypass(HttpServletRequest request, @RequestParam(name = "token") String token) throws IOException {
//        if ("abcdefg".equals(token)) {
//            RecaptchaService.setSuccessfulCaptchaToken(request);
//            return "bypass";
//        }
//        return "no bypass";
//    }
//
//    @RequestMapping(value = "/challenge-status", method = RequestMethod.GET)
//    public NameValuePairDTO challengeStatus(HttpServletRequest request) throws IOException {
//        NameValuePairDTO pair = new NameValuePairDTO();
//        pair.setName("status");
//        pair.setValue(RecaptchaService.isSuccessfulCaptchaToken(request) ? "success" : "failed");
//        return pair;
//    }
//
//
//    @RequestMapping(value = "/challenge-invalidate", method = RequestMethod.GET)
//    public String challengeInvalidate(HttpServletRequest request) throws IOException {
//        RecaptchaService.unsetSuccessfulCaptchaToken(request);
//        return "invalidated";
//    }
}
