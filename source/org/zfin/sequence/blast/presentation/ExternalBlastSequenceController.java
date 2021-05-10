package org.zfin.sequence.blast.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.presentation.BlastBean;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.Sequence;
import org.zfin.sequence.blast.Database;

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
 * To get the above, we can use a dblink, an accession_bk, an accession (if there is only one entry, which their usually is) + (refDB)
 * <p/>
 * Request must have accession, refDB zdbID, blastDB zdbID.
 */
@Controller
public class ExternalBlastSequenceController extends AbstractExternalBlastController{

    @RequestMapping("/blast/blast-with-sequence")
    protected String showBlastDefinitions(@RequestParam(required = false) String accession,
                                          @RequestParam(required = false) String blastDB,
                                          @ModelAttribute("formBean") BlastBean blastBean) throws Exception {
        Database database ;
        if(blastDB.startsWith("ZDB-")){
            database = (Database) HibernateUtil.currentSession().get(Database.class, blastDB);
        }
        else {
            database = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.getType(blastDB));
        }

        if(database==null){
            blastDB = "ZDB-BLASTDB-090929-26";
            database = (Database) HibernateUtil.currentSession().get(Database.class, blastDB);
        }

        blastBean.setDatabase(database);
        List<Sequence> sequenceList = new ArrayList<>();
        Sequence sequence = new Sequence();
        sequence.setData(accession);
        sequenceList.add(sequence);
        blastBean.setSequences(sequenceList);


        if (CollectionUtils.isEmpty(blastBean.getSequences())) {
            return "blast/external_blast_redirect";
        }

        blastBean.setHiddenProperties(getHiddenVariables(sequence,database,true));

        return "blast/external_blast_redirect";
    }
}