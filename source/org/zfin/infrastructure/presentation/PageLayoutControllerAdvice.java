package org.zfin.infrastructure.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.zebrashare.repository.ZebrashareRepository;

import java.time.Year;

@ControllerAdvice
public class PageLayoutControllerAdvice {

    @Autowired
    private ZebrashareRepository zebrashareRepository;

    @ModelAttribute("currentUser")
    public Person populateCurrentUser() {
        return ProfileService.getCurrentSecurityUser();
    }

    @ModelAttribute("currentUserHasZebraShareSubmissions")
    public boolean populateCurrentUserHasZebraShareSubmissions() {
        Person person = ProfileService.getCurrentSecurityUser();
        return CollectionUtils.isNotEmpty(zebrashareRepository.getZebraSharePublicationsForPerson(person));
    }

    @ModelAttribute("copyrightYear")
    public int populateCopyrightYear() {
        return Year.now().getValue();
    }

    // Needed for exception handling which cannot pre-populate model attributes (I don't know why)
    public void populateModelAttributes(Model model){
        model.addAttribute("currentUser", populateCurrentUser());
        model.addAttribute("currentUserHasZebraShareSubmissions", populateCurrentUserHasZebraShareSubmissions());
        model.addAttribute("copyrightYear", populateCopyrightYear());
    }
}
