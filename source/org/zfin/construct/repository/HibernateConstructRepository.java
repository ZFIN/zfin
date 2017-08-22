package org.zfin.construct.repository;


import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.zfin.construct.ConstructComponent;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.ConstructRelationship;
import org.zfin.database.BtsContainsService;
import org.zfin.expression.Figure;
import org.zfin.fish.WarehouseSummary;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.*;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;

/**
 * Basic repository class to handle fish searches against a database.
 */
@Repository
public class HibernateConstructRepository implements ConstructRepository {

    private static Logger logger = Logger.getLogger(org.zfin.construct.repository.HibernateConstructRepository.class);
    private static PublicationRepository pr = RepositoryFactory.getPublicationRepository();

    private ZfinEntity getZfinEntity(String zdbID, String name) {
        ZfinEntity entity = new ZfinEntity();
        entity.setName(name);
        entity.setID(zdbID);
        return entity;
    }

    public WarehouseSummary getWarehouseSummary(WarehouseSummary.Mart mart) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(WarehouseSummary.class);
        criteria.add(Restrictions.eq("martName", mart.getName()));
        return (WarehouseSummary) criteria.uniqueResult();
    }

    /**
     * Retrieve the status of the construct mart:
     * true: construct mart ready for usage
     * false: construct mart is being rebuilt.
     *
     * @return status
     */

    public ZdbFlag getConstructMartStatus() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ZdbFlag.class);
        criteria.add(Restrictions.eq("type", ZdbFlag.Type.REGEN_CONSTRUCTMART));
        return (ZdbFlag) criteria.uniqueResult();
    }


    public ConstructRelationship getConstructRelationship(ConstructCuration marker1, Marker marker2, ConstructRelationship.Type type) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(ConstructRelationship.class);
        criteria.add(Restrictions.eq("construct", marker1));
        criteria.add(Restrictions.eq("marker", marker2));
        criteria.add(Restrictions.eq("type", type));
        return (ConstructRelationship) criteria.uniqueResult();

    }

    public ConstructRelationship getConstructRelationshipByID(String zdbID) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(ConstructRelationship.class);
        criteria.add(Restrictions.eq("zdbID", zdbID));
        return (ConstructRelationship) criteria.uniqueResult();
    }


    public List<ConstructRelationship> getConstructRelationshipsByPublication(String publicationZdbID) {
        List<ConstructRelationship.Type> constructRelationshipList = new ArrayList<ConstructRelationship.Type>();
        constructRelationshipList.add(ConstructRelationship.Type.PROMOTER_OF);
        constructRelationshipList.add(ConstructRelationship.Type.CODING_SEQUENCE_OF);
        constructRelationshipList.add(ConstructRelationship.Type.CONTAINS_REGION);

        Session session = currentSession();
        String hql = "select distinct cmr from ConstructRelationship as cmr, " +
                "PublicationAttribution as attribution " +
                "where  attribution.dataZdbID = cmr.zdbID AND " +
                "cmr.type in (:constructRelationshipType)AND " +
                "attribution.publication.zdbID = :pubID ";

        Query query = session.createQuery(hql);
        query.setParameter("pubID", publicationZdbID);
        query.setParameterList("constructRelationshipType", constructRelationshipList);
        List<ConstructRelationship> constructRelationships = (List<ConstructRelationship>) query.list();
        Collections.sort(constructRelationships, new Comparator<ConstructRelationship>() {
            @Override
            public int compare(ConstructRelationship o1, ConstructRelationship o2) {
                return o1.getConstruct().getName().compareTo(o2.getConstruct().getName());
            }
        });
        // order
        /*Collections.sort(markerRelationships, new Comparator<ConstructRelationship>(){
            @Override
            public int compare(ConstructRelationship o1, ConstructRelationship o2) {
                return o1.getFirstMarker().getAbbreviationOrder().compareTo(o2.getFirstMarker().getAbbreviationOrder()) ;
            }
        });*/
        return constructRelationships;
    }


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
                    ;
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
            Set<PublicationAttribution> pubAttrbs = new HashSet<PublicationAttribution>();
            pubAttrbs.add(pa);
            Marker mrkr = new Marker();
            mrkr.setPublications(pubAttrbs);
            currentSession().save(pa);
        }
    }

    public ConstructCuration getConstructByID(String zdbID) {
        Session session = currentSession();
        return (ConstructCuration) session.get(ConstructCuration.class, zdbID);
    }

    public ConstructCuration getConstructByName(String conName) {
        Session session = currentSession();
        return (ConstructCuration) session.get(ConstructCuration.class, conName);
    }

    public void createConstruct(ConstructCuration construct, Publication pub) {
        if (construct.getName() == null)
            throw new RuntimeException("Cannot create a new construct without a name.");
        if (construct == null)
            throw new RuntimeException("No construct object provided.");
        if (construct.getConstructType() == null)
            throw new RuntimeException("Cannot create a new construct without a type.");
        if (pub == null)
            throw new RuntimeException("Cannot create a new construct without a publication.");

        construct.setOwner(ProfileService.getCurrentSecurityUser());
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
    public List<ConstructComponent> getConstructComponentsByComponentID(String componentZdbID) {
        Session session = HibernateUtil.currentSession();

        Criteria criteria = session.createCriteria(ConstructComponent.class);
        criteria.add(Restrictions.eq("componentZdbID", componentZdbID));
        return (List<ConstructComponent>) criteria.list();
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
