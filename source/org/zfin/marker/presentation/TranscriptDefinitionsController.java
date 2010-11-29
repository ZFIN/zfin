package org.zfin.marker.presentation;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.sequence.TranscriptService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TranscriptDefinitionsController extends AbstractController {


    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) throws Exception {

        TranscriptBean transcriptBean = new TranscriptBean();


        transcriptBean.setTranscriptTypeStatusDefinitionList(TranscriptService.getAllTranscriptTypeStatusDefinitions());
        transcriptBean.setTranscriptTypeList(TranscriptService.getAllTranscriptTypes());

        ModelAndView modelAndView = new ModelAndView("marker/transcript-definitions.page");
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, "Transcript Type and Status Definitions");
        modelAndView.addObject(LookupStrings.FORM_BEAN, transcriptBean);

        return modelAndView;

    }
}
