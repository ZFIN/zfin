package org.zfin.infrastructure.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.profile.service.ProfileService;

@Controller
@RequestMapping("/")
public class HomeController {


    @RequestMapping(method= RequestMethod.GET)
    public String index(Model model) {

        if (ProfileService.getCurrentSecurityUser() != null) {
            model.addAttribute("user", ProfileService.getCurrentSecurityUser());
        }

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "ZFIN: The Zebrafish Model Organism Database");

        return "infrastructure/home.page";
    }

}
