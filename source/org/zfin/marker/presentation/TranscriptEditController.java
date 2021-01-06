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
import org.zfin.marker.Transcript;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.profile.service.ProfileService;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TranscriptEditController {

    @Autowired
    MarkerRepository markerRepository;

    @Autowired
    MarkerService markerService;



    @SneakyThrows
    @RequestMapping("/marker/transcript/prototype-edit/{zdbID}")
    public String showTranscriptPrototypeEdit(Model model, @PathVariable String zdbID) {
        Transcript transcript = markerRepository.getTranscriptByZdbID(zdbID);
        if (transcript == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("transcript", transcript);
        if (!transcript.getTranscriptType().getDisplay().equals("mRNA"))  {

                model.addAttribute("markerRelationshipTypes", new ObjectMapper().writeValueAsString(
                        markerService.getMarkerRelationshipEditMetadata(transcript,
                                MarkerRelationship.Type.TRANSCRIPT_TARGETS_GENE,
                                MarkerRelationship.Type.CLONE_CONTAINS_TRANSCRIPT,
                                MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT
                        )));

        }
        else{

                model.addAttribute("markerRelationshipTypes", new ObjectMapper().writeValueAsString(
                        markerService.getMarkerRelationshipEditMetadata(transcript,
                                MarkerRelationship.Type.CLONE_CONTAINS_TRANSCRIPT,
                                MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT
                        )));

        }
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Edit " + transcript.getAbbreviation());

        return "marker/transcript/transcript-edit";
    }

}
