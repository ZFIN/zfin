package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;

/**
 */
@Controller
public class GenericMarkerViewController {

    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private Logger logger = Logger.getLogger(GenericMarkerViewController.class);

    @RequestMapping("/marker/view/{zdbID}")
    public String getGenericMarkerView(
            Model model
            , @PathVariable("zdbID") String zdbID
    ) {

        logger.info("zdbID: " + zdbID);
        Marker marker = markerRepository.getMarkerByID(zdbID);
        logger.info("gene: " + marker);
        MarkerBean markerBean = new MarkerBean();
        markerBean.setMarker(marker);

        MarkerService.createDefaultViewForMarker(markerBean);

        // MAPPING INFO:
        markerBean.setMappedMarkerBean(MarkerService.getMappedMarkers(marker));

        model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, markerBean.getMarkerTypeDisplay() + ": " + marker.getAbbreviation());

        return "marker/marker-view.page";
    }

}

