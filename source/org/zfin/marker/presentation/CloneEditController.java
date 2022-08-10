package org.zfin.marker.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

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
        addJsonAttribute(model, "cloningSiteList", escapeQuote(markerRepository.getCloneSites().stream().filter(Objects::nonNull).collect(toList())));
        addJsonAttribute(model, "libraryList", escapeQuote(markerRepository.getProbeLibraryNames().stream().filter(Objects::nonNull).collect(toList())));
        addJsonAttribute(model, "vectorList", escapeQuote(markerRepository.getVectorNames().stream().filter(Objects::nonNull).collect(toList())));
        addJsonAttribute(model, "digestList", escapeQuote(markerRepository.getDigests().stream().filter(Objects::nonNull).collect(toList())));
        addJsonAttribute(model, "polymeraseListList", escapeQuote(markerRepository.getPolymeraseNames().stream().filter(Objects::nonNull).collect(toList())));
        model.addAttribute("markerRelationshipTypes", new ObjectMapper().writeValueAsString(
                markerService.getMarkerRelationshipEditMetadata(clone,
                        MarkerRelationship.Type.CLONE_CONTAINS_GENE,
                        MarkerRelationship.Type.CLONE_CONTAINS_SMALL_SEGMENT
                )));
        return "marker/clone/clone-edit";
    }

    private List<String> escapeQuote(List<String> list) {
        return list.stream().map(s -> s.replace("'", "")).collect(toList());
    }

    private void addJsonAttribute(Model model, String name, Object value) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        model.addAttribute(name, mapper.writeValueAsString(value));
    }

}
