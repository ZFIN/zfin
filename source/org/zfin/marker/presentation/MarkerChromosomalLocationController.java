package org.zfin.marker.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.gwt.root.dto.CuratorNoteDTO;
import org.zfin.gwt.root.dto.NoteDTO;
import org.zfin.gwt.root.server.DTOMarkerService;
import org.zfin.infrastructure.DataNote;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.profile.MarkerSupplier;
import org.zfin.profile.Organization;
import org.zfin.profile.presentation.ChromosomalLocationBean;
import org.zfin.profile.presentation.ChromosomalLocationBeanValidator;
import org.zfin.profile.presentation.SupplierBean;
import org.zfin.profile.presentation.SupplierBeanValidator;
import org.zfin.profile.repository.ProfileRepository;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/marker")
public class MarkerChromosomalLocationController {

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @InitBinder("chromosomalLocationBean")
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(new ChromosomalLocationBeanValidator());
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/chromosomal-location", method = RequestMethod.GET)
    public ChromosomalLocationBean getChromosomalLocationForMarker(@PathVariable String zdbID) {
        ChromosomalLocationBean chromosomalLocation = new ChromosomalLocationBean();
        chromosomalLocation.setChromosome("1");
        return chromosomalLocation;
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/chromosomal-location", method = RequestMethod.POST)
    public ChromosomalLocationBean addChromosomalLocationForMarker(@PathVariable String zdbID,
                                             @Valid @RequestBody ChromosomalLocationBean chromosomalLocation,
                                             BindingResult errors) {
        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid Chromosomal Location", errors);
        }

        System.out.println("TODO: implement adding chromosomal location information");

        ChromosomalLocationBean chromosomalLocationBean = new ChromosomalLocationBean();
        chromosomalLocationBean.setChromosome("2");
        return chromosomalLocationBean;
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/chromosomal-location/{chromosomalLocationID}", method = RequestMethod.POST)
    public ChromosomalLocationBean updateCuratorNote(@PathVariable String markerId,
                                            @PathVariable String chromosomalLocationID,
                                            @Valid @RequestBody ChromosomalLocationBean chromosomalLocation) {

        System.out.println("TODO: implement editing chromosomal location information");

        ChromosomalLocationBean chromosomalLocationBean = new ChromosomalLocationBean();
        chromosomalLocationBean.setChromosome("3");
        return chromosomalLocationBean;
    }

    @ResponseBody
    @RequestMapping(value = "/{markerID}/chromosomal-location/{zdbId}", method = RequestMethod.DELETE, produces = "text/plain")
    public String removeChromosomalLocation(@PathVariable String markerID,
                                 @PathVariable String zdbId) {
        //TODO: implement this
        return "OK";
    }

}
