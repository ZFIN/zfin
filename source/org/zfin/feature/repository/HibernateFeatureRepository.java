package org.zfin.feature.repository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.zfin.feature.*;
import org.zfin.feature.presentation.FeatureLabEntry;
import org.zfin.feature.presentation.FeaturePrefixLight;
import org.zfin.feature.presentation.LabLight;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.Mutagee;
import org.zfin.gwt.root.dto.Mutagen;
import org.zfin.infrastructure.DataAlias;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.people.FeatureSource;
import org.zfin.people.Lab;
import org.zfin.people.LabFeaturePrefix;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;


/**
 * Hibernate implementation of the Antibody Repository.
 */
public class HibernateFeatureRepository implements FeatureRepository {

    private static InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();

    private Logger logger  = Logger.getLogger(HibernateFeatureRepository.class) ;

    public Feature getFeatureByID(String zdbID) {
        return (Feature) HibernateUtil.currentSession().get(Feature.class,zdbID);
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
     * @param publicationID publication
     * @return list of features
     */
    @SuppressWarnings("unchecked")
    public List<Feature> getFeaturesByPublication(String publicationID) {
        Session session = currentSession();
        String hql = "select feature from Feature as feature, " +
                "PublicationAttribution as attribution " +
                "where  attribution.dataZdbID = feature.zdbID AND " +
                "      attribution.publication.zdbID = :pubID " +
                "      order by feature.abbreviationOrder";

        Query query = session.createQuery(hql);
        query.setParameter("pubID", publicationID);
        return (List<Feature>) query.list();
    }


    @SuppressWarnings("unchecked")
    public List<FeatureMarkerRelationship> getFeatureRelationshipsByPublication(String publicationZdbID){

        Session session = currentSession();
        String hql = "select distinct fmr from FeatureMarkerRelationship as fmr, " +
                "PublicationAttribution as attribution " +
                "where  attribution.dataZdbID = fmr.feature.zdbID AND " +
                "      attribution.publication.zdbID = :pubID " ;

        Query query = session.createQuery(hql);
        query.setParameter("pubID", publicationZdbID);
        List<FeatureMarkerRelationship> featureMarkerRelationships = (List<FeatureMarkerRelationship>) query.list();

        // order 
        Collections.sort(featureMarkerRelationships, new Comparator<FeatureMarkerRelationship>(){
            @Override
            public int compare(FeatureMarkerRelationship o1, FeatureMarkerRelationship o2) {
                return o1.getFeature().getAbbreviationOrder().compareTo(o2.getFeature().getAbbreviationOrder()) ;
            }
        });
        return featureMarkerRelationships ;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getRelationshipTypesForFeatureType(FeatureTypeEnum featureTypeEnum) {

        String sql="\n" +
                "select distinct fmreltype_name " +
                "   from " +
                "   feature_marker_relationship_type, feature_type_group," +
                "   feature_type_group_member" +
                "   where" +
                "   ftrgrpmem_ftr_type= :featureType " +
                "   and ftrgrpmem_ftr_type_group=fmreltype_ftr_type_group";

//                     " --                      $(IF,$(NE,$featcur_reln_add_type,Transgenic Insertion),and fmreltype_name not like 'contains%')" +
        // this line is not necessary, since this is what is already mapped in the database
//        if(!featureType.equals(Feature.Type.TRANSGENIC_INSERTION.toString())){
//            sql += " and fmreltype_name not like 'contains%' ";
//        }

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
                .setString("featureType",featureTypeEnum.name())
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
                .setString("pubZdbID",publicationZdbID)
                .setString("featureRelation",featureRelationshipName)
                .list();
        List<Marker> markers = new ArrayList<Marker>() ;
        for(String zdbId: markerZdbIds){
            Marker m = (Marker) HibernateUtil.currentSession().get(Marker.class,zdbId) ;
            markers.add(m) ;
        }
        return markers ;
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<String> getAllFeaturePrefixes(){
        return HibernateUtil.currentSession().createQuery(
                " select distinct fp.prefixString from FeaturePrefix fp order by fp.prefixString asc ").list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<LabFeaturePrefix> getAllCurrentLabFeaturePrefixesWithFeature(){
        String hql = " select lfp from LabFeaturePrefix lfp join lfp.featurePrefix fp" +
                " where lfp.currentDesignation= :currentLineDesignation "  +
                " and exists ( " +
                " select 't' from Feature f where f.featurePrefix = lfp.featurePrefix " +
                " ) " +
                " order by fp.prefixString asc " +
                " " ;
        return HibernateUtil.currentSession().createQuery(hql)
                .setBoolean("currentLineDesignation",true)
                .list() ;
    }

    /**
     * This is rewritten for speed.
     * @return Gets a reprentation of all of the FeaturePrefixes with their associated labs.
     */
    public List<FeaturePrefixLight> getFeaturePrefixWithLabs(){
        String sql = "select fp.fp_prefix,fp.fp_institute_display,l.zdb_id,l.name, lfp.lfp_current_designation " +
                "from feature_prefix fp " +
                "join lab_feature_prefix lfp on fp.fp_pk_id=lfp.lfp_prefix_id " +
                "join lab l on lfp.lfp_lab_zdb_id=l.zdb_id " +
                "group by fp.fp_prefix, fp.fp_institute_display,l.zdb_id,l.name, lfp.lfp_current_designation " +
                "order by fp.fp_prefix, l.name   " ;
        List<Object[]> results = HibernateUtil.currentSession().createSQLQuery(sql).list() ;
        List<FeaturePrefixLight> featurePrefixLightList = new ArrayList<FeaturePrefixLight>() ;
        FeaturePrefixLight featurePrefixLight = null ;
        String currentPrefix = null ;
        int i = 0 ;
        for(Object[] result : results){
            // if an existing one, then just add the lab
            if(featurePrefixLight!=null
                    && currentPrefix!=null
                    && result[0].toString().equals(currentPrefix)){
                if(Boolean.parseBoolean(result[4].toString())){
                    featurePrefixLight.addLabLight(createLab(result));
                }
            }
            // if result is not equal, or we are at the start or end, add the current and open a new one
            else
            if(i==results.size()-1
                    || currentPrefix==null
                    || !result[0].toString().equals(currentPrefix)){
                // add the current one
                if(featurePrefixLight!=null){
                    featurePrefixLightList.add(featurePrefixLight) ;
                }
                featurePrefixLight = new FeaturePrefixLight();
                currentPrefix = result[0].toString();
                featurePrefixLight.setPrefix(currentPrefix);
                if(Boolean.parseBoolean(result[4].toString())){
                    featurePrefixLight.addLabLight(createLab(result));
                }
                if(result[1]!=null){
                    featurePrefixLight.setInstituteDisplay(result[1].toString());
                }
                else{
                    throw new RuntimeException("Should not get here when iterating over feature prefixes.");
                }
            }
            ++i ;
        }

        return featurePrefixLightList;
    }

    private LabLight createLab(Object[] result){
        LabLight lab = new LabLight() ;
        lab.setZdbID(result[2].toString());
        lab.setName(result[3].toString());
        lab.setCurrentDesignation(Boolean.parseBoolean(result[4].toString()));
        return lab ;
    }

    /**
     * We can use the f.sources[0] notation, because there is a 1-1 relationship between Feature and Lab source.
     * @param prefix
     * @return
     */
    @Override
    public List<FeatureLabEntry> getFeaturesForPrefix(String prefix){
        String hql = " select distinct f , f.sources  from Feature f, FeaturePrefix fp   "
                + " where f.featurePrefix = fp "
                + " and fp.prefixString = :featurePrefix "
                + " order by  f.abbreviationOrder asc "
                + "";
        List<Object[]> featureEntryObjects =  currentSession().createQuery(hql)
                .setParameter("featurePrefix",prefix)
                .list();
        List<FeatureLabEntry> featureLabEntries = new ArrayList<FeatureLabEntry>() ;
        for(Object[] featureEntryObj : featureEntryObjects){
            FeatureLabEntry featureLabEntry = new FeatureLabEntry();
            featureLabEntry.setFeature((Feature) featureEntryObj[0]);
            featureLabEntry.setSourceOrganization(((FeatureSource) featureEntryObj[1]).getOrganization());
            featureLabEntries.add( featureLabEntry ) ;
        }
        return featureLabEntries;

    }

    @Override
    public List<LabFeaturePrefix> getLabFeaturePrefixForPrefix(String prefix){
        String hql = " select distinct lfp from LabFeaturePrefix lfp , Feature f, FeaturePrefix fp "
                + " where f.featurePrefix = fp "
                + " and fp.prefixString = :featurePrefix "
                + " and lfp.featurePrefix = fp "
                + "";
        return currentSession().createQuery(hql).setParameter("featurePrefix",prefix).list();
    }

    @Override
    public List<Lab> getLabsWithFeaturesForPrefix(String prefix){
        String hql = " select distinct lfp.lab from LabFeaturePrefix lfp , Feature f, FeaturePrefix fp "
                + " where f.featurePrefix = fp "
                + " and fp.prefixString = :featurePrefix "
                + " and lfp.featurePrefix = fp "
                + "";
        return currentSession().createQuery(hql).setParameter("featurePrefix",prefix).list();
    }

    public Lab getLabByFeature(Feature ftr) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct l  from  FeatureSource fs, Lab l" +
                "     where fs.feature = :ftr " +
                "    and fs.organization = l" ;


        Query query = session.createQuery(hql);
        query.setParameter("ftr", ftr);
        return ((Lab) query.uniqueResult());

    }

    public String getPrefix(int labPrefixID) {
        Session session = HibernateUtil.currentSession();
        String hqlLab1 = " select fp.prefixString from  FeaturePrefix fp where fp.featurePkID =:labPrefixID ";
        Query queryLab = session.createQuery(hqlLab1);
        queryLab.setParameter("labPrefixID", labPrefixID);
        return (String) queryLab.uniqueResult();
    }

    public String getCurrentPrefixForLab(String labZdbID) {
        Session session = HibernateUtil.currentSession();
        String hqlLab1 = " select fp.prefixString from  LabFeaturePrefix  lfp join lfp.lab l " +
                " join lfp.featurePrefix fp " +
                " where l.zdbID =:labZdbID" +
                " and lfp.currentDesignation =:currentDesignation ";
        Query queryLab = session.createQuery(hqlLab1);
        queryLab.setString("labZdbID", labZdbID);
        queryLab.setBoolean("currentDesignation", true);
        return (String) queryLab.uniqueResult();

    }


    public List<Lab> getLabsOfOriginWithPrefix(){
        String hqlLab = " select distinct lfp.lab from LabFeaturePrefix lfp  "  +
                " where lfp.featurePrefix is not null " +
                " and lfp.lab.name is not null  " +
                " and lfp.currentDesignation = :true" +
                " order by lfp.lab.name ";
        return HibernateUtil.currentSession()
                .createQuery(hqlLab)
                .setBoolean("true",true)
                .list();
    }

    public List<FeaturePrefix> getLabPrefixes(String labName) {
        String hqlLab1 = " select lfp from LabFeaturePrefix lfp  " +
                " join lfp.lab lb " +
                " where lb.name=:labName " +
                " order by lfp.currentDesignation desc, lfp.featurePrefix.prefixString asc";
        List<LabFeaturePrefix> labFeaturePrefixes = HibernateUtil.currentSession().createQuery(hqlLab1)
                .setParameter("labName",labName).list();
        List<FeaturePrefix> featurePrefixes = new ArrayList<FeaturePrefix>() ;
        for(LabFeaturePrefix labFeaturePrefix : labFeaturePrefixes){
            FeaturePrefix featurePrefix = labFeaturePrefix.getFeaturePrefix();
            featurePrefix.setCurrentDesignationForSet(labFeaturePrefix.getCurrentDesignation());
            featurePrefixes.add(featurePrefix);
        }

        if(CollectionUtils.isEmpty(featurePrefixes)){
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
                "     where fp.prefixString = :desig " ;

        return (FeaturePrefix) HibernateUtil.currentSession().createQuery(hql)
                .setParameter("desig",labPrefix)
                .uniqueResult();
    }

    public FeatureAssay addFeatureAssay(Feature ftr, Mutagen mutagen, Mutagee mutagee){
        FeatureAssay ftrAss= new FeatureAssay();
        ftrAss.setFeatAssayFeature(ftr);
        ftrAss.setMutagen(mutagen);
        ftrAss.setMutagee(mutagee);
        HibernateUtil.currentSession().save(ftrAss);
        return ftrAss;
    }


    public FeatureAssay getFeatureAssay(String ftrZdbID) {
        Criteria criteria = HibernateUtil.currentSession().createCriteria(FeatureAssay.class);
        criteria.add(Restrictions.eq("featzdbID", ftrZdbID));
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
    public DataNote addFeatureDataNote(Feature feature, String note, Person curator) {
        logger.debug("enter addMarDataNote");
        DataNote dnote = new DataNote();
        dnote.setDataZdbID(feature.getZdbID());
        logger.debug("markerZdbId for datanote: " + feature.getZdbID());
        dnote.setCurator(curator);
        dnote.setDate(new Date());
        dnote.setNote(note);
        logger.debug("data note curator: " + curator);
        Set<DataNote> dataNotes = feature.getDataNotes();
        if (dataNotes == null) {
            dataNotes = new HashSet<DataNote>();
            dataNotes.add(dnote);
            feature.setDataNotes(dataNotes);
        } else dataNotes.add(dnote);


        HibernateUtil.currentSession().save(dnote);
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

        currentSession().flush();

        int removed = query.executeUpdate();


        infrastructureRepository.deleteActiveDataByZdbID(alias.getZdbID());
        currentSession().flush();

        hql = "delete from FeatureAlias ma " +
                " where ma.dataZdbID = :zdbID ";
        query = currentSession().createQuery(hql);
        query.setString("zdbID", alias.getZdbID());

        removed = query.executeUpdate();
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
                "select l.lg " +
                        "from Linkage l join l.linkageMemberFeatures as m " +
                        " where m.zdbID = :zdbId ");
        query.setParameter("zdbId", feat.getZdbID());
        lgList.addAll(query.list());
        return lgList;
    }


    public List<Feature> getFeaturesByAbbreviation(String name) {
        List<Feature> features = new ArrayList<Feature>();
        Session session = currentSession();

        Criteria criteria1 = session.createCriteria(Feature.class);
        criteria1.add(Restrictions.like("abbreviation", name, MatchMode.START));
        criteria1.addOrder(Order.asc("abbreviationOrder"));
        features.addAll(criteria1.list());

        Criteria criteria2 = session.createCriteria(Feature.class);
        criteria2.add(Restrictions.like("abbreviation", name, MatchMode.ANYWHERE));
        criteria2.add(Restrictions.not(Restrictions.like("abbreviation", name, MatchMode.START)));
        criteria2.addOrder(Order.asc("abbreviationOrder"));
        features.addAll(criteria2.list());
        return features;
    }

    public Feature getFeatureByPrefixAndLineNumber(String prefix, String lineNumber){
        String hql = " select f from Feature f join f.featurePrefix fp " +
                " where f.lineNumber = :lineNumber  " +
                " and fp.prefixString = :prefix " ;
        List<Feature> features =  currentSession().createQuery(hql)
                .setString("lineNumber",lineNumber)
                .setString("prefix",prefix)
                .list();

        if(CollectionUtils.isEmpty(features)){
            logger.debug("no features founds for prefix["+prefix + "] and line number["+lineNumber+"]");
            return null ;
        }
        if(features.size()==1){
            return features.get(0) ;
        }
        else{
            logger.error(""+features.size()+" found for prefix["+prefix + "] and line number["+lineNumber+"]");
            return null ;
        }
    }

    /**
     * @param publicationZdbID Attributed publication zdbID.
     * @return List of feature objects attributed to this pub.
     */
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
        query.setString("relation", FeatureMarkerRelationship.Type.IS_ALLELE_OF.toString());
        query.setString("relationship1", FeatureMarkerRelationship.Type.MARKERS_PRESENT.toString());
        query.setString("relationship2", FeatureMarkerRelationship.Type.MARKERS_MISSING.toString());
        //query.setParameter("type", Marker.Type.GENE);
        //query.setString("type", Marker.Type.GENE.toString());

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
        query.setParameter("relationship", FeatureMarkerRelationship.Type.MARKERS_PRESENT.toString());
        query.setString("feat", feature.getZdbID());


        //query.setString("type", Marker.Type.GENE.toString());

        return (List<Marker>) query.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public String setCurrentLabPrefix(String labZdbId, String prefix) {
        List<LabFeaturePrefix> labFeaturePrefixes = HibernateUtil.currentSession().createCriteria(LabFeaturePrefix.class)
                .add(Restrictions.eq("lab.zdbID",labZdbId))
                .list();
        String hql = " update lab_feature_prefix  " +
                " set lfp_current_designation = :currentDesignation " +
                " where lfp_lab_zdb_id = :labZdbID " +
                " and lfp_prefix_id = :prefix " ;
        Query query = HibernateUtil.currentSession().createSQLQuery(hql) ;
        query.setString("labZdbID",labZdbId);
        for(LabFeaturePrefix labFeaturePrefix : labFeaturePrefixes){
            logger.info("feature prefix before: "+labFeaturePrefix);
            query.setInteger("prefix",labFeaturePrefix.getFeaturePrefix().getFeaturePkID()) ;
            query.setBoolean("currentDesignation",labFeaturePrefix.getFeaturePrefix().getPrefixString().equals(prefix)) ;
            query.executeUpdate();
            HibernateUtil.currentSession().flush();
        }
        return prefix ;
    }
}

