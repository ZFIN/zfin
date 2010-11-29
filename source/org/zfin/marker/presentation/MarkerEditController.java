package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.zfin.antibody.Antibody;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Clone;
import org.zfin.marker.Transcript;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class MarkerEditController extends AbstractController {

    private static Logger logger = Logger.getLogger(MarkerEditController.class);

    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) throws Exception {
        String zdbID = httpServletRequest.getParameter(LookupStrings.ZDB_ID);
        logger.info("zdbID: " + zdbID);

        MarkerBean markerBean = new MarkerBean();

        if (zdbID.startsWith("ZDB-TSCRIPT-")) {
            Transcript transcript = RepositoryFactory.getMarkerRepository().getTranscriptByZdbID(zdbID);
            if (transcript != null) {
                markerBean.setMarker(transcript);
                ModelAndView modelAndView = new ModelAndView("marker/transcript-edit.page", LookupStrings.FORM_BEAN, markerBean);
                modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, Area.TRANSCRIPT.getEditTitleString()+transcript.getAbbreviation());
                return modelAndView;
            }
        }


        if (zdbID.startsWith("ZDB-ATB-")) {
            Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID(zdbID);
            if (antibody != null) {
                markerBean.setMarker(antibody);
                ModelAndView modelAndView = new ModelAndView("marker/antibody-edit.page", LookupStrings.FORM_BEAN, markerBean);
                modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, Area.ANTIBODY.getEditTitleString()+antibody.getAbbreviation());
                return modelAndView;
            }
        }


        // handle things that mark to clone
        Clone clone = RepositoryFactory.getMarkerRepository().getCloneById(zdbID);
        if (clone != null) {
            markerBean.setMarker(clone);
            ModelAndView modelAndView = new ModelAndView("marker/clone-edit.page", LookupStrings.FORM_BEAN, markerBean);
            modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, Area.CLONE.getEditTitleString()+clone.getAbbreviation());
            return modelAndView;
        }

        ModelAndView errorModelAndView = new ModelAndView(LookupStrings.RECORD_NOT_FOUND_PAGE);
        errorModelAndView.addObject(LookupStrings.ZDB_ID, zdbID);
        return errorModelAndView;


    }

}