package org.zfin.profile.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.presentation.MarkerViewController;
import org.zfin.properties.ZfinProperties;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;

/**
 */
@Controller
public class ProfileController {

    @Autowired
    private PersonController personController;

    @Autowired
    private LabController labController;

    @Autowired
    private CompanyController companyController;

    @Autowired
    private MarkerViewController markerViewController;

    @RequestMapping(value = "/view/{zdbID}", method = RequestMethod.GET)

    public String viewProfile(@PathVariable String zdbID, Model model, @RequestHeader("User-Agent") String userAgent) {
        model.addAttribute("deleteURL", "/action/infrastructure/deleteRecord/" + zdbID);
        if (zdbID.startsWith("ZDB-LAB")) {
            return labController.viewLab(zdbID, model);
        } else if (zdbID.startsWith("ZDB-COMPANY")) {
            return companyController.viewCompany(zdbID, model);
        } else if (zdbID.startsWith("ZDB-PERS")) {
            return personController.viewPerson(zdbID, model);
        } else {
            // TODO: THIS IS HACK as this looks the same as marker in web.xml (and this gets read first).
            return markerViewController.getAnyMarker(model, zdbID, userAgent);
        }
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

    @RequestMapping(value="/help/international-characters")
    public String getInternationalCharactersHelp(HttpServletRequest request){
        return "international-characters.popup";
    }

}
