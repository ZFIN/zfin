package org.zfin.marker.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerGoService;
import org.zfin.repository.RepositoryFactory;

/**
 * Created by kschaper on 12/16/14.
 */
@Controller
@RequestMapping("/marker")
public class MarkerGoViewController {

    @Autowired
    MarkerGoService markerGoService;

    @RequestMapping("/marker-go-view/{markerZdbId}")
    public String markerGoView(Model model, @PathVariable String markerZdbId) {


        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerOrReplacedByID(markerZdbId);
        if (marker == null) {
            model.addAttribute("markerZdbId", markerZdbId);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }



        model.addAttribute("marker",marker);
        model.addAttribute("markerGoViewTableRows",markerGoService.getMarkerGoViewTableRows(marker));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "GO Details: " + marker.getAbbreviation());

        return "marker/marker-go-view.page";
    }

    @RequestMapping("/marker-go-edit/{markerZdbId}")
    public String markerGoEdit(Model model, @PathVariable String markerZdbId) {

        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        Marker marker = markerRepository.getMarkerByID(markerZdbId);

        model.addAttribute("marker",marker);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "GO Update: " + marker.getAbbreviation());

        return "marker/marker-go-edit.page";
    }


}
