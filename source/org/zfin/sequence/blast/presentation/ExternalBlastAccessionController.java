package org.zfin.sequence.blast.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.presentation.BlastBean;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.Sequence;
import org.zfin.sequence.blast.Database;
import org.zfin.sequence.blast.MultipleBlastServerService;

/**
 * Processes a sequence in order to send the post to the appropriate external blast database.
 * <p/>
 * To process the request we need:
 * - database abbreviation to post against
 * - sequence from the database
 * <p/>
 * To get the sequence from the database we need:
 * - accession
 * - reference database (to get the blast database)
 * <p/>
 * To get the above, we can use a dblink, an accession_bk, an acccession (if there is only one entry, which their usually is) + (refDB)
 * <p/>
 * Request must have accession, refDB zdbID, blastDB zdbID.
 */
@Controller
public class ExternalBlastAccessionController extends AbstractExternalBlastController{

    @RequestMapping("/blast/external-blast")
    protected String showExternalBlast(@RequestParam(required = false) String accession,
                                          @RequestParam(required = false) String refDB,
                                          @RequestParam(required = false) String blastDB,
                                          @ModelAttribute("formBean") BlastBean blastBean) throws Exception {


        ReferenceDatabase referenceDatabase = (ReferenceDatabase) HibernateUtil.currentSession().get(ReferenceDatabase.class, refDB);
        Database database = (Database) HibernateUtil.currentSession().get(Database.class, blastDB);

        blastBean.setDatabase(database);
        blastBean.setSequences(MultipleBlastServerService.getSequencesForAccessionAndReferenceDBs(accession, referenceDatabase));


        if (CollectionUtils.isEmpty(blastBean.getSequences())) {
            return "external-blast.page";
        }

        if (blastBean.getSequences().size() == 2) {
            logger.fatal("2 sequences retreived for [" + accession + "] refDB [" + refDB + "]");
        }

        Sequence sequence = blastBean.getSequence();
        blastBean.setHiddenProperties(getHiddenVariables(sequence,database,referenceDatabase.isShortSequence()));
        return "external-blast.page";
    }

    private static Logger logger = LogManager.getLogger(ExternalBlastAccessionController.class);

}