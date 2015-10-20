package org.zfin.marker.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;

@Controller
@RequestMapping("/marker")
public class GeneEditController {

    @Autowired
    MarkerRepository markerRepository;

    @RequestMapping("/{zdbID}/edit")
    public String showOrthology(@PathVariable("zdbID") String zdbID,
                                Model model) {
        Marker gene = markerRepository.getMarkerByID(zdbID);

        if (gene == null) {
            model.addAttribute(LookupStrings.ZDB_ID, zdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("gene", gene);
        return "marker/gene-edit.page";
    }

}
