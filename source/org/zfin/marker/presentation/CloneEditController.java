package org.zfin.marker.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Clone;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;

@Controller
@RequestMapping("/marker")
public class CloneEditController {

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private MarkerService markerService;

    @SneakyThrows
    @RequestMapping(value = "/clone/prototype-edit/{zdbID}")
    private String showCloneEdit(@PathVariable String zdbID, Model model) {
        Clone clone = markerRepository.getCloneById(zdbID);
        if (clone == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
        model.addAttribute("clone", clone);
        model.addAttribute("markerRelationshipTypes", new ObjectMapper().writeValueAsString(
                markerService.getMarkerRelationshipEditMetadata(clone,
                        MarkerRelationship.Type.CLONE_CONTAINS_GENE,
                        MarkerRelationship.Type.CLONE_CONTAINS_SMALL_SEGMENT
                )));
        return "marker/clone/clone-edit";
    }

}
