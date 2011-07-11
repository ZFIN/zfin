package org.zfin.sequence.blast.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.servlet.ModelAndView;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.presentation.BlastBean;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.Sequence;
import org.zfin.sequence.blast.Database;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

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
public class ExternalBlastSequenceController extends AbstractExternalBlastController{

    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) throws Exception {
        // extract parameters
        String accession = httpServletRequest.getParameter(LookupStrings.ACCESSION);
        String blastDBZdbID = httpServletRequest.getParameter(LookupStrings.BLAST_DB);
        Database database ;
        if(blastDBZdbID.startsWith("ZDB-")){
            database = (Database) HibernateUtil.currentSession().get(Database.class, blastDBZdbID);
        }
        else {
            database = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.getType(blastDBZdbID));
        }

        if(database==null){
            blastDBZdbID = "ZDB-BLASTDB-090929-26";
            database = (Database) HibernateUtil.currentSession().get(Database.class, blastDBZdbID);
        }


        BlastBean blastBean = new BlastBean();
        blastBean.setDatabase(database);
        List<Sequence> sequenceList = new ArrayList<Sequence>();
        Sequence sequence = new Sequence();
        sequence.setData(accession);
        sequenceList.add(sequence);
        blastBean.setSequences(sequenceList);


        ModelAndView modelAndView = new ModelAndView("external-blast.page");
        modelAndView.addObject(LookupStrings.FORM_BEAN, blastBean);

        if (CollectionUtils.isEmpty(blastBean.getSequences())) {
            return modelAndView;
        }

        blastBean.setHiddenProperties(getHiddenVariables(sequence,database,true));

        return modelAndView;
    }
}