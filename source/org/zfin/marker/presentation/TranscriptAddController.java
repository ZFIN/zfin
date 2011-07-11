package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.TranscriptStatus;
import org.zfin.marker.TranscriptType;
import org.zfin.people.Person;
import org.zfin.sequence.service.TranscriptService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 */
@Controller
public class TranscriptAddController {

    private static Logger logger = Logger.getLogger(TranscriptAddController.class);

    public static final String LOOKUP_TRANSCRIPT_TYPES = "transcriptTypes";

    private TranscriptAddValidator validator=new TranscriptAddValidator();

    @RequestMapping( value = "/transcript-add",method = RequestMethod.GET)
    public String getView(
            Model model ,
            @ModelAttribute("formBean") TranscriptAddBean formBean
            ,BindingResult result
    ) {

        Map<String, String> types = new LinkedHashMap<String, String>();
        types.put("", "Choose Type");

        TranscriptType.Type[] transcriptTypes = TranscriptType.Type.values();
        for (TranscriptType.Type transcriptType : transcriptTypes) {
            types.put(transcriptType.toString(), transcriptType.toString());
        }

        Map<String, String> statuses = new LinkedHashMap<String, String>();
        statuses.put("Choose Status", "");

        TranscriptStatus.Status[] transcriptStatuses = TranscriptStatus.Status.values();
        for (TranscriptStatus.Status transcriptStatus : transcriptStatuses) {
            statuses.put(transcriptStatus.toString(), transcriptStatus.toString());
        }


        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Add Transcript");
        model.addAttribute("types", types);
        model.addAttribute("statuses", statuses);

        return "marker/transcript-add.page";
    }


    @RequestMapping( value = "/transcript-add",method = RequestMethod.POST)
    protected String addTranscript(Model model
            , @ModelAttribute("formBean") TranscriptAddBean formBean
            , BindingResult result) throws Exception {

        validator.validate(formBean, result);

        // create transcript
        if(result.hasErrors()){
            return getView(model, formBean,result);
        }

        try {
            formBean.setOwnerZdbID(Person.getCurrentSecurityUser().getZdbID());
            HibernateUtil.createTransaction();
            Marker marker = TranscriptService.createTranscript(formBean);
            String zdbID = marker.getZdbID();
            HibernateUtil.flushAndCommitCurrentSession();
            return "redirect:marker-edit?zdbID=" + zdbID;
        }
        catch (Exception e) {
            logger.error(e) ;
            HibernateUtil.rollbackTransaction();
            // todo: add some errors here
            result.reject("no lookup","Failed to add transcript: "+e.getMessage());
            return getView(model, formBean,result);
        }
    }

}