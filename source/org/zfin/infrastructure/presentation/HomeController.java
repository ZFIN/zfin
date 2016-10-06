package org.zfin.infrastructure.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;

@Controller
@RequestMapping("/")
public class HomeController {


    @RequestMapping(method= RequestMethod.GET)
    public String index(Model model) {

        Person person = ProfileService.getCurrentSecurityUser();
        if (person != null) {
            person.getLabs();
            model.addAttribute("user", person);
        }

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "The Zebrafish Model Organism Database");

        return "infrastructure/home.page";
    }

}
