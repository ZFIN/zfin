package org.zfin.marker.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.profile.service.ProfileService;

@Controller
@RequestMapping("/str")
public class SequenceTargetingReagentEditController {

    @Autowired
    MarkerRepository markerRepository;

    @RequestMapping("/{zdbID}/edit")
    public String showEditPublicationForm(Model model, @PathVariable String zdbID) {
        SequenceTargetingReagent str = markerRepository.getSequenceTargetingReagent(zdbID);
        if (str == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("str", str);
        model.addAttribute("showSupplier", str.getType() == Marker.Type.TALEN || str.getType() == Marker.Type.CRISPR);
        model.addAttribute("user", ProfileService.getCurrentSecurityUser());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Edit " + str.getAbbreviation());

        return "marker/sequence-targeting-reagent-edit.page";
    }

}
