package org.zfin.marker.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.mapping.GenomeLocation;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.profile.presentation.ChromosomalLocationBean;
import org.zfin.profile.presentation.ChromosomalLocationBeanValidator;
import org.zfin.profile.repository.ProfileRepository;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<ChromosomalLocationBean> getChromosomalLocationForMarker(@PathVariable String zdbID) {
        List<GenomeLocation> genomeLocations = markerRepository.getGenomeLocation(zdbID);
        List<ChromosomalLocationBean> resultList = genomeLocations
                .stream()
                .map(ChromosomalLocationBean::fromGenomeLocation)
                .collect(Collectors.toList());
        return resultList;
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/chromosomal-location", method = RequestMethod.POST)
    public ChromosomalLocationBean addChromosomalLocationForMarker(@PathVariable String zdbID,
                                             @Valid @RequestBody ChromosomalLocationBean chromosomalLocation,
                                             BindingResult errors) {
        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid Chromosomal Location", errors);
        }

        GenomeLocation genomeLocation = chromosomalLocation.toGenomeLocation();
        genomeLocation.setEntityID(zdbID);
        GenomeLocation persistedLocation = markerRepository.addGenomeLocation(genomeLocation);

        return ChromosomalLocationBean.fromGenomeLocation(persistedLocation);
    }

    @ResponseBody
    @RequestMapping(value = "/{markerId}/chromosomal-location/{ID}", method = RequestMethod.POST)
    public ChromosomalLocationBean updateChromosomalLocationForMarker(@PathVariable String markerId,
                                            @PathVariable Long ID,
                                            @Valid @RequestBody ChromosomalLocationBean chromosomalLocationBean) {

        GenomeLocation genomeLocation = markerRepository.getGenomeLocationByID(ID);
        genomeLocation.setAssembly(chromosomalLocationBean.getAssembly());
        genomeLocation.setChromosome(chromosomalLocationBean.getChromosome());
        genomeLocation.setStart(chromosomalLocationBean.getStartLocation());
        genomeLocation.setEnd(chromosomalLocationBean.getEndLocation());

        return chromosomalLocationBean;
    }

    @ResponseBody
    @RequestMapping(value = "/{markerID}/chromosomal-location/{ID}", method = RequestMethod.DELETE, produces = "text/plain")
    public String removeChromosomalLocation(@PathVariable String markerID,
                                 @PathVariable Long ID) {
        markerRepository.deleteGenomeLocation(ID);
        return "OK";
    }


    @ResponseBody
    @RequestMapping(value = "/{markerID}/chromosomal-location/{ID}", method = RequestMethod.GET)
    public ChromosomalLocationBean getChromosomalLocationForMarker(@PathVariable String markerID,
                                                                         @PathVariable Long ID) {
        GenomeLocation genomeLocation = markerRepository.getGenomeLocationByID(ID);
        return ChromosomalLocationBean.fromGenomeLocation(genomeLocation);
    }

}
