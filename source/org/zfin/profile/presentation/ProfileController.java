package org.zfin.profile.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.presentation.LookupStrings;

/**
 */
@Controller
@RequestMapping(value = "/profile")
public class ProfileController {

    @Autowired
    private PersonController personController;

    @Autowired
    private LabController labController;

    @Autowired
    private CompanyController companyController;

    @RequestMapping(value = "/view/{zdbID}", method = RequestMethod.GET)
    public String viewProfile(@PathVariable String zdbID, Model model) {
        model.addAttribute("deleteURL", "/action/infrastructure/deleteRecord/" + zdbID);
        if (zdbID.startsWith("ZDB-LAB")) {
            return labController.viewLab(zdbID, model);
        } else if (zdbID.startsWith("ZDB-COMPANY")) {
            return companyController.viewCompany(zdbID, model);
        } else if (zdbID.startsWith("ZDB-PERS")) {
            return personController.viewPerson(zdbID, model);
        }
        return null;
    }


    @RequestMapping(value = "/edit/{zdbID}", method = RequestMethod.GET)
    public String submitEdit(@PathVariable String zdbID, Model model) {
        if (zdbID.startsWith("ZDB-LAB")) {
            return labController.editView(zdbID, model);
        } else if (zdbID.startsWith("ZDB-COMPANY")) {
            return companyController.editView(zdbID, model);
        } else if (zdbID.startsWith("ZDB-PERS")) {
            return personController.editView(zdbID, model);
        } else {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
    }

    @RequestMapping(value = "/help/international-characters")
    public String getInternationalCharactersHelp() {
        return "international-characters";
    }

}
