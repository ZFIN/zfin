package org.zfin.gwt.root.server;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.CuratorSessionDTO;
import org.zfin.gwt.root.server.rpc.ZfinRemoteServiceServlet;
import org.zfin.gwt.root.ui.SessionSaveService;
import org.zfin.people.CuratorSession;
import org.zfin.people.Person;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.Iterator;
import java.util.List;


/**
 */
@SuppressWarnings({"GwtServiceNotRegistered"})
public class SessionSaveServiceImpl extends ZfinRemoteServiceServlet implements SessionSaveService {

    private Logger log = Logger.getLogger(SessionSaveServiceImpl.class);
    private static ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();
    private static final String STAGE_SELECTOR_MODE = "StageSelectorMode";

    public synchronized void saveCuratorUpdate(List<CuratorSessionDTO> curatorSessionUpdateList) {
        if (curatorSessionUpdateList.isEmpty()) {
            log.debug("trying to save empty list");
            return;
        }
        Iterator<CuratorSessionDTO> iterator = curatorSessionUpdateList.iterator();
        log.debug("updating " + curatorSessionUpdateList.size() + " session objects");
        while (iterator.hasNext()) {
            updateCuratorSession(iterator.next());
        }
        log.debug("updated " + curatorSessionUpdateList.size() + " session objects");
    }

    /**
     * Update a single curator session object.
     *
     * @param curatorSessionUpdate Curator session DTO
     */
    public void updateCuratorSession(CuratorSessionDTO curatorSessionUpdate) {

        Person curator = Person.getCurrentSecurityUser();
        if (curator == null)
            throw new RuntimeException("Not logged in.");
        String personZdbID = curator.getZdbID();
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
        try {
            HibernateUtil.createTransaction();
            if (realCS != null) {
                log.debug("found cs object with id: " + realCS.getID() + " and value: " + realCS.getValue()
                        + " updating value to: " + value);
                realCS.setValue(value);

            } else {
                log.debug("wasn't an existing curator session object, saving it...");
                profileRepository.createCuratorSession(personZdbID, publicationZdbID, field, value);
            }
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (HibernateException e) {
            HibernateUtil.rollbackTransaction();
            e.printStackTrace();
        }


    }

    /**
     * Retrieve session variable box size.
     *
     * @param publicationID publication
     * @param boxDivID      name of div element
     */
    public CuratorSessionDTO readBoxSizeFromSession(String publicationID, String boxDivID) {
        CuratorSession session = profileRepository.getCuratorSession(publicationID, boxDivID, CuratorSession.Attribute.MUTANT_DISPLAY_BOX);
        return DTOConversionService.convertToCuratorSessionDTO(session);
    }

    /**
     * Retrieve the stage selector mode.
     * single selection mode or multi selection mode.
     *
     * @param publicationID publication ID
     */
    public boolean isStageSelectorSingleMode(String publicationID) {

        String key = createSessionKey(STAGE_SELECTOR_MODE, publicationID);
        Object value = getServletContext().getAttribute(key);
        return value == null ? true : (Boolean) value;
    }

    /**
     * Set the stage selector mode.
     *
     * @param isSingleMode  single - multi
     * @param publicationID publication
     */
    public void setStageSelectorSingleMode(boolean isSingleMode, String publicationID) {
        String key = createSessionKey(STAGE_SELECTOR_MODE, publicationID);
        getServletContext().setAttribute(key, isSingleMode);
    }

    private String createSessionKey(String prefix, String publicationID) {
        Person person = Person.getCurrentSecurityUser();
        if (person == null)
            throw new RuntimeException("Not logged in. No authenticated user found in session.");
        return prefix + ": " + person.getZdbID() + ": " + publicationID;
    }


}