package org.zfin.marker.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;

/**
 */
@Controller
public class MiniGeneController {

    private static Logger logger = Logger.getLogger(MiniGeneController.class);

    @RequestMapping(value ="/mini-gene")
    public String getMiniGeneView(Model model
            ,@RequestParam(value="zdbID",required = false) String zdbID
            ,@RequestParam(value="abbrev",required = false) String abbreviation
            ,@RequestParam(value="showClose",required = false) Boolean showClose
            ,@RequestParam(value="external",required = false) Boolean external
    )
            throws Exception {
        logger.info("zdbID: " + zdbID);
        Marker marker;

        if (!StringUtils.isEmpty(zdbID)){
            marker = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        }
        else{
            marker = RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(abbreviation);
        }

        logger.info("marker: " + marker);
        MarkerBean markerBean = new MarkerBean();
        markerBean.setMarker(marker);

        model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
        model.addAttribute("showClose", showClose);
        model.addAttribute("external", external);

        return "marker/mini-gene.insert";

    }
}
