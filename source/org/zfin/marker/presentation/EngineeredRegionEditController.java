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
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;

@Controller
@Log4j2
public class EngineeredRegionEditController {

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private MarkerService markerService;

    @SneakyThrows
    @RequestMapping("/marker/eregion/prototype-edit/{zdbID}")
    public String showAntibodyEdit(Model model, @PathVariable String zdbID) {

        zdbID = markerService.getActiveMarkerID(zdbID);
        log.info("zdbID: " + zdbID);
        Marker eregion = markerRepository.getMarkerByID(zdbID);

        if (eregion == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("eregion", eregion);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Edit " + eregion.getAbbreviation());

        return "marker/eregion/eregion-edit";
    }

}
