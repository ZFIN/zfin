package org.zfin.infrastructure.captcha;

//TODO: implement aspect for redirecting

//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.ProceedingJoinPoint;
//
//import org.springframework.stereotype.Component;
//
//@Aspect
//@Component
public class CaptchaValidationAspect {

//    @Around("@annotation(RequiresCaptcha)")
//    public Object validateCaptcha(ProceedingJoinPoint joinPoint) throws Throwable {
//        HttpServletRequest request = // Obtain the request from the current context, possibly via a method argument
//
//        if (ENABLE_CAPTCHA) {
//            Optional<String> redirectUrl = RecaptchaService.blockAccessIfNotValidated(request);
//
//            if (redirectUrl.isPresent()) {
//                // Perform the redirect directly here
//                return new ModelAndView("redirect:/action/captcha/challenge?redirect=" +
//                        URLEncoder.encode(redirectUrl.get(), StandardCharsets.UTF_8));
//            }
//        }
//
//        // Proceed with the original method if no redirect is needed
//        return joinPoint.proceed();
//    }
}