package org.zfin.marker.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.service.TranscriptService;

@Controller
@RequestMapping("/marker")
public class RelatedTranscriptsController {


    @RequestMapping(value = "/related-transcripts")
    protected String handleRequestInternal(Model model
            , @RequestParam("zdbID") String zdbID
    ) throws Exception {

        TranscriptBean transcriptBean = new TranscriptBean();

        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerOrReplacedByID(zdbID);
        if (gene == null) {
            model.addAttribute("zdbID", zdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        transcriptBean.setMarker(gene);

        transcriptBean.setRelatedTranscriptDisplay(TranscriptService.getRelatedTranscriptsForGene(gene));

        model.addAttribute(LookupStrings.FORM_BEAN, transcriptBean);

        return "marker/related-transcripts";
    }
}
