package org.zfin.infrastructure.presentation;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;

import java.time.Year;

@ControllerAdvice
public class PageLayoutControllerAdvice {

    @ModelAttribute("currentUser")
    public Person populateCurrentUser() {
        return ProfileService.getCurrentSecurityUser();
    }

    @ModelAttribute("copyrightYear")
    public int populateCopyrightYear() {
        return Year.now().getValue();
    }

}
