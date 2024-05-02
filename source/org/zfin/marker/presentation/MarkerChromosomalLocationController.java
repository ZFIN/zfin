package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mapping.MappingService;
import org.zfin.mapping.MarkerLocation;
import org.zfin.marker.repository.MarkerRepository;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/marker")
public class MarkerChromosomalLocationController {

    //todo: determine this based on user input
    private static final String MANUALLY_CURATED_TERM_ID = "ZDB-TERM-170419-251"; //(curator inference used in manual assertion)

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @InitBinder("chromosomalLocationBean")
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(new ChromosomalLocationBeanValidator());
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/{zdbID}/chromosomal-location", method = RequestMethod.GET)
    public List<ChromosomalLocationBean> getChromosomalLocationForMarker(@PathVariable String zdbID) {
        List<MarkerLocation> genomeLocations = MappingService.getMarkerLocation(zdbID);
        return genomeLocations
                .stream()
                .map(ChromosomalLocationBean::fromMarkerLocation)
                .collect(Collectors.toList());
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/{markerId}/chromosomal-location", method = RequestMethod.POST)
    public ChromosomalLocationBean addChromosomalLocationForMarker(@PathVariable String markerId,
                                             @Valid @RequestBody ChromosomalLocationBean chromosomalLocation,
                                             BindingResult errors) {
        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid Chromosomal Location", errors);
        }

        Transaction transaction = HibernateUtil.currentSession().beginTransaction();

        MarkerLocation markerLocation = chromosomalLocation.toMarkerLocation();
        MarkerLocation persistedLocation = markerRepository.addMarkerLocation(markerLocation);
        markerLocation.setLocationReferences(chromosomalLocation);

        transaction.commit();

        return ChromosomalLocationBean.fromMarkerLocation(persistedLocation);
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/{markerId}/chromosomal-location/{zdbID}", method = RequestMethod.POST)
    public ChromosomalLocationBean updateChromosomalLocationForMarker(@PathVariable String markerId,
                                            @PathVariable String zdbID,
                                            @Valid @RequestBody ChromosomalLocationBean chromosomalLocationBean) {

        Transaction transaction = HibernateUtil.currentSession().beginTransaction();
        MarkerLocation markerLocation = markerRepository.getMarkerLocationByID(zdbID);
        markerLocation.setFieldsByChromosomalLocationBean(chromosomalLocationBean);
        markerLocation.setLocationReferences(chromosomalLocationBean);
        markerRepository.saveMarkerLocation(markerLocation);
        transaction.commit();
        return chromosomalLocationBean;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/{markerID}/chromosomal-location/{zdbID}", method = RequestMethod.DELETE)
    public ChromosomalLocationBean removeChromosomalLocation(@PathVariable String markerID,
                                 @PathVariable String zdbID) {
        MarkerLocation markerLocation = markerRepository.getMarkerLocationByID(zdbID);
        ChromosomalLocationBean deletedChromosomalLocation = ChromosomalLocationBean.fromMarkerLocation(markerLocation);

        Transaction transaction = HibernateUtil.currentSession().beginTransaction();
        markerRepository.deleteMarkerLocation(zdbID);
        transaction.commit();

        return deletedChromosomalLocation;
    }


    @JsonView(View.API.class)
    @RequestMapping(value = "/{markerID}/chromosomal-location/{zdbID}", method = RequestMethod.GET)
    public ChromosomalLocationBean getChromosomalLocationForMarker(@PathVariable String markerID,
                                                                         @PathVariable String zdbID) {
        MarkerLocation markerLocation = markerRepository.getMarkerLocationByID(zdbID);
        return ChromosomalLocationBean.fromMarkerLocation(markerLocation);
    }

}
