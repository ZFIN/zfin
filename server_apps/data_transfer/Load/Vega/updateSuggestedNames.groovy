#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import org.hibernate.Session
import org.hibernate.criterion.Restrictions
import org.zfin.framework.HibernateSessionCreator
import org.zfin.framework.HibernateUtil
import org.zfin.properties.ZfinProperties
import org.zfin.sequence.Accession
import org.zfin.sequence.reno.Candidate
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.zfin.sequence.reno.RunCandidate

ZfinProperties.init("${System.getenv()['SOURCEROOT']}/home/WEB-INF/zfin.properties")
new HibernateSessionCreator()

Session session = HibernateUtil.currentSession();
def transaction = session.beginTransaction()

Logger log = Logger.getLogger(updateSuggestedNames.class);
//Logger.getRootLogger().setLevel(Level.TRACE)
Logger.getRootLogger().setLevel(Level.INFO)
//Logger.getLogger(updateSuggestedNames.class).setLevel(Level.INFO)



session.createSQLQuery("""
select distinct cnd_zdb_id, cnd_suggested_name, accbk_defline, runcan_zdb_id, accbk_pk_id, accbk_acc_num
From candidate, run_candidate, run, blast_query, blast_hit, accession_bank
 where cnd_zdb_id = runcan_cnd_zdb_id
and run_zdb_id = runcan_run_zdb_id
and bqry_zdb_id = bhit_bqry_zdb_id
and bqry_runcan_Zdb_id = runcan_zdb_id
and  bqry_accbk_pk_id = accbk_pk_id
and run_name like 'Vega_2014_11_18%';
""").list().each { row ->

    String cndZdbId = row[0]
    String cndSuggestedName = row[1]
    String defline = row[2]
    String runCanZdbId = row[3]


    def newSuggestedName = suggestName(defline, cndSuggestedName)

    if (newSuggestedName != cndSuggestedName) {
        log.debug "---------------"
        log.debug row
        log.debug defline
        log.info "$cndZdbId: $cndSuggestedName => $newSuggestedName"

        Candidate olderCandidate = session.createCriteria(Candidate.class).add(Restrictions.eq("suggestedName",newSuggestedName)).uniqueResult()
    	Candidate candidate = session.createCriteria(Candidate.class).add(Restrictions.eq("zdbID",cndZdbId)).uniqueResult()
        if (candidate) {
            if (olderCandidate) {
                log.debug "Re-using old candidate"
                RunCandidate runCandidate = session.get(RunCandidate.class,runCanZdbId)
                runCandidate.candidate = olderCandidate
                session.save(runCandidate)
                if(candidate) { session.delete(candidate) }
            } else {
                log.debug "Updating candidate name"
                candidate.suggestedName = newSuggestedName
                session.save(candidate)
            }
        }

    }

}

if ("--commit" in args) {
    log.info "committing..."
    transaction.commit()
} else {
    log.info "rolling back..."
    transaction.rollback()
}




boolean startsWithAny(String input, List<String> values) {
    Boolean returnValue = false
    values.each { value ->
        if (input.startsWith(value)) { returnValue = true }
    }
    return returnValue
}

String suggestName(String defline, String candidateSuggestedName) {
    String possibleName = defline.tokenize("|")[4].tokenize()[1]

    if (geneIsNotKnown(defline) && startsWithAny(possibleName, ["BUSM","CH21","CH73","DKEY","RP71","ZFOS","CH10"])) {
        candidateSuggestedName = "si:" + possibleName.toLowerCase()[0..-5]
    }

    return candidateSuggestedName
}

Boolean geneIsNotKnown(String defline) {
    return !(defline.contains("ZDB-GENE"))
}


log.debug "success..?"