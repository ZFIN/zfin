package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.zfin.antibody.Antibody;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.MergeService;
import org.zfin.marker.Transcript;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
        String type = zdbIDToDelete.substring(4, 8);

        Marker markerToDelete = null;

        if (type.startsWith("ATB") || type.startsWith("GEN"))  {

            markerToDelete = RepositoryFactory.getMarkerRepository().getMarkerByID(formBean .getZdbIDToDelete());

            formBean.setMarkerToDelete(markerToDelete);
    //        model.addAttribute("markerToDeleteId", markerToDelete.getZdbID());
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, markerToDelete.getAbbreviation());
        }
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
        Marker markerTobeMerged = formBean.getMarkerToDelete();
        if (markerTobeMerged == null) {
            markerTobeMerged = RepositoryFactory.getMarkerRepository().getMarkerByID(formBean .getZdbIDToDelete());
            formBean.setMarkerToDelete(markerTobeMerged);
        }
        if (markerTobeMerged.isInTypeGroup(Marker.TypeGroup.ATB) || markerTobeMerged.isInTypeGroup(Marker.TypeGroup.GENE)) {
            Marker markerToDelete = RepositoryFactory.getMarkerRepository().getMarkerByID(formBean.getZdbIDToDelete()) ;
            formBean.setMarkerToDelete(markerToDelete);
            // get abbrev
            Marker markerToMergeInto = RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(formBean.getMarkerToMergeIntoViewString());
            formBean.setMarkerToMergeInto(markerToMergeInto);

            if (markerToMergeInto == null && markerTobeMerged.isInTypeGroup(Marker.TypeGroup.ATB) ) {
                Antibody antibodyToMergeInto = RepositoryFactory.getAntibodyRepository().getAntibodyByName(formBean.getMarkerToMergeIntoViewString());
                if (antibodyToMergeInto == null) {
                    result.rejectValue(null, "nocode", new String[]{formBean.getMarkerToMergeIntoViewString()}, "Bad antibody name [{0}]");
                }
            }

            if (markerTobeMerged.isInTypeGroup(Marker.TypeGroup.ATB))
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
        }

        return "marker/merge-marker-finish.page";
    }

    // looks up gene to be merged into
    @RequestMapping(value = "/find-gene-to-merge-into", method = RequestMethod.GET)
    public
    @ResponseBody
    List<TargetGeneLookupEntry> lookupGeneToMergeInto(@RequestParam("term") String lookupString) {
           return RepositoryFactory.getMarkerRepository().getTargetGenesWithNoTranscriptForString(lookupString);
    }

    @RequestMapping(value = "/get-transcripts-for-geneId", method = RequestMethod.GET)
    public
    @ResponseBody
    List<TranscriptPresentation> getTranscriptsForGene(@RequestParam("geneZdbId") String geneZdbId) {
        return RepositoryFactory.getMarkerRepository().getTranscriptsForGeneId(geneZdbId);
    }
}
