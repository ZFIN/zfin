package org.zfin.marker.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.Species;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyType;
import org.zfin.antibody.Isotype;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@Log4j2
public class EfgEditController {

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private MarkerService markerService;

    @SneakyThrows
    @RequestMapping("/marker/efg/prototype-edit/{zdbID}")
    public String showAntibodyEdit(Model model, @PathVariable String zdbID) {

        zdbID = markerService.getActiveMarkerID(zdbID);
        log.info("zdbID: " + zdbID);
        Marker efg = markerRepository.getMarkerByID(zdbID);

        if (efg == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("efg", efg);

/*
        addJsonAttribute(model, "hostSpeciesList", mapSpeciesCommonNames(antibodyRepository.getHostSpeciesList()));
        addJsonAttribute(model, "immunogenSpeciesList", mapSpeciesCommonNames(antibodyRepository.getImmunogenSpeciesList()));
        addJsonAttribute(model, "heavyChainIsotypes", mapString(Isotype.HeavyChain.values()));
        addJsonAttribute(model, "lightChainIsotypes", mapString(Isotype.LightChain.values()));
        addJsonAttribute(model, "clonalTypes", new String[]{ AntibodyType.MONOCLONAL.getValue(), AntibodyType.POLYCLONAL.getValue() });
*/

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Edit " + efg.getAbbreviation());

        return "marker/efg/efg-edit";
    }

    private void addJsonAttribute(Model model, String name, Object value) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        model.addAttribute(name, mapper.writeValueAsString(value));
    }

}
