package org.zfin.marker.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.sequence.service.TranscriptService;

@Controller
public class TranscriptDefinitionsController {


//    @RequestMapping(value = "/transcript-definitions")
//    public String getTranscriptDefinitions(Model model) throws Exception {
//
//        TranscriptBean transcriptBean = new TranscriptBean();
//
//
//        transcriptBean.setTranscriptTypeStatusDefinitionList(TranscriptService.getAllTranscriptTypeStatusDefinitions());
//        transcriptBean.setTranscriptTypeList(TranscriptService.getAllTranscriptTypes());
//
//        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Transcript Type and Status Definitions");
//        model.addAttribute(LookupStrings.FORM_BEAN, transcriptBean) ;
//
//        return "marker/transcript-definitions.page";
//
//    }

    @RequestMapping(value = "/transcript-types")
    public String getTranscriptTypes(Model model) throws Exception {
        TranscriptBean transcriptBean = new TranscriptBean();
        transcriptBean.setTranscriptTypeList(TranscriptService.getAllTranscriptTypes());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Transcript Types");
        model.addAttribute(LookupStrings.FORM_BEAN, transcriptBean) ;
        return "marker/transcript-types.insert";
    }


    @RequestMapping(value = "/transcript-statuses")
    public String getTranscriptStatuses(Model model) throws Exception {
        TranscriptBean transcriptBean = new TranscriptBean();
        transcriptBean.setTranscriptTypeStatusDefinitionList(TranscriptService.getAllTranscriptTypeStatusDefinitions());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Transcript Statuses");
        model.addAttribute(LookupStrings.FORM_BEAN, transcriptBean) ;
        return "marker/transcript-statuses.insert";
    }
}
