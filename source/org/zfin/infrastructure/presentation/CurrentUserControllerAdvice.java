package org.zfin.infrastructure.presentation;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;

@ControllerAdvice
public class CurrentUserControllerAdvice {

    @ModelAttribute("currentUser")
    public Person populateCurrentUser() {
        return ProfileService.getCurrentSecurityUser();
    }

}
