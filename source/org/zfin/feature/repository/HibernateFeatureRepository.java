package org.zfin.feature.repository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.BasicTransformerAdapter;
import org.springframework.stereotype.Repository;
import org.zfin.feature.*;
import org.zfin.feature.presentation.FeatureLabEntry;
import org.zfin.feature.presentation.FeaturePrefixLight;
import org.zfin.feature.presentation.LabLight;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.Mutagee;
import org.zfin.gwt.root.dto.Mutagen;
import org.zfin.infrastructure.DataAlias;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.PreviousNameLight;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.profile.FeatureSource;
import org.zfin.profile.Organization;
import org.zfin.profile.OrganizationFeaturePrefix;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DBLink;

import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;


/**
 * Hibernate implementation of the Antibody Repository.
 */
@Repository
public class HibernateFeatureRepository implements FeatureRepository {

    private static InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();

    private Logger logger = Logger.getLogger(HibernateFeatureRepository.class);

    public Feature getFeatureByID(String zdbID) {
        return (Feature) HibernateUtil.currentSession().get(Feature.class, zdbID);
    }

    public DataAlias getSpecificDataAlias(Feature feature, String alias) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(DataAlias.class);
        criteria.add(Restrictions.eq("feature", feature));
        criteria.add(Restrictions.eq("alias", alias));
        return (DataAlias) criteria.uniqueResult();
    }

    /**
     * Retrieve a list of all feature for a given publication.
     * Features need to be directly attributed to the publication in question.
     *
     * @param publicationID publication
     * @return list of features
     */
    @SuppressWarnings("unchecked")
    public List<Feature> getFeaturesByPublication(String publicationID) {
        Session session = currentSession();
        String hql = "select feature from Feature as feature, " +
                "PublicationAttribution as attribution " +
                "where  attribution.dataZdbID = feature.zdbID AND " +
                "      attribution.publication.zdbID = :pubID AND" +
                "      attribution.sourceType = :type" +
                "     order by feature.abbreviationOrder";

        Query query = session.createQuery(hql);
        query.setParameter("pubID", publicationID);
        query.setParameter("type", RecordAttribution.SourceType.STANDARD);
        return (List<Feature>) query.list();
    }


    @SuppressWarnings("unchecked")
    public List<FeatureMarkerRelationship> getFeatureRelationshipsByPublication(String publicationZdbID) {

        Session session = currentSession();
        String hql = "select distinct fmr from FeatureMarkerRelationship as fmr, " +
                "PublicationAttribution as attribution " +
                "where  attribution.dataZdbID = fmr.feature.zdbID AND " +
                "      attribution.publication.zdbID = :pubID ";

        Query query = session.createQuery(hql);
        query.setParameter("pubID", publicationZdbID);
        List<FeatureMarkerRelationship> featureMarkerRelationships = (List<FeatureMarkerRelationship>) query.list();

        // order
        Collections.sort(featureMarkerRelationships, new Comparator<FeatureMarkerRelationship>() {
            @Override
            public int compare(FeatureMarkerRelationship o1, FeatureMarkerRelationship o2) {
                return o1.getFeature().getAbbreviationOrder().compareTo(o2.getFeature().getAbbreviationOrder());
            }
        });
        return featureMarkerRelationships;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getRelationshipTypesForFeatureType(FeatureTypeEnum featureTypeEnum) {

        String sql = "\n" +
                "select distinct fmreltype_name " +
                "   from " +
                "   feature_marker_relationship_type, feature_type_group," +
                "   feature_type_group_member" +
                "   where" +
                "   ftrgrpmem_ftr_type= :featureType " +
                "   and ftrgrpmem_ftr_type_group=fmreltype_ftr_type_group";

        sql += "   union" +
                "   select distinct mreltype_name " +
                "   from" +
                "   marker_relationship_type, marker_type_group, " +
                "   marker_type_group_member   " +
                "   where" +
                "   mtgrpmem_mrkr_type= :featureType " +
                "   and mtgrpmem_mrkr_type_group=mtgrp_name" +
                "   and (mreltype_mrkr_type_group_1=mtgrpmem_mrkr_type_group" +
                "   or mreltype_mrkr_type_group_2=mtgrpmem_mrkr_type_group) " +
                "";
        return (List<String>) HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("featureType", featureTypeEnum.name())
                .list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Marker> getMarkersForFeatureRelationAndSource(String featureRelationshipName, String publicationZdbID) {
        String sql = "select distinct mrkr_zdb_id " +
                "    from marker, feature_marker_relationship_type, " +
                "    marker_relationship_type, marker_type_group_member," +
                "    record_attribution" +
                "    where mrkr_zdb_id = recattrib_data_zdb_id" +
                "    and recattrib_source_zdb_id=:pubZdbID " +
                "    and mrkr_type=mtgrpmem_mrkr_type" +
                "    and  ((mtgrpmem_mrkr_type_group=mreltype_mrkr_type_group_2 and  mreltype_name=:featureRelation) or (mtgrpmem_mrkr_type_group=fmreltype_mrkr_type_group" +
                "    and fmreltype_name = :featureRelation))";


        List<String> markerZdbIds = (List<String>) HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("pubZdbID", publicationZdbID)
                .setString("featureRelation", featureRelationshipName)
                .list();
        List<Marker> markers = new ArrayList<Marker>();
        for (String zdbId : markerZdbIds) {
            Marker m = (Marker) HibernateUtil.currentSession().get(Marker.class, zdbId);
            markers.add(m);
        }
        return markers;
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<String> getAllFeaturePrefixes() {
        return HibernateUtil.currentSession().createQuery(
                " select distinct fp.prefixString from FeaturePrefix fp order by fp.prefixString asc ").list();
    }

    @Override

    /**
     * This is rewritten for speed.
     * @return Gets a reprentation of all of the FeaturePrefixes with their associated labs.
     */
    public List<FeaturePrefixLight> getFeaturePrefixWithLabs() {
        String sql = "select fp.fp_prefix,fp.fp_institute_display,l.zdb_id,l.name, sfp.sfp_current_designation " +
                "from feature_prefix fp " +
                "join source_feature_prefix sfp on fp.fp_pk_id=sfp.sfp_prefix_id " +
                "join lab l on sfp.sfp_source_zdb_id=l.zdb_id " +
                "union " +
                "select fp.fp_prefix,fp.fp_institute_display,l.zdb_id,l.name, sfp.sfp_current_designation " +
                "from feature_prefix fp " +
                "join source_feature_prefix sfp on fp.fp_pk_id=sfp.sfp_prefix_id " +
                "join company l on sfp.sfp_source_zdb_id=l.zdb_id " +
                "group by fp.fp_prefix, fp.fp_institute_display,l.zdb_id,l.name, sfp.sfp_current_designation " +
                "order by fp.fp_prefix, l.name ";
        List<Object[]> results = HibernateUtil.currentSession().createSQLQuery(sql).list();
        List<FeaturePrefixLight> featurePrefixLightList = new ArrayList<FeaturePrefixLight>();
        FeaturePrefixLight featurePrefixLight = null;
        String currentPrefix = null;
        int i = 0;
        for (Object[] result : results) {
            // if an existing one, then just add the lab
            if (featurePrefixLight != null
                    && currentPrefix != null
                    && result[0].toString().equals(currentPrefix)) {
                if (Boolean.parseBoolean(result[4].toString())) {
                    featurePrefixLight.addLabLight(createLab(result));
                }
            }
            // if result is not equal, or we are at the start or end, add the current and open a new one
            else if (i == results.size()
                    || currentPrefix == null
                    || !result[0].toString().equals(currentPrefix)) {
                // add the current one
                if (featurePrefixLight != null) {
                    featurePrefixLightList.add(featurePrefixLight);
                }
                featurePrefixLight = new FeaturePrefixLight();
                currentPrefix = result[0].toString();
                featurePrefixLight.setPrefix(currentPrefix);
                if (Boolean.parseBoolean(result[4].toString())) {
                    featurePrefixLight.addLabLight(createLab(result));
                }
                if (result[1] != null) {
                    featurePrefixLight.setInstituteDisplay(result[1].toString());
                } else {
                    throw new RuntimeException("Should not get here when iterating over feature prefixes.");
                }
            }
            ++i;
        }
        featurePrefixLightList.add(featurePrefixLight);
        return featurePrefixLightList;
    }

    private LabLight createLab(Object[] result) {
        LabLight lab = new LabLight();
        lab.setZdbID(result[2].toString());
        lab.setName(result[3].toString());
        lab.setCurrentDesignation(Boolean.parseBoolean(result[4].toString()));
        return lab;
    }

    /**
     * We can use the f.sources[0] notation, because there is a 1-1 relationship between Feature and Lab source.
     *
     * @param prefix
     * @return
     */
    @Override
    public List<FeatureLabEntry> getFeaturesForPrefix(String prefix) {
        String hql = " select distinct f, s  from Feature f "
                + " join f.featurePrefix fp   "
                + " left join f.sources s "
                + " where f.featurePrefix = fp "
                + " and fp.prefixString = :featurePrefix "
                + " order by  f.abbreviationOrder asc "
                + "";
        List<Object[]> featureEntryObjects = currentSession().createQuery(hql)
                .setParameter("featurePrefix", prefix)
                .list();
        List<FeatureLabEntry> featureLabEntries = new ArrayList<FeatureLabEntry>();
        for (Object[] featureEntryObj : featureEntryObjects) {
            FeatureLabEntry featureLabEntry = new FeatureLabEntry();
            featureLabEntry.setFeature((Feature) featureEntryObj[0]);
            if (featureEntryObj[1] != null) {
                featureLabEntry.setSourceOrganization(((FeatureSource) featureEntryObj[1]).getOrganization());
            }
            featureLabEntries.add(featureLabEntry);
        }
        return featureLabEntries;

    }

    @Override
    public List<OrganizationFeaturePrefix> getOrganizationFeaturePrefixForPrefix(String prefix) {
        String hql = " select distinct lfp from OrganizationFeaturePrefix lfp , Feature f, FeaturePrefix fp "
                + " where f.featurePrefix = fp "
                + " and fp.prefixString = :featurePrefix "
                + " and lfp.featurePrefix = fp "
                + "";
        return currentSession().createQuery(hql).setParameter("featurePrefix", prefix).list();
    }

    @Override
    public List<Organization> getLabsWithFeaturesForPrefix(String prefix) {
        String hql = " select distinct lfp.organization from OrganizationFeaturePrefix lfp , Feature f, FeaturePrefix fp "
                + " where f.featurePrefix = fp "
                + " and fp.prefixString = :featurePrefix "
                + " and lfp.featurePrefix = fp "
                + "";
        return currentSession().createQuery(hql).setParameter("featurePrefix", prefix).list();
    }

    public Organization getLabByFeature(Feature ftr) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct l  from  FeatureSource fs, Organization l" +
                "     where fs.feature = :ftr " +
                "    and fs.organization = l";


        Query query = session.createQuery(hql);
        query.setParameter("ftr", ftr);
        return ((Organization) query.uniqueResult());

    }

    public String getPrefixById(int labPrefixID) {
        Session session = HibernateUtil.currentSession();
        String hqlLab1 = " select fp.prefixString from  FeaturePrefix fp where fp.featurePkID =:labPrefixID ";
        Query queryLab = session.createQuery(hqlLab1);
        queryLab.setParameter("labPrefixID", labPrefixID);
        return (String) queryLab.uniqueResult();
    }

    public String getCurrentPrefixForLab(String labZdbID) {
        Session session = HibernateUtil.currentSession();
        String hqlLab1 = " select fp.prefixString from  OrganizationFeaturePrefix  lfp join lfp.organization l " +
                " join lfp.featurePrefix fp " +
                " where l.zdbID =:labZdbID" +
                " and lfp.currentDesignation =:currentDesignation ";
        Query queryLab = session.createQuery(hqlLab1);
        queryLab.setString("labZdbID", labZdbID);
        queryLab.setBoolean("currentDesignation", true);
        return (String) queryLab.uniqueResult();

    }


    public List<Organization> getLabsOfOriginWithPrefix() {
        String hqlLab = " select distinct lfp.organization from OrganizationFeaturePrefix lfp  " +
                " where lfp.featurePrefix is not null " +
                " and lfp.organization.name is not null  " +
                " and lfp.currentDesignation = :true";

        Query query = HibernateUtil.currentSession().createQuery(hqlLab);
        query.setBoolean("true", true);
        /*List<FeatureMarkerRelationship> featureMarkerRelationships = (List<FeatureMarkerRelationship>) query.list();
              HibernateUtil.currentSession().createQuery(hqlLab)
                .setBoolean("true", true)*/
                /*setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] aliases) {
                        return tuple[0];
                    }
                })*/
        List<Organization> organizations = (List<Organization>) query.list();
        Collections.sort(organizations, new Comparator<Organization>() {
            @Override
            public int compare(Organization o1, Organization o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return organizations;
    }


    public List<FeaturePrefix> getLabPrefixes(String labName) {
        return getLabPrefixes(labName, true);
    }


    public List<FeaturePrefix> getLabPrefixes(String labName, boolean assignIfEmpty) {
        String hqlLab1 = " select lfp from OrganizationFeaturePrefix lfp  " +
                " join lfp.organization lb " +
                " where lb.name=:labName " +
                " order by lfp.currentDesignation desc, lfp.featurePrefix.prefixString asc";
//        select sfp_current_designation, fp_prefix from source_feature_prefix
//        join feature_prefix on (sfp_prefix_id = fp_pk_id)
//        join zdb_active_source on (zactvs_zdb_id = sfp_source_zdb_id)
//        join lab on (zactvs_zdb_id = zdb_id)
//        where lab.name = labName;

        List<OrganizationFeaturePrefix> organizationFeaturePrefixes = HibernateUtil.currentSession().createQuery(hqlLab1)
                .setParameter("labName", labName).list();
        return generateFeaturePrefixes(organizationFeaturePrefixes, assignIfEmpty);
    }

    public List<FeaturePrefix> getLabPrefixesById(String labZdbID, boolean assignIfEmpty) {
        String hqlLab1 = " select lfp from OrganizationFeaturePrefix lfp  " +
                " join lfp.organization lb " +
                " where lb.zdbID=:labZdbID " +
                " order by lfp.currentDesignation desc, lfp.featurePrefix.prefixString asc";

        List<OrganizationFeaturePrefix> organizationFeaturePrefixes = HibernateUtil.currentSession().createQuery(hqlLab1)
                .setParameter("labZdbID", labZdbID).list();

        return generateFeaturePrefixes(organizationFeaturePrefixes, assignIfEmpty);
    }


    public List<FeaturePrefix> getCurrentLabPrefixesById(String labZdbID, boolean assignIfEmpty) {
        String hqlLab1 = " select lfp from OrganizationFeaturePrefix lfp  " +
                " join lfp.organization lb " +
                " where lb.zdbID=:labZdbID " +
                " and lfp.currentDesignation='t' " +
                " order by lfp.currentDesignation desc, lfp.featurePrefix.prefixString asc";

        List<OrganizationFeaturePrefix> organizationFeaturePrefixes = HibernateUtil.currentSession().createQuery(hqlLab1)
                .setParameter("labZdbID", labZdbID).list();

        return generateFeaturePrefixes(organizationFeaturePrefixes, assignIfEmpty);
    }

    /**
     * This is a helper method for getLabPrefixes and getLabPrefixesById
     *
     * @param organizationFeaturePrefixes
     * @param assignIfEmpty
     * @return featurePrefixes
     */
    private List<FeaturePrefix> generateFeaturePrefixes(List<OrganizationFeaturePrefix> organizationFeaturePrefixes, boolean assignIfEmpty) {
        List<FeaturePrefix> featurePrefixes = new ArrayList<FeaturePrefix>();
        for (OrganizationFeaturePrefix organizationFeaturePrefix : organizationFeaturePrefixes) {
            FeaturePrefix featurePrefix = organizationFeaturePrefix.getFeaturePrefix();
            featurePrefix.setCurrentDesignationForSet(organizationFeaturePrefix.getCurrentDesignation());
            featurePrefixes.add(featurePrefix);
        }
        if (CollectionUtils.isEmpty(featurePrefixes) && assignIfEmpty) {
            FeaturePrefix featurePrefix = new FeaturePrefix();
            featurePrefix.setPrefixString("zf");
            featurePrefix.setCurrentDesignationForSet(true);
            featurePrefixes.add(featurePrefix);
        }
        return featurePrefixes;
    }

    public List<String> getFeatureTypes() {
        String hql = " select c.name from FeatureType c group by c.significance ";
        return currentSession().createQuery(hql).list();
    }

    public List<String> getFeatureTypeDisplayNames() {
        String hql = " select c.dispName from FeatureType c group by c.significance ";
        Session session = currentSession();
        return session.createQuery(hql).list();
    }

    public String getFeatureTypeDisplay(String featureType) {
        String hql = " select c.dispName from FeatureType c " +
                " where c.name=:featureType ";
        Session session = currentSession();
        Query query = session.createQuery(hql);
        query.setParameter("featureType", featureType);
        return ((String) query.uniqueResult());
    }


    public FeaturePrefix getFeatureLabPrefixID(String labPrefix) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct fp from  FeaturePrefix fp" +
                "     where fp.prefixString = :desig ";

        return (FeaturePrefix) HibernateUtil.currentSession().createQuery(hql)
                .setParameter("desig", labPrefix)
                .uniqueResult();
    }

    public FeatureAssay addFeatureAssay(Feature ftr, Mutagen mutagen, Mutagee mutagee) {
        FeatureAssay ftrAss = new FeatureAssay();
        ftrAss.setFeature(ftr);
        ftrAss.setMutagen(mutagen);
        ftrAss.setMutagee(mutagee);
        HibernateUtil.currentSession().save(ftrAss);
        return ftrAss;
    }


    public FeatureAssay getFeatureAssay(Feature feature) {
        Criteria criteria = HibernateUtil.currentSession().createCriteria(FeatureAssay.class);
        criteria.add(Restrictions.eq("feature", feature));
        criteria.setMaxResults(1);
        FeatureAssay ftrAss = (FeatureAssay) criteria.uniqueResult();
        return ftrAss;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Feature> getFeaturesForAttribution(String publicationZdbID) {
        String hql = "" +
                " select distinct f from Feature f , RecordAttribution ra " +
                " where ra.dataZdbID=f.zdbID and ra.sourceType = :standard and ra.sourceZdbID = :pubZdbID " +
                " order by f.abbreviationOrder " +
                " ";

        return (List<Feature>) HibernateUtil.currentSession().createQuery(hql)
                .setString("pubZdbID", publicationZdbID)
                .setString("standard", RecordAttribution.SourceType.STANDARD.toString())
                .list();
    }


    @Override
    public DataNote addFeatureDataNote(Feature feature, String note) {
        logger.debug("enter addMarDataNote");
        DataNote dnote = new DataNote();
        dnote.setDataZdbID(feature.getZdbID());
        logger.debug("markerZdbId for datanote: " + feature.getZdbID());
        dnote.setCurator(ProfileService.getCurrentSecurityUser());
        dnote.setDate(new Date());
        dnote.setNote(note);
        logger.debug("data note curator: " + ProfileService.getCurrentSecurityUser());
        Set<DataNote> dataNotes = feature.getDataNotes();
        if (dataNotes == null) {
            dataNotes = new HashSet<>();
            dataNotes.add(dnote);
            feature.setDataNotes(dataNotes);
        } else dataNotes.add(dnote);
        currentSession().save(dnote);
        logger.debug("dnote zdb_id: " + dnote.getZdbID());
        return dnote;
    }

    public void deleteFeatureAlias(Feature feature, FeatureAlias alias) {
        if (feature == null)
            throw new RuntimeException("No marker object provided.");
        if (alias == null)
            throw new RuntimeException("No alias object provided.");
        // check that the alias belongs to the marker
        if (!feature.getAliases().contains(alias))
            throw new RuntimeException("Alias '" + alias + "' does not belong to the marker '" + feature + "'! " +
                    "Cannot remove such an alias.");
        // remove the ZDB active data record with cascade.

        String hql = "delete from FeatureHistory  mh " +
                " where mh.featureAlias = :zdbID ";
        Query query = currentSession().createQuery(hql);
        query.setString("zdbID", alias.getZdbID());
        query.executeUpdate();

        feature.getAliases().remove(alias);
        infrastructureRepository.deleteActiveDataByZdbID(alias.getZdbID());
    }


    public void deleteFeatureDBLink(Feature feature, DBLink sequence) {
        if (feature == null)
            throw new RuntimeException("No marker object provided.");
        if (sequence == null)
            throw new RuntimeException("No alias object provided.");
        // check that the alias belongs to the marker
        if (!feature.getDbLinks().contains(sequence))
            throw new RuntimeException("Alias '" + sequence + "' does not belong to the marker '" + feature + "'! " +
                    "Cannot remove such an alias.");
        // remove the ZDB active data record with cascade.


        infrastructureRepository.deleteActiveDataByZdbID(sequence.getZdbID());
        currentSession().flush();

        currentSession().refresh(feature);

        // run the fast search table script so the alias is not showing up any more.
        //runFeatureNameFastSearchUpdate(feature);
    }


    @Override
    public Feature getFeatureByAbbreviation(String name) {
        return (Feature) currentSession().createCriteria(Feature.class)
                .add(Restrictions.eq("abbreviation", name))
                .uniqueResult()
                ;
    }

    @Override
    public String getFeatureByAbbreviationInTrackingTable(String featTrackingFeatAbbrev) {
        Session session = HibernateUtil.currentSession();
        String hqlFtrTrack = " select ft.featTrackingFeatAbbrev from  FeatureTracking ft where ft.feature.zdbID =:featTrackingFeatAbbrev ";
        Query queryTracker = session.createQuery(hqlFtrTrack);
        queryTracker.setParameter("featTrackingFeatAbbrev", featTrackingFeatAbbrev);
        return (String) queryTracker.uniqueResult();
    }

    public String getFeatureByIDInTrackingTable(String featTrackingFeatZdbID) {
        Session session = HibernateUtil.currentSession();
        String hqlFtrTrack = " select ft.featTrackingFeatAbbrev from  FeatureTracking ft where ft.feature.zdbID =:featTrackingFeatZdbID ";
        Query queryTracker = session.createQuery(hqlFtrTrack);
        queryTracker.setParameter("featTrackingFeatZdbID", featTrackingFeatZdbID);
        return (String) queryTracker.uniqueResult();
    }

    public TreeSet<String> getFeatureLG(Feature feat) {
        Session session = HibernateUtil.currentSession();
        TreeSet<String> lgList = new TreeSet<String>();


        String hql = "select distinct mm.lg" +
                "  from MappedMarker mm" +
                "   where mm.marker.zdbID=:ftr ";
        Query query = session.createQuery(hql);
        query.setString("ftr", feat.getZdbID());
        lgList.addAll(query.list());

        query = session.createQuery(
                "select l.chromosome " +
                        "from Linkage l join l.linkageMemberSet as m " +
                        " where (m.markerOneZdbId = :zdbId OR m.markerTwoZdbId = :zdbId) ");
        query.setParameter("zdbId", feat.getZdbID());
        lgList.addAll(query.list());
        return lgList;
    }


    public List<Feature> getFeaturesByAbbreviation(String name) {
        List<Feature> features = new ArrayList<>();
        Session session = currentSession();

        Criteria criteria1 = session.createCriteria(Feature.class);
        criteria1.add(Restrictions.like("abbreviation", name, MatchMode.START).ignoreCase());
        criteria1.addOrder(Order.asc("abbreviationOrder"));
        features.addAll(criteria1.list());

        Criteria criteria2 = session.createCriteria(Feature.class);
        criteria2.add(Restrictions.like("abbreviation", name, MatchMode.ANYWHERE).ignoreCase());
        criteria2.add(Restrictions.not(Restrictions.like("abbreviation", name, MatchMode.START).ignoreCase()));
        criteria2.addOrder(Order.asc("abbreviationOrder"));
        features.addAll(criteria2.list());
        return features;
    }

    public Feature getFeatureByPrefixAndLineNumber(String prefix, String lineNumber) {
        String hql = " select f from Feature f join f.featurePrefix fp " +
                " where f.lineNumber = :lineNumber  " +
                " and fp.prefixString = :prefix ";
        List<Feature> features = currentSession().createQuery(hql)
                .setString("lineNumber", lineNumber)
                .setString("prefix", prefix)
                .list();

        if (CollectionUtils.isEmpty(features)) {
            logger.debug("no features founds for prefix[" + prefix + "] and line number[" + lineNumber + "]");
            return null;
        }
        if (features.size() == 1) {
            return features.get(0);
        } else {
            logger.error("" + features.size() + " found for prefix[" + prefix + "] and line number[" + lineNumber + "]");
            return null;
        }
    }

    public String getPrefix(String prefixString) {
        Session session = HibernateUtil.currentSession();
        String hqlLab1 = " select fp.prefixString from  FeaturePrefix fp where fp.prefixString =:prefixString ";
        Query queryLab = session.createQuery(hqlLab1);
        queryLab.setParameter("prefixString", prefixString);
        return (String) queryLab.uniqueResult();
    }

    public List<Feature> getFeatureForAttribution(String publicationZdbID) {

        String hql = "" +
                " select distinct f from Feature f , RecordAttribution ra " +
                " where ra.dataZdbID=f.zdbID and ra.sourceType = :standard and ra.sourceZdbID = :pubZdbID " +
                " order by f.abbreviationOrder " +
                " ";

        return (List<Feature>) HibernateUtil.currentSession().createQuery(hql)
                .setString("pubZdbID", publicationZdbID)
                .setString("standard", RecordAttribution.SourceType.STANDARD.toString())
                .list();
    }

    @SuppressWarnings({"unchecked"})
    public List<Feature> getFeaturesForStandardAttribution(Publication publication) {
        String hql = "select f from PublicationAttribution pa , Feature f " +
                " where pa.dataZdbID=f.zdbID and pa.publication.zdbID= :pubZdbID  " +
                " and pa.sourceType= :sourceType  ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setString("pubZdbID", publication.getZdbID());
        query.setString("sourceType", PublicationAttribution.SourceType.STANDARD.toString());
        return query.list();
    }

    public List<Marker> getMarkersByFeature(Feature feature) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct fmrel.marker from  FeatureMarkerRelationship fmrel, Marker m" +
                "     where fmrel.feature.zdbID = :feat" +
                " and fmrel.type in (:relation, :relationship1, :relationship2) " +
                " and fmrel.marker=m ";


        Query query = session.createQuery(hql);

        query.setString("feat", feature.getZdbID());
        query.setString("relation", FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF.toString());
        query.setString("relationship1", FeatureMarkerRelationshipTypeEnum.MARKERS_PRESENT.toString());
        query.setString("relationship2", FeatureMarkerRelationshipTypeEnum.MARKERS_MISSING.toString());
        //query.setParameter("type", Marker.Type.GENE);
        //query.setString("type", Marker.Type.GENE.toString());

        return (List<Marker>) query.list();
    }

    public List<Marker> getMarkerIsAlleleOf(Feature feature) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct fmrel.marker from  FeatureMarkerRelationship fmrel, Marker m" +
                "     where fmrel.feature.zdbID = :feat" +
                " and fmrel.type = :relation " +
                " and fmrel.marker=m ";

        Query query = session.createQuery(hql);
        query.setString("feat", feature.getZdbID());
        query.setString("relation", FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF.toString());

        return (List<Marker>) query.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Marker> getMarkersPresentForFeature(Feature feature) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct fmrel.marker from  FeatureMarkerRelationship fmrel, Marker m" +
                "     where fmrel.feature.zdbID = :feat" +
                " and fmrel.type=:relationship " +
                " and fmrel.marker=m " +
                " and m.markerType =:type";

        Query query = session.createQuery(hql);
        query.setParameter("relationship", FeatureMarkerRelationshipTypeEnum.MARKERS_PRESENT.toString());
        query.setString("feat", feature.getZdbID());


        //query.setString("type", Marker.Type.GENE.toString());

        return (List<Marker>) query.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public String setCurrentPrefix(String organizationZdbId, String prefix) {
        List<OrganizationFeaturePrefix> organizationFeaturePrefixes = HibernateUtil.currentSession().createCriteria(OrganizationFeaturePrefix.class)
                .add(Restrictions.eq("organization.zdbID", organizationZdbId))
                .list();
        String hql = " update source_feature_prefix  " +
                " set sfp_current_designation = :currentDesignation " +
                " where sfp_source_zdb_id = :organizationZdbID " +
                " and sfp_prefix_id = :prefix ";
        Query query = HibernateUtil.currentSession().createSQLQuery(hql);
        query.setString("organizationZdbID", organizationZdbId);
        String returnedPrefix = null;
        for (OrganizationFeaturePrefix organizationFeaturePrefix : organizationFeaturePrefixes) {
            logger.info("feature prefix before: " + organizationFeaturePrefix);
            query.setInteger("prefix", organizationFeaturePrefix.getFeaturePrefix().getFeaturePkID());
            if (organizationFeaturePrefix.getFeaturePrefix().getPrefixString().equals(prefix)) {
                query.setBoolean("currentDesignation", true);
                returnedPrefix = prefix;
            } else {
                query.setBoolean("currentDesignation", false);
            }
            query.executeUpdate();
            HibernateUtil.currentSession().flush();
        }
        return returnedPrefix;
    }


    public FeaturePrefix setNewLabPrefix(String prefix, String location) {
        logger.debug("enter addMarDataNote");
        FeaturePrefix fpPrefix = new FeaturePrefix();
        fpPrefix.setPrefixString(prefix);
        fpPrefix.setInstitute(location);
        HibernateUtil.currentSession().save(fpPrefix);

        return fpPrefix;
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<Feature> getFeaturesForLab(String zdbID) {
        String hql = " select f from Feature f join f.sources s " +
                " where s.organization.zdbID = :zdbID " +
                " order by f.abbreviationOrder asc " +
                "";
        List<Feature> features = HibernateUtil.currentSession().createQuery(hql)
                .setString("zdbID", zdbID)
                .list();
        return features;
    }

    @Override
    public int setLabOfOriginForFeature(Organization lab, Feature feature) {
        String sql = " update int_data_source " +
                " set ids_source_zdb_id = :newLabZdbId " +
                " where ids_data_zdb_id  = :featureZdbId  ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("newLabZdbId", lab.getZdbID())
                .setString("featureZdbId", feature.getZdbID());
        int recordsUpdated = query.executeUpdate();
        if (recordsUpdated != 1) {
            logger.error("A feature must have had multiple labs: " + feature.getZdbID()
                    + " records updated: " + recordsUpdated);
        }
        return recordsUpdated;
    }

    @Override
    public void deleteLabOfOriginForFeature(Feature feature) {
        String sql = " delete int_data_source " +
                " where ids_data_zdb_id  = :featureZdbId  ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("featureZdbId", feature.getZdbID());
        int recordsUpdated = query.executeUpdate();
        if (recordsUpdated != 1) {
            logger.error("A feature must of had multiple labs to delete: " + feature.getZdbID()
                    + " records deleted : " + recordsUpdated);
        }
    }

    @Override
    public int addLabOfOriginForFeature(Feature feature, String labOfOrigin) {
        String sql = " insert into int_data_source (ids_source_zdb_id,ids_data_zdb_id)" +
                " values ( :newLabZdbId , :featureZdbId ) " +
                "    ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("newLabZdbId", labOfOrigin)
                .setString("featureZdbId", feature.getZdbID());
        int recordsUpdated = query.executeUpdate();
        if (recordsUpdated != 1) {
            logger.error("A feature must of had multiple labs: " + feature.getZdbID()
                    + " records updated: " + recordsUpdated);
        }
        return recordsUpdated;
    }

    /**
     * Retrieve all feature ids.
     * If firstNIds > 0 return only the first N.
     * If firstNIds < 0 return null
     *
     * @param firstNIds number of records to return
     * @return list of ids
     */
    @Override
    public List<String> getAllFeatures(int firstNIds) {
        if (firstNIds < 0)
            return null;
        Session session = HibernateUtil.currentSession();
        String hql = "select zdbID from Feature order by zdbID";
        Query query = session.createQuery(hql);
        if (firstNIds > 0)
            query.setMaxResults(firstNIds);
        return query.list();

    }

    @Override
    public FeaturePrefix getFeaturePrefixByPrefix(String prefix) {
        Session session = HibernateUtil.currentSession();
        String hqlLab1 = " select fp from  FeaturePrefix fp where fp.prefixString =:prefix";
        Query queryLab = session.createQuery(hqlLab1);
        queryLab.setParameter("prefix", prefix);
        return (FeaturePrefix) queryLab.uniqueResult();
    }

    @Override
    public int insertOrganizationPrefix(Organization organization, FeaturePrefix featurePrefix) {
        OrganizationFeaturePrefix organizationFeaturePrefix = new OrganizationFeaturePrefix();
        organizationFeaturePrefix.setCurrentDesignation(true);
        organizationFeaturePrefix.setFeaturePrefix(featurePrefix);
        organizationFeaturePrefix.setOrganization(organization);
        HibernateUtil.currentSession().save(organizationFeaturePrefix);
        return 1;
    }

    @Override
    public int setNoLabPrefix(String zdbID) {
        String sql = "update source_feature_prefix  " +
                "set sfp_current_designation = 'f' " +
                "where sfp_source_zdb_id=:labZdbId ";
        return HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("labZdbId", zdbID)
                .executeUpdate();

    }

    @Override
    public List<PreviousNameLight> getPreviousNamesLight(final Genotype genotype) {
        String sql = "  " +
                " select da.dalias_alias, ra.recattrib_source_zdb_id, da.dalias_zdb_id " +
                "    from data_alias da " +
                "    join alias_group ag on da.dalias_group_id=ag.aliasgrp_pk_id " +
                "    left outer join record_attribution ra on ra.recattrib_data_zdb_id=da.dalias_zdb_id  " +
                "    where dalias_data_zdb_id = :markerZdbID " +
                "    and aliasgrp_pk_id = dalias_group_id " +
                "    and aliasgrp_name = 'alias' " +
                " ";
        return (List<PreviousNameLight>) HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("markerZdbID", genotype.getZdbID())
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] aliases) {
                        PreviousNameLight previousNameLight = new PreviousNameLight(genotype.getName());
                        previousNameLight.setMarkerZdbID(genotype.getZdbID());
                        previousNameLight.setAlias(tuple[0].toString());
                        previousNameLight.setAliasZdbID(tuple[2].toString());
                        if (tuple[1] != null) {
                            previousNameLight.setPublicationZdbID(tuple[1].toString());
                            previousNameLight.setPublicationCount(1);
                        }

                        return previousNameLight;
                    }

                    @Override
                    public List transformList(List list) {
                        Map<String, PreviousNameLight> map = new HashMap<String, PreviousNameLight>();
                        for (Object o : list) {
                            PreviousNameLight previousName = (PreviousNameLight) o;
                            PreviousNameLight previousNameStored = map.get(previousName.getAlias());

                            //if it hasn't been stored, it's the first occurrence of this alias text, store it
                            if (previousNameStored == null) {
                                map.put(previousName.getAlias(), previousName);
                            } else {  //if it's already been stored, just increment the pub count
                                previousNameStored.setPublicationCount(previousNameStored.getPublicationCount() + previousName.getPublicationCount());
                                map.put(previousNameStored.getAlias(), previousNameStored);
                            }
                        }

                        list = new ArrayList(map.values());

                        Collections.sort(list);

                        return list;
                    }
                })
                .list();
    }

    @Override
    public List<Feature> getFeaturesByMarker(Marker marker) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct fmrel.feature from  FeatureMarkerRelationship fmrel" +
                "     where fmrel.marker = :marker " +
                " and fmrel.type  = :relation ";


        Query query = session.createQuery(hql);

        query.setParameter("marker", marker);
        query.setString("relation", FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF.toString());

        return (List<Feature>) query.list();
    }

    public List<Feature> getFeaturesByConstruct(Marker marker) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct ftr from Feature ftr, FeatureMarkerRelationship fmrel" +
                "     where fmrel.marker = :marker " +
                " and fmrel.type in (:relation1, :relation2)  " +
                " and fmrel.feature = ftr " +
                " order by ftr.name ";

        Query query = session.createQuery(hql);

        query.setParameter("marker", marker);
        query.setString("relation1", FeatureMarkerRelationshipTypeEnum.CONTAINS_INNOCUOUS_SEQUENCE_FEATURE.toString());
        query.setString("relation2", FeatureMarkerRelationshipTypeEnum.CONTAINS_PHENOTYPIC_SEQUENCE_FEATURE.toString());

        return (List<Feature>) query.list();
    }

    public int deleteFeatureFromTracking(String featureZdbId) {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("delete from FeatureTracking ft where ft.feature.zdbID=:featureZdbId");
        query.setParameter("featureZdbId", featureZdbId);
        return query.executeUpdate();
    }

    public Set<Feature> getFeaturesCreatedBySequenceTargetingReagent(SequenceTargetingReagent sequenceTargetingReagent) {
        if (sequenceTargetingReagent == null)
            return null;

        Session session = currentSession();
        String hql = "select fmrel from FeatureMarkerRelationship fmrel " +
                " where fmrel.featureMarkerRelationshipType.name = :createdBy " +
                "   and fmrel.marker.zdbID = :strZDBid ";

        Query query = session.createQuery(hql);
        query.setParameter("createdBy", "created by");
        query.setParameter("strZDBid", sequenceTargetingReagent.getZdbID());
        List<FeatureMarkerRelationship> featureMarkerRelationships = (List<FeatureMarkerRelationship>) query.list();

        if (featureMarkerRelationships == null || featureMarkerRelationships.size() == 0)
            return null;

        Set<Feature> featuresCreatedBySTR = new HashSet<Feature>();
        for (Iterator iterator = featureMarkerRelationships.iterator(); iterator.hasNext(); ) {
            FeatureMarkerRelationship fmr = (FeatureMarkerRelationship) iterator.next();
            featuresCreatedBySTR.add(fmr.getFeature());
        }
        return featuresCreatedBySTR;
    }

    @Override
    public void saveFeature(Feature feature, Publication publication) {

        currentSession().save(feature);
        // create standard attribution
        PublicationAttribution ra = new PublicationAttribution();
        ra.setPublication(publication);
        ra.setDataZdbID(feature.getZdbID());
        ra.setSourceType(RecordAttribution.SourceType.STANDARD);

        //add another record for "Feature type" source type attribution
        PublicationAttribution featureTypeAttribution = new PublicationAttribution();
        featureTypeAttribution.setPublication(publication);
        featureTypeAttribution.setDataZdbID(feature.getZdbID());
        featureTypeAttribution.setSourceType(RecordAttribution.SourceType.FEATURE_TYPE);

        Set<PublicationAttribution> set = new HashSet<>(2);
        set.add(ra);
        set.add(featureTypeAttribution);
        feature.setPublications(set);
        currentSession().flush();

        // create attributions for feature note
        if (feature.getExternalNotes() != null) {
            for (FeatureNote note : feature.getExternalNotes()) {
                savePublicationAttribution(publication, note.getZdbID());
            }
        }
        // create attributions for mutation details: DNA
        if (feature.getFeatureProteinMutationDetail() != null) {
            savePublicationAttribution(publication, feature.getFeatureProteinMutationDetail().getZdbID());
        }
        // create attributions for mutation details: Protein
        if (feature.getFeatureDnaMutationDetail() != null) {
            savePublicationAttribution(publication, feature.getFeatureDnaMutationDetail().getZdbID());
        }
        // create attribution for transcripts
        if (feature.getFeatureTranscriptMutationDetailSet() != null) {
            for (FeatureTranscriptMutationDetail detail : feature.getFeatureTranscriptMutationDetailSet()) {
                savePublicationAttribution(publication, detail.getZdbID());
            }
        }

        // create Attribution for feature alias
        if (CollectionUtils.isNotEmpty(feature.getAliases())) {
            for (FeatureAlias alias : feature.getAliases()) {
                PublicationAttribution pa = new PublicationAttribution();
                pa.setPublication(publication);
                pa.setSourceType(RecordAttribution.SourceType.STANDARD);
                pa.setDataZdbID(alias.getZdbID());
                Set<PublicationAttribution> pubattr = new HashSet<>();
                pubattr.add(pa);
                alias.setPublications(pubattr);
            }
        }

    }

    private void savePublicationAttribution(Publication publication, String zdbID) {
        PublicationAttribution pa = new PublicationAttribution();
        pa.setPublication(publication);
        pa.setDataZdbID(zdbID);
        pa.setSourceType(RecordAttribution.SourceType.STANDARD);
        currentSession().save(pa);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getMutagensForFeatureType(FeatureTypeEnum featureTypeEnum) {

        String sql = "select distinct ftmgm_mutagen from feature_type_mutagen_group_member " +
                " where ftmgm_feature_type = :featureType " +
                " order by ftmgm_mutagen";

        return (List<String>) HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("featureType", featureTypeEnum.name())
                .list();

    }

    @Override
    public void update(Feature feature, Set<FeatureTranscriptMutationDetail> addTranscriptAttribution, String publicationID) {
        HibernateUtil.currentSession().update(feature);
        HibernateUtil.currentSession().flush();

        if (feature.getFeatureTranscriptMutationDetailSet() != null) {
            for (FeatureTranscriptMutationDetail detail : feature.getFeatureTranscriptMutationDetailSet()) {
                if (addTranscriptAttribution.contains(detail))
                    infrastructureRepository.insertMutationDetailAttribution(detail.getZdbID(), publicationID);
            }
        }
    }
}

