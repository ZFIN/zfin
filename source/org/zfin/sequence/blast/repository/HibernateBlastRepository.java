/**
 *  Class BlastRepository.
 */
package org.zfin.sequence.blast.repository ;

import org.zfin.sequence.blast.Hit;
import org.zfin.sequence.reno.RunCandidate;
import org.zfin.framework.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Query;

public class HibernateBlastRepository implements BlastRepository {

    public Hit getBestHit(RunCandidate rc) {
        Session session = HibernateUtil.currentSession();
        //Query query = session.createQuery ("from ");
          String hsqlString = "select h from Hit h, RunCandidate rc, Query q where " +
                  "h.query.zdbID=q.zdbID" +
                  "q.runCandidate.zdbID=rc.zdbID" +
                  "order by h.score " ;
        Query query = session.createQuery(hsqlString) ;
        query.setMaxResults(1) ;

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}


