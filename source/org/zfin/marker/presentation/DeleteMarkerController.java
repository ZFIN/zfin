package org.zfin.marker.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.antibody.Antibody;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.marker.MergeService;
import org.zfin.publication.CurationPresentation;
import org.zfin.repository.RepositoryFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Attempts to delete a marker and lands on a splash page to indicate success/failure.
 * <p/>
 * 1. If there is a problem durint the
 */
@Controller
public class DeleteMarkerController {

    private Logger logger = Logger.getLogger(DeleteMarkerController.class);

    @RequestMapping(value ="/delete")
    public String deleteMarker(
            @RequestParam("zdbIDToDelete") String zdbIDToDelete
            ,  @ModelAttribute("formBean") DeleteBean formBean
            ) throws Exception {

        formBean.setZdbIDToDelete(zdbIDToDelete);

        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(formBean .getZdbIDToDelete());
        formBean.setMarkerToDelete(marker);

        // a bit of validation, maybe put somewhere else
        if (marker.isInTypeGroup(Marker.TypeGroup.ATB)) {
            Set<ExpressionExperiment> expressionExperiments = ((Antibody) marker).getAntibodyLabelings();
            if (CollectionUtils.isNotEmpty(expressionExperiments)) {
                int numExpression = expressionExperiments.size();
                Set<String> pubs = new HashSet<String>();
                for (ExpressionExperiment expressionExperiment : expressionExperiments) {
                    pubs.add(CurationPresentation.getLink(
                            expressionExperiment.getPublication(),
                            CurationPresentation.CurationTab.FX)
                    );
                }
                String argString = "";
                for (Iterator<String> iter = pubs.iterator(); iter.hasNext();) {
                    argString += iter.next();
                    argString += (iter.hasNext() ? "<br> " : "");
                }

                formBean.addError("Antibody can not be deleted, being used in " + numExpression + " expression records in " + pubs.size() + " pubs: <br>" + argString);
                return "marker/delete-marker.page";
            }
        }


        try {
            HibernateUtil.createTransaction();
            MergeService.deleteMarker(marker);
            HibernateUtil.flushAndCommitCurrentSession();
        }
        catch (Exception e) {
            logger.error("Failed to delete marker: " + formBean, e);
            HibernateUtil.rollbackTransaction();
            formBean.addError("Failed to delete marker: " + formBean  + "<br>" + e.getMessage());
        }

        return "marker/delete-marker.page";
    }
}
