package org.zfin.marker.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Clone;
import org.zfin.marker.repository.MarkerRepository;

@Controller
@RequestMapping("/marker")
public class CloneEditController {

    @Autowired
    private MarkerRepository markerRepository;

    @RequestMapping(value = "/clone/prototype-edit/{zdbID}")
    private String showCloneEdit(@PathVariable String zdbID, Model model) {
        Clone clone = markerRepository.getCloneById(zdbID);
        if (clone == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
        model.addAttribute("clone", clone);
        return "marker/clone/clone-edit";
    }

}
