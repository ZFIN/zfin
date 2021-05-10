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
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyType;
import org.zfin.antibody.Isotype;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.presentation.LookupStrings;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class AntibodyEditController {

    @Autowired
    AntibodyRepository antibodyRepository;

    @SneakyThrows
    @RequestMapping("/marker/antibody/prototype-edit/{zdbID}")
    public String showAntibodyEdit(Model model, @PathVariable String zdbID) {
        Antibody antibody = antibodyRepository.getAntibodyByID(zdbID);
        if (antibody == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("antibody", antibody);

        addJsonAttribute(model, "hostSpeciesList", mapSpeciesCommonNames(antibodyRepository.getHostSpeciesList()));
        addJsonAttribute(model, "immunogenSpeciesList", mapSpeciesCommonNames(antibodyRepository.getImmunogenSpeciesList()));
        addJsonAttribute(model, "heavyChainIsotypes", mapString(Isotype.HeavyChain.values()));
        addJsonAttribute(model, "lightChainIsotypes", mapString(Isotype.LightChain.values()));
        addJsonAttribute(model, "clonalTypes", new String[]{ AntibodyType.MONOCLONAL.getValue(), AntibodyType.POLYCLONAL.getValue() });

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Edit " + antibody.getAbbreviation());

        return "marker/antibody/antibody-edit";
    }

    private void addJsonAttribute(Model model, String name, Object value) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        model.addAttribute(name, mapper.writeValueAsString(value));
    }

    private List<String> mapSpeciesCommonNames(List<Species> speciesList) {
        return speciesList.stream()
                .map(Species::getCommonName)
                .collect(Collectors.toList());
    }

    private List<String> mapString(Object[] values) {
        return Arrays.stream(values).map(Objects::toString).collect(Collectors.toList());
    }

}
