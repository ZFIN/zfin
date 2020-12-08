package org.zfin.marker.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.profile.service.ProfileService;

import java.util.ArrayList;
import java.util.List;

@Controller
public class SequenceTargetingReagentEditController {

    @Autowired
    MarkerRepository markerRepository;

    @Autowired
    MarkerService markerService;

    @RequestMapping("/str/{zdbID}/edit")
    public String showEditPublicationForm(Model model, @PathVariable String zdbID) {
        SequenceTargetingReagent str = markerRepository.getSequenceTargetingReagent(zdbID);
        if (str == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("str", str);
        model.addAttribute("showSupplier", str.getType() == Marker.Type.TALEN || str.getType() == Marker.Type.CRISPR);
        model.addAttribute("user", ProfileService.getCurrentSecurityUser());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Edit " + str.getAbbreviation());

        return "marker/sequence-targeting-reagent-edit";
    }

    @SneakyThrows
    @RequestMapping("/marker/str/prototype-edit/{zdbID}")
    public String showSTRPrototypeEdit(Model model, @PathVariable String zdbID) {
        SequenceTargetingReagent str = markerRepository.getSequenceTargetingReagent(zdbID);
        if (str == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("str", str);
        List<MarkerRelationship.Type> types = new ArrayList<>();
        types.add(MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE);
        if (str.getType() == Marker.Type.CRISPR) {
            types.add(MarkerRelationship.Type.CRISPR_TARGETS_REGION);
        }
        if (str.getType() == Marker.Type.TALEN) {
            types.add(MarkerRelationship.Type.TALEN_TARGETS_REGION);
        }
        model.addAttribute("markerRelationshipTypes", new ObjectMapper().writeValueAsString(
                markerService.getMarkerRelationshipEditMetadata(str, types.toArray(new MarkerRelationship.Type[] {}))));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Edit " + str.getAbbreviation());

        return "marker/sequenceTargetingReagent/sequence-targeting-reagent-edit";
    }

}
