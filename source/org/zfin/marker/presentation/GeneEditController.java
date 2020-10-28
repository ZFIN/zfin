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

    @RequestMapping(value = "/gene/prototype-edit/{zdbID}")
    public String getGeneEdit(Model model, @PathVariable String zdbID) {
        Marker gene = markerRepository.getMarkerByID(zdbID);
        model.addAttribute("gene", gene);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Edit Gene: " + gene.getAbbreviation());
        return "marker/gene/gene-edit";
    }

}
