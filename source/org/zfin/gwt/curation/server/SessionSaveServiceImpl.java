package org.zfin.gwt.curation.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.dto.CuratorSessionDTO;
import org.zfin.gwt.curation.ui.SessionSaveService;
import org.zfin.people.CuratorSession;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.Iterator;
import java.util.List;


/**
 */
public class SessionSaveServiceImpl extends RemoteServiceServlet implements SessionSaveService {
    private final Logger log = Logger.getLogger(SessionSaveServiceImpl.class);
    private static ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();

    public synchronized void saveCuratorUpdate(List<CuratorSessionDTO> curatorSessionUpdateList) {
        if (curatorSessionUpdateList.isEmpty()) {
            log.debug("trying to save empty list");
            return;
        }
        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Iterator<CuratorSessionDTO> iterator = curatorSessionUpdateList.iterator();
            log.debug("updating " + curatorSessionUpdateList.size() + " session objects");
            while (iterator.hasNext()) {
                saveCuratorUpdate(iterator.next());
            }
            log.debug("updated " + curatorSessionUpdateList.size() + " session objects");
            tx.commit();
        } catch (Exception e) {
            try {
                tx.rollback();
            } catch (HibernateException he) {
                log.error("Error during roll back of transaction", he);
            }
            log.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }
    }

    private void saveCuratorUpdate(CuratorSessionDTO curatorSessionUpdate) {

        String personZdbID = curatorSessionUpdate.getCuratorZdbID();
        String publicationZdbID = curatorSessionUpdate.getPublicationZdbID();
        String field = curatorSessionUpdate.getField();
        String value = curatorSessionUpdate.getValue();

        log.debug("curator: " + personZdbID);
        log.debug("publication: " + publicationZdbID);
        log.debug("field: " + field);
        log.debug("value: " + value);

        CuratorSession realCS = profileRepository.getCuratorSession(personZdbID, publicationZdbID, field);

        //if the repository method doesn't come back with a curator_session object, one doesn't exist
        //yet, so just make one
        if (realCS != null) {
            log.debug("found cs object with id: " + realCS.getID() + " and value: " + realCS.getValue()
                    + " updating value to: " + value);
            realCS.setValue(value);

        } else {
            log.debug("wasn't an existing curator session object, saving it...");
            profileRepository.createCuratorSession(personZdbID, publicationZdbID, field, value);
        }


    }


}