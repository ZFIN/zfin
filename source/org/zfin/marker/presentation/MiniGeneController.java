package org.zfin.marker.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class MiniGeneController extends AbstractController {

    private static Logger logger = Logger.getLogger(MiniGeneController.class);

    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) throws Exception {
        String zdbID = httpServletRequest.getParameter(LookupStrings.ZDB_ID);
        String abbreviation = httpServletRequest.getParameter("abbrev");
        logger.info("zdbID: " + zdbID);
        Marker marker;

        if (!StringUtils.isEmpty(zdbID))
            marker = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        else
            marker = RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(abbreviation);

        logger.info("marker: " + marker);
        MarkerBean markerBean = new MarkerBean();
        markerBean.setMarker(marker);

        ModelAndView modelAndView = new ModelAndView("marker/mini-gene.insert", LookupStrings.FORM_BEAN, markerBean);

        String showClose = httpServletRequest.getParameter("showClose");
        modelAndView.addObject("showClose", Boolean.valueOf(showClose));
        String external = httpServletRequest.getParameter("external");
        modelAndView.addObject("external", Boolean.valueOf(external));


        return modelAndView;

    }
}
