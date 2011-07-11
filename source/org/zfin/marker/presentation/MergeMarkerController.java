package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.antibody.Antibody;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.MergeService;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Note that this is only for merging markers and does not handle genotypes or features.
 */
@Controller
public class MergeMarkerController {

    private MergeMarkerValidator validator = new MergeMarkerValidator();
    private Logger logger = Logger.getLogger(MergeMarkerController.class);

    protected void onBind(HttpServletRequest request, Object command, BindException errors) throws Exception {
        MergeBean mergeBean = (MergeBean) command;
        Marker markerToDelete = RepositoryFactory.getMarkerRepository().getMarkerByID(mergeBean.getZdbIDToDelete());
        mergeBean.setMarkerToDelete(markerToDelete);

        Marker markerToMergeInto = RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(mergeBean.getMarkerToMergeIntoViewString());
        mergeBean.setMarkerToMergeInto(markerToMergeInto);

        if (markerToMergeInto == null) {
            Antibody antibodyToMergeInto = RepositoryFactory.getAntibodyRepository().getAntibodyByName(mergeBean.getMarkerToMergeIntoViewString());
            if (antibodyToMergeInto == null) {
                errors.rejectValue(null, "nocode", new String[]{mergeBean.getMarkerToMergeIntoViewString()}, "Bad antibody name [{0}]");
            }
        }
    }

    @RequestMapping( value = "/merge",method = RequestMethod.GET)
    protected String getView(
            Model model
            ,@RequestParam("zdbIDToDelete") String zdbIDToDelete
            ,@ModelAttribute("formBean") MergeBean formBean
            ,BindingResult result
    ) throws Exception {
        Marker markerToDelete = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbIDToDelete);
        formBean.setMarkerToDelete(markerToDelete);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, markerToDelete.getAbbreviation());
        return "marker/merge-marker.page";
    }

    @RequestMapping( value = "/merge",method = RequestMethod.POST)
    protected String mergeMarkers(
            Model model
            ,@ModelAttribute("formBean") MergeBean formBean
//            ,@RequestParam("getZdbIDToDelete") String zdbIDToDelete
//            ,@RequestParam("markerToMergeIntoViewString") String markerToMergeIntoViewString
            ,BindingResult result
    ) throws Exception {

        Marker markerToDelete = RepositoryFactory.getMarkerRepository().getMarkerByID(formBean.getZdbIDToDelete()) ;
        formBean.setMarkerToDelete(markerToDelete);
        // get abbrev
        Marker markerToMergeInto = RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(formBean.getMarkerToMergeIntoViewString());
        formBean.setMarkerToMergeInto(markerToMergeInto);

        if (markerToMergeInto == null) {
            Antibody antibodyToMergeInto = RepositoryFactory.getAntibodyRepository().getAntibodyByName(formBean.getMarkerToMergeIntoViewString());
            if (antibodyToMergeInto == null) {
                result.rejectValue(null, "nocode", new String[]{formBean.getMarkerToMergeIntoViewString()}, "Bad antibody name [{0}]");
            }
        }

        validator.validate(formBean,result);

        if(result.hasErrors()){
            return getView(model,formBean.getZdbIDToDelete(),formBean,result);
        }

        try {
            HibernateUtil.createTransaction();
            MergeService.mergeMarker(markerToDelete, markerToMergeInto);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            logger.error("Error merging marker [" + markerToDelete + "] into [" + markerToMergeInto + "]", e);
            HibernateUtil.rollbackTransaction();
            result.reject("no lookup", "Error merging marker [" + markerToDelete + "] into [" + markerToMergeInto + "]:\n"+ e);
            return getView(model,formBean.getZdbIDToDelete(),formBean,result);
        }
//        finally {
//            HibernateUtil.rollbackTransaction();
//        }

        model.addAttribute(LookupStrings.FORM_BEAN, formBean );
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, markerToDelete.getAbbreviation());
        return "marker/merge-marker-finish.page";
    }
}
