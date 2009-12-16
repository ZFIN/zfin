package org.zfin.sequence.blast.presentation;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Transcript;
import org.zfin.marker.presentation.BlastBean;
import org.zfin.sequence.*;
import org.zfin.sequence.blast.MountedWublastBlastService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Displays blast sequences for
 */
public class DisplayBlastSequenceController extends AbstractController {

    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) throws Exception {
        // set base bean
        BlastBean blastBean = new BlastBean();

        String accession = httpServletRequest.getParameter(LookupStrings.ACCESSION);
        List<Sequence> sequences = MountedWublastBlastService.getInstance().
                getSequencesForAccessionAndDisplayGroup(
                        accession, DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE,
                        DisplayGroup.GroupName.DISPLAYED_PROTEIN_SEQUENCE);
        logger.info("transcriptviewcontroller # of seq: " + sequences.size());
        blastBean.setSequences(sequences);

        for (Sequence sequence : blastBean.getSequences()) {
            DBLink dbLink = sequence.getDbLink();
            if (dbLink.getDataZdbID().startsWith("ZDB-TSCRIPT")) {
                // ick . . would like to refact Transcript / TranscriptDBLink at some point
                blastBean.setTranscript((Transcript) ((TranscriptDBLink) dbLink).getTranscript());
            } else if (dbLink.getDataZdbID().startsWith("ZDB-GENE")) {
                blastBean.setGene(((MarkerDBLink) dbLink).getMarker());
            }
        }


        ModelAndView modelAndView = new ModelAndView("display-blast-sequence.page");
        modelAndView.addObject(LookupStrings.FORM_BEAN, blastBean);

        return modelAndView;
    }
}
