package org.zfin.marker.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.antibody.Antibody;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.marker.MergeService;
import org.zfin.publication.CurationPresentation;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Attempts to delete a marker and lands on a splash page to indicate success/failure.
 * <p/>
 * 1. If there is a problem durint the
 */
public class DeleteMarkerController extends AbstractCommandController {

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        DeleteBean deleteBean = (DeleteBean) command;

        ModelAndView modelAndView = new ModelAndView("marker-delete.page");
        modelAndView.addObject(getCommandName(), deleteBean);
        modelAndView.addObject("errors", errors);


        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(deleteBean.getZdbIDToDelete());
        deleteBean.setMarkerToDelete(marker);

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

                ObjectError error = new ObjectError(getCommandName(), new String[]{}, new String[]{},
                        "Antibody can not be deleted, being used in " + numExpression + " expression records in " + pubs.size() + " pubs: <br>" + argString);
                errors.addError(error);
                return modelAndView;
            }
        }


        try {
            HibernateUtil.createTransaction();
            MergeService.deleteMarker(marker);
            HibernateUtil.flushAndCommitCurrentSession();
        }
        catch (Exception e) {
            logger.error("Failed to delete marker: " + deleteBean, e);
            HibernateUtil.rollbackTransaction();
            ObjectError error = new ObjectError(getCommandName(), new String[]{}, new String[]{}, "Failed to delete marker: " + deleteBean + "<br>" + e.getMessage());
            errors.addError(error);
        }

        return modelAndView;
    }
}
