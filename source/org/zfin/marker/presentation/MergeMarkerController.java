package org.zfin.marker.presentation;

import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.zfin.antibody.Antibody;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.MergeService;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Note that this is only for merging markers and does not handle genotypes or features.
 */
public class MergeMarkerController extends SimpleFormController{

    @Override
    protected void onBind(HttpServletRequest request, Object command, BindException errors) throws Exception {
        MergeBean mergeBean = (MergeBean) command ;
        Marker markerToDelete = RepositoryFactory.getMarkerRepository().getMarkerByID(mergeBean.getZdbIDToDelete());
        mergeBean.setMarkerToDelete(markerToDelete);

        Marker markerToMergeInto = RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(mergeBean.getMarkerToMergeIntoViewString()) ;
        mergeBean.setMarkerToMergeInto(markerToMergeInto) ;

        if(markerToMergeInto==null){
            Antibody antibodyToMergeInto = RepositoryFactory.getAntibodyRepository().getAntibodyByName(mergeBean.getMarkerToMergeIntoViewString()) ;
            if(antibodyToMergeInto==null){
                errors.rejectValue(null,"nocode",new String[]{mergeBean.getMarkerToMergeIntoViewString()},"Bad antibody name [{0}]");
            }
        }
    }

    @Override
    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
        MergeBean mergeBean = (MergeBean) command ;
        Marker markerToDelete = RepositoryFactory.getMarkerRepository().getMarkerByID(mergeBean.getZdbIDToDelete());
        mergeBean.setMarkerToDelete(markerToDelete);
        Map modelMap = new ModelMap(getCommandName(),mergeBean) ;
        modelMap.put(LookupStrings.DYNAMIC_TITLE, markerToDelete.getAbbreviation());
        return  modelMap ;
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        MergeBean mergeBean = (MergeBean) command ;

        Marker markerToDelete = mergeBean.getMarkerToDelete();
        Marker markerToMergeInto = mergeBean.getMarkerToMergeInto();

        try {
            HibernateUtil.createTransaction();
            MergeService.mergeMarker(markerToDelete,markerToMergeInto) ;
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            logger.error("Error merging marker ["+markerToDelete+"] into ["+markerToMergeInto+"]",e);
            HibernateUtil.rollbackTransaction();
            throw e;
        }
//        finally {
//            HibernateUtil.rollbackTransaction();
//        }

        ModelAndView modelAndView = new ModelAndView(getSuccessView()) ;
        modelAndView.addObject(getCommandName(),mergeBean) ;
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, markerToDelete.getAbbreviation());
        return modelAndView;
    }
}
