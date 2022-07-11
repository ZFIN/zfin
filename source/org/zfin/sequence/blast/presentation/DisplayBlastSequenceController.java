package org.zfin.sequence.blast.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.presentation.BlastBean;
import org.zfin.sequence.*;
import org.zfin.sequence.blast.MountedWublastBlastService;

import java.util.List;

/**
 * Displays blast sequences for
 */
@Controller
public class DisplayBlastSequenceController {

    @RequestMapping("/blast/display-sequence")
    protected String downloadSequence(@RequestParam(required = false) String accession,
                                      @ModelAttribute("formBean") BlastBean blastBean) throws Exception {
        HibernateUtil.createTransaction();
        try {
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
                    blastBean.setTranscript(((TranscriptDBLink) dbLink).getTranscript());
                } else if (dbLink.getDataZdbID().startsWith("ZDB-GENE") || dbLink.getDataZdbID().contains("RNAG")) {
                    blastBean.setGene(((MarkerDBLink) dbLink).getMarker());
                }
            }
        } catch (Exception e) {
            logger.error("DisplayBlastSequenceController error: " + e.getMessage());
            HibernateUtil.rollbackTransaction();
            throw e;
        }
        HibernateUtil.flushAndCommitCurrentSession();
        return "blast/display_blast_sequence";
    }

    private Logger logger = LogManager.getLogger(DisplayBlastSequenceController.class);
}
