package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.antibody.Antibody;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Clone;
import org.zfin.marker.Transcript;
import org.zfin.repository.RepositoryFactory;

/**
 */
@Controller
public class MarkerEditController {

    private static Logger logger = Logger.getLogger(MarkerEditController.class);

    @RequestMapping("/marker-edit")
    public String getMarkerEdit(Model model
            ,@RequestParam("zdbID") String zdbID
    ) throws Exception {
        logger.info("zdbID: " + zdbID);

        MarkerBean markerBean = new MarkerBean();

        if (zdbID.startsWith("ZDB-TSCRIPT-")) {
            Transcript transcript = RepositoryFactory.getMarkerRepository().getTranscriptByZdbID(zdbID);
            if (transcript != null) {
                markerBean.setMarker(transcript);
                model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
                model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.TRANSCRIPT.getEditTitleString()+transcript.getAbbreviation());
                return "marker/transcript-edit.page";
            }
        }


        if (zdbID.startsWith("ZDB-ATB-")) {
            Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID(zdbID);
            if (antibody != null) {
                markerBean.setMarker(antibody);
                model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
                model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.ANTIBODY.getEditTitleString()+antibody.getAbbreviation());
                return "marker/antibody-edit.page";
            }
        }


        // handle things that mark to clone
        Clone clone = RepositoryFactory.getMarkerRepository().getCloneById(zdbID);
        if (clone != null) {
            markerBean.setMarker(clone);
            model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.CLONE.getEditTitleString()+clone.getAbbreviation());
            return "marker/clone-edit.page";
        }

        model.addAttribute(LookupStrings.ZDB_ID, zdbID);
        return LookupStrings.RECORD_NOT_FOUND_PAGE;


    }

}