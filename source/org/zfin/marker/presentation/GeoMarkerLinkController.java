package org.zfin.marker.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.expression.service.ExpressionService;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;

/**
 */
@Controller
public class GeoMarkerLinkController {

    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

    @Autowired
    ExpressionService expressionService ;

    @RequestMapping("/microarray/geo/link/{symbol}")
    public String getGeoLink(@PathVariable String symbol, Model model) {

        Marker marker = markerRepository.getMarkerByAbbreviation(symbol);

        model.addAttribute("geolink", expressionService.getGeoLinkForMarker(marker));

        return "marker/geo-link.insert";
    }

}

