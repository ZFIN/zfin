package org.zfin.marker.presentation;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.presentation.LookupStrings;

@Controller
public class AntibodyEditController {

    @Autowired
    AntibodyRepository antibodyRepository;

    @SneakyThrows
    @RequestMapping("/marker/antibody/prototype-edit/{zdbID}")
    public String showAntibodyEdit(Model model, @PathVariable String zdbID) {
        Antibody antibody = antibodyRepository.getAntibodyByID(zdbID);
        if (antibody == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("antibody", antibody);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Edit " + antibody.getAbbreviation());

        return "marker/antibody/antibody-edit";
    }

}
