package org.zfin.construct.repository;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.zfin.construct.ConstructComponent;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.ConstructRelationship;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

/**
 * Basic repository class to handle fish searches against a database.
 */
@Repository
public class HibernateConstructRepository implements ConstructRepository {

    private static Logger logger = LogManager.getLogger(org.zfin.construct.repository.HibernateConstructRepository.class);
    private static PublicationRepository pr = RepositoryFactory.getPublicationRepository();

    public ConstructRelationship getConstructRelationship(ConstructCuration marker1, Marker marker2, ConstructRelationship.Type type) {
        Session session = currentSession();
        String hql = """
            from ConstructRelationship
            where construct = :construct
            AND marker = :marker
            AND type = :type
            """;
        Query<ConstructRelationship> query = session.createQuery(hql, ConstructRelationship.class);
        query.setParameter("construct", marker1);
        query.setParameter("marker", marker2);
        query.setParameter("type", type);
        return query.uniqueResult();

    }

    public ConstructRelationship getConstructRelationshipByID(String zdbID) {
        Session session = currentSession();
        return session.get(ConstructRelationship.class, zdbID);
    }


    public List<ConstructRelationship> getConstructRelationshipsByPublication(String publicationZdbID) {
        List<ConstructRelationship.Type> constructRelationshipList = new ArrayList<>();
        constructRelationshipList.add(ConstructRelationship.Type.PROMOTER_OF);
        constructRelationshipList.add(ConstructRelationship.Type.CODING_SEQUENCE_OF);
        constructRelationshipList.add(ConstructRelationship.Type.CONTAINS_REGION);

        Session session = currentSession();
        String hql = "select distinct cmr from ConstructRelationship as cmr, " +
                     "PublicationAttribution as attribution " +
                     "where  attribution.dataZdbID = cmr.zdbID AND " +
                     "cmr.type in (:constructRelationshipType)AND " +
                     "attribution.publication.zdbID = :pubID ";

        Query<ConstructRelationship> query = session.createQuery(hql, ConstructRelationship.class);
        query.setParameter("pubID", publicationZdbID);
        query.setParameterList("constructRelationshipType", constructRelationshipList);
        List<ConstructRelationship> constructRelationships = query.list();
        constructRelationships.sort(Comparator.comparing(o -> o.getConstruct().getName()));
        return constructRelationships;
    }

    @Override
    public void addConstructRelationships(Set<Marker> promMarker, Set<Marker> codingMarker, ConstructCuration construct, String pubID) {
        //      HibernateUtil.createTransaction();

        if (!promMarker.isEmpty()) {
            for (Marker promMarkers : promMarker) {
                ConstructRelationship cmRel = getConstructRelationship(construct, promMarkers, ConstructRelationship.Type.PROMOTER_OF);
                if (cmRel == null) {
                    ConstructRelationship promMRel = new ConstructRelationship();
                    promMRel.setConstruct(construct);
                    promMRel.setMarker(promMarkers);
                    promMRel.setType(ConstructRelationship.Type.PROMOTER_OF);
                    currentSession().save(promMRel);
                    addConstructRelationshipAttribution(promMRel, pr.getPublication(pubID), construct);
                }
                // ir.insertRecordAttribution(promMRel.getZdbID(),pubID);

            }
        }
        if (!codingMarker.isEmpty()) {
            for (Marker codingMarkers : codingMarker) {
                ConstructRelationship cmRel = getConstructRelationship(construct, codingMarkers, ConstructRelationship.Type.CODING_SEQUENCE_OF);
                if (cmRel == null) {
                    ConstructRelationship codingRel = new ConstructRelationship();
                    codingRel.setConstruct(construct);
                    codingRel.setMarker(codingMarkers);
                    codingRel.setType(ConstructRelationship.Type.CODING_SEQUENCE_OF);
                    currentSession().save(codingRel);
                    addConstructRelationshipAttribution(codingRel, pr.getPublication(pubID), construct);
                }
                //    ir.insertRecordAttribution(codingRel.getZdbID(),pubID);

                //

            }
        }
        currentSession().flush();
        //       flushAndCommitCurrentSession();
    }

    @Override
    public void removeConstructRelationships(Set<Marker> promMarkers, Set<Marker> codingMarkers, ConstructCuration construct, String pubID) {
        Marker constructMarker = getMarkerRepository().getMarkerByID(construct.getZdbID());
        List<String> zdbIDsToDelete = new ArrayList<>(); //deleteActiveDataByZdbID
        if (!promMarkers.isEmpty()) {
            for (Marker promMarker : promMarkers) {
                ConstructRelationship cmRel = getConstructRelationship(construct, promMarker, ConstructRelationship.Type.PROMOTER_OF);
                if (cmRel != null) {
                    zdbIDsToDelete.add(cmRel.getZdbID());
                }
                MarkerRelationship mRel = getMarkerRepository().getMarkerRelationship(constructMarker, promMarker, MarkerRelationship.Type.PROMOTER_OF);
                if (mRel != null) {
                    zdbIDsToDelete.add(mRel.getZdbID());
                }
            }
        }

        if (!codingMarkers.isEmpty()) {
            for (Marker codingMarker : codingMarkers) {
                ConstructRelationship cmRel = getConstructRelationship(construct, codingMarker, ConstructRelationship.Type.CODING_SEQUENCE_OF);
                if (cmRel != null) {
                    zdbIDsToDelete.add(cmRel.getZdbID());
                }
                MarkerRelationship mRel = getMarkerRepository().getMarkerRelationship(constructMarker, codingMarker, MarkerRelationship.Type.CODING_SEQUENCE_OF);
                if (mRel != null) {
                    zdbIDsToDelete.add(mRel.getZdbID());
                }
            }
        }
        getInfrastructureRepository().deleteActiveDataByZdbID(zdbIDsToDelete);
    }

    public void addConstructPub(ConstructCuration construct, Publication publication) {
        if (publication == null)
            throw new RuntimeException("Cannot attribute this marker with a blank pub.");

        String markerZdbID = construct.getZdbID();
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        RecordAttribution recordAttribution = ir.getRecordAttribution(markerZdbID, publication.getZdbID(), RecordAttribution.SourceType.STANDARD);

        // only add the publication when it is not there
        if (recordAttribution == null) {
            PublicationAttribution pa = new PublicationAttribution();
            pa.setDataZdbID(markerZdbID);
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            pa.setPublication(publication);
            Set<PublicationAttribution> pubAttrbs = new HashSet<>();
            pubAttrbs.add(pa);
            Marker mrkr = new Marker();
            mrkr.setPublications(pubAttrbs);
            currentSession().save(pa);
        }
    }

    public ConstructCuration getConstructByID(String zdbID) {
        Session session = currentSession();
        return session.get(ConstructCuration.class, zdbID);
    }

    public ConstructCuration getConstructByName(String conName) {
        Session session = currentSession();
        return session.get(ConstructCuration.class, conName);
    }

    public List<Marker> getAllConstructs() {
        List<String> types = new ArrayList<>();

        types.add(Marker.Type.TGCONSTRCT.name());
        types.add(Marker.Type.GTCONSTRCT.name());
        types.add(Marker.Type.PTCONSTRCT.name());
        types.add(Marker.Type.ETCONSTRCT.name());


        String hql = "select m from Marker m  where m.markerType.name in (:types) ";
        Query<Marker> query = HibernateUtil.currentSession().createQuery(hql, Marker.class);
        query.setParameterList("types", types);
        return query.list();

    }

    @Override
    public void createConstruct(ConstructCuration construct, Publication pub, Person loggedInUser) {
        if (construct.getName() == null)
            throw new RuntimeException("Cannot create a new construct without a name.");
        if (construct == null)
            throw new RuntimeException("No construct object provided.");
        if (construct.getName() == null)
            throw new RuntimeException("Cannot create a new construct without a name.");
        if (construct.getConstructType() == null)
            throw new RuntimeException("Cannot create a new construct without a type.");
        if (pub == null)
            throw new RuntimeException("Cannot create a new construct without a publication.");

        if (loggedInUser == null) {
            loggedInUser = ProfileService.getCurrentSecurityUser();
        }

        construct.setOwner(loggedInUser);
        if (!construct.getOwner().getAccountInfo().getRoot())
            throw new RuntimeException("Non-root user cannot create a construct");
        currentSession().save(construct);
        // Need to flush here to make the trigger fire as that will
        // create a MarkerHistory record needed.
        //   currentSession().flush();

        //add publication to attribution list.
        RepositoryFactory.getInfrastructureRepository().insertRecordAttribution(construct.getZdbID(), pub.getZdbID());

        // run procedure for fast search table
    }

    @Override
    public void updateConstructName(String constructZdbID, String newName) {
        ConstructCuration existingConstruct = getConstructByID(constructZdbID);
        if (existingConstruct == null) {
            throw new RuntimeException("Cannot update construct name for construct with zdbID: " + constructZdbID + " because it does not exist.");
        }
        existingConstruct.setName(newName);
        currentSession().update(existingConstruct);

        Marker existingMarker = getMarkerRepository().getMarkerByID(constructZdbID);
        existingMarker.setName(newName);
        existingMarker.setAbbreviation(newName);
        currentSession().update(existingMarker);
    }


    @Override
    public void createConstruct(ConstructCuration construct, Publication pub) {
        createConstruct(construct, pub, null);
    }

    @Override
    public List<ConstructComponent> getConstructComponentsByConstructZdbId(String constructZdbId) {
        Session session = HibernateUtil.currentSession();
        Query<ConstructComponent> criteria = session.createQuery(
                """
                from ConstructComponent
                where constructZdbID = :constructZdbID
                order by componentOrder
                """,
                ConstructComponent.class);
        criteria.setParameter("constructZdbID", constructZdbId);
        return criteria.list();
    }

    @Override
    public List<ConstructComponent> getConstructComponentsByComponentID(String componentZdbID) {
        Session session = HibernateUtil.currentSession();
        Query<ConstructComponent> query = session.createQuery("from ConstructComponent where componentZdbID = :componentZdbID", ConstructComponent.class);
        query.setParameter("componentZdbID", componentZdbID);
        return query.list();
    }

    public void addConstructRelationshipAttribution(ConstructRelationship cmrel, Publication attribution, ConstructCuration construct) {

        String attributionZdbID = attribution.getZdbID();
        String relZdbID = cmrel.getZdbID();

        if (attributionZdbID.equals(""))
            throw new RuntimeException("Cannot attribute this alias with a blank pub.");

        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        RecordAttribution recordAttribution = ir.getRecordAttribution(relZdbID, attributionZdbID, RecordAttribution.SourceType.STANDARD);

        // only add the publication when it is not there
        if (recordAttribution == null) {
            PublicationAttribution pa = new PublicationAttribution();
            pa.setSourceZdbID(attributionZdbID);
            pa.setDataZdbID(relZdbID);
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            Publication publication = RepositoryFactory.getPublicationRepository().getPublication(attributionZdbID);
            pa.setPublication(publication);
            currentSession().save(pa);
            currentSession().refresh(cmrel);
            addConstructPub(construct, publication);
        }
        /*/change to construct signature
        Marker marker= getMarkerRepository().getMarkerByID(construct.getZdbID());
        ir.insertUpdatesTable(marker, "", "new attribution, construct relationship: " + cmrel.getZdbID() + " with pub: " + attributionZdbID, attributionZdbID, "");*/
    }
}
