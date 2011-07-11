package org.zfin.sequence.blast.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.servlet.ModelAndView;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.presentation.BlastBean;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.Sequence;
import org.zfin.sequence.blast.Database;
import org.zfin.sequence.blast.MultipleBlastServerService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class ExternalBlastAccessionController extends AbstractExternalBlastController{

    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) throws Exception {
        // extract parameters
        String accession = httpServletRequest.getParameter(LookupStrings.ACCESSION);
        String refDBZdbID = httpServletRequest.getParameter(LookupStrings.REF_DB);
        String blastDBZdbID = httpServletRequest.getParameter(LookupStrings.BLAST_DB);

        ReferenceDatabase referenceDatabase = (ReferenceDatabase) HibernateUtil.currentSession().get(ReferenceDatabase.class, refDBZdbID);
        Database database = (Database) HibernateUtil.currentSession().get(Database.class, blastDBZdbID);


        BlastBean blastBean = new BlastBean();
        blastBean.setDatabase(database);
        blastBean.setSequences(MultipleBlastServerService.getSequencesForAccessionAndReferenceDBs(accession, referenceDatabase));


        ModelAndView modelAndView = new ModelAndView("external-blast.page");
        modelAndView.addObject(LookupStrings.FORM_BEAN, blastBean);

        if (CollectionUtils.isEmpty(blastBean.getSequences())) {
            return modelAndView;
        }


        if (blastBean.getSequences().size() == 2) {
            logger.fatal("2 sequences retreived for[" + accession + "] refDB[" + refDBZdbID + "]");
        }

        Sequence sequence = blastBean.getSequence();

        blastBean.setHiddenProperties(getHiddenVariables(sequence,database,referenceDatabase.isShortSequence()));;

        return modelAndView;
    }
}