package org.zfin.marker.presentation;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.TranscriptService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RelatedTranscriptsController extends AbstractController {

    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) throws Exception {

        TranscriptBean transcriptBean = new TranscriptBean();

        String zdbID = httpServletRequest.getParameter(LookupStrings.ZDB_ID);
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        transcriptBean.setMarker(gene);

        transcriptBean.setRelatedTranscriptDisplay(TranscriptService.getRelatedTranscriptsForGene(gene));

        ModelAndView modelAndView = new ModelAndView("related-transcripts.page");
        modelAndView.addObject(LookupStrings.FORM_BEAN, transcriptBean);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, gene.getAbbreviation());

        return modelAndView;
    }
}
