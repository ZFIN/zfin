package org.zfin.marker.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.Species;
import org.zfin.antibody.AntibodyType;
import org.zfin.antibody.Isotype;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.*;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.profile.service.ProfileService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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


        addJsonAttribute(model, "transcriptTypes", mapString(TranscriptType.Type.values()));
        addJsonAttribute(model, "transcriptStatus", mapString(TranscriptStatus.Status.values()));


        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Edit " + transcript.getAbbreviation());

        return "marker/transcript/transcript-edit";
    }

    private void addJsonAttribute(Model model, String name, Object value) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        model.addAttribute(name, mapper.writeValueAsString(value));
    }


    private List<String> mapString(Object[] values) {
        return Arrays.stream(values).map(Objects::toString).collect(Collectors.toList());
    }


}
