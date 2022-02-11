package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.mapping.GenomeLocation;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.profile.presentation.ChromosomalLocationBean;
import org.zfin.profile.presentation.ChromosomalLocationBeanValidator;
import org.zfin.profile.repository.ProfileRepository;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/marker")
public class MarkerChromosomalLocationController {

    @Autowired
    private MarkerRepository markerRepository;

    @InitBinder("chromosomalLocationBean")
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(new ChromosomalLocationBeanValidator());
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/{zdbID}/chromosomal-location", method = RequestMethod.GET)
    public List<ChromosomalLocationBean> getChromosomalLocationForMarker(@PathVariable String zdbID) {
        List<GenomeLocation> genomeLocations = markerRepository.getGenomeLocation(zdbID);
        return genomeLocations
                .stream()
                .map(ChromosomalLocationBean::fromGenomeLocation)
                .collect(Collectors.toList());
    }

    @JsonView(View.API.class)
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

    @JsonView(View.API.class)
    @RequestMapping(value = "/{markerId}/chromosomal-location/{ID}", method = RequestMethod.POST)
    public ChromosomalLocationBean updateChromosomalLocationForMarker(@PathVariable String markerId,
                                            @PathVariable Long ID,
                                            @Valid @RequestBody ChromosomalLocationBean chromosomalLocationBean) {

        GenomeLocation genomeLocation = markerRepository.getGenomeLocationByID(ID);
        genomeLocation.setAssembly(chromosomalLocationBean.getAssembly());
        genomeLocation.setChromosome(chromosomalLocationBean.getChromosome());
        genomeLocation.setStart(chromosomalLocationBean.getStartLocation());
        genomeLocation.setEnd(chromosomalLocationBean.getEndLocation());
        markerRepository.saveGenomeLocation(genomeLocation);

        return chromosomalLocationBean;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/{markerID}/chromosomal-location/{ID}", method = RequestMethod.DELETE)
    public ChromosomalLocationBean removeChromosomalLocation(@PathVariable String markerID,
                                 @PathVariable Long ID) {
        GenomeLocation genomeLocation = markerRepository.getGenomeLocationByID(ID);
        ChromosomalLocationBean deletedChromosomalLocation = ChromosomalLocationBean.fromGenomeLocation(genomeLocation);
        markerRepository.deleteGenomeLocation(ID);
        return deletedChromosomalLocation;
    }


    @JsonView(View.API.class)
    @RequestMapping(value = "/{markerID}/chromosomal-location/{ID}", method = RequestMethod.GET)
    public ChromosomalLocationBean getChromosomalLocationForMarker(@PathVariable String markerID,
                                                                         @PathVariable Long ID) {
        GenomeLocation genomeLocation = markerRepository.getGenomeLocationByID(ID);
        return ChromosomalLocationBean.fromGenomeLocation(genomeLocation);
    }

}
