package org.zfin.marker.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.seo.CanonicalLinkConfig;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;

@Controller
@RequestMapping("/marker")
public class GenericMarkerViewController {

    private Logger logger = LogManager.getLogger(GenericMarkerViewController.class);

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private MarkerService markerService;

    @RequestMapping("/generic/view/{zdbID}")
    public String getGenericMarkerView(Model model, @PathVariable("zdbID") String zdbID) throws Exception {
        CanonicalLinkConfig.addCanonicalIfFound(model);

        zdbID = markerService.getActiveMarkerID(zdbID);
        logger.info("zdbID: " + zdbID);
        Marker marker = markerRepository.getMarkerByID(zdbID);
        logger.info("gene: " + marker);
        MarkerBean markerBean = new MarkerBean();
        markerBean.setMarker(marker);

        MarkerService.createDefaultViewForMarker(markerBean);

        model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, markerBean.getMarkerTypeDisplay() + ": " + marker.getAbbreviation());

        return "marker/marker-view";
    }

}

