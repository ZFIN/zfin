package org.zfin.feature.repository;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.zfin.database.HibernateUpgradeHelper;
import org.zfin.feature.*;
import org.zfin.feature.presentation.FeatureLabEntry;
import org.zfin.feature.presentation.FeaturePrefixLight;
import org.zfin.feature.presentation.LabLight;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.Mutagee;
import org.zfin.gwt.root.dto.Mutagen;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mapping.FeatureLocation;
import org.zfin.mapping.VariantSequence;
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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.framework.HibernateUtil.currentSession;

@Repository
public class HibernateFeatureRepository implements FeatureRepository {

    private static final InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();

    private final Logger logger = LogManager.getLogger(HibernateFeatureRepository.class);

    public Feature getFeatureByID(String zdbID) {
        return HibernateUtil.currentSession().get(Feature.class, zdbID);
    }

    public FeatureGenomicMutationDetail getFgmdByID(String zdbID) {
        return HibernateUtil.currentSession().get(FeatureGenomicMutationDetail.class, zdbID);
    }

    /**
     * Retrieve a list of all feature for a given publication.
     * Features need to be directly attributed to the publication in question.
     *
     * @param publicationID publication
     * @return list of features
     */
    public List<Feature> getFeaturesByPublication(String publicationID) {
        String hql = """
            select distinct feature from Feature as feature, PublicationAttribution as pubAttribution
            left outer join fetch feature.featureAssay
            left outer join fetch feature.featureMarkerRelations
            left outer join fetch feature.featureDnaMutationDetailSet
            left outer join fetch feature.featureProteinMutationDetailSet
            where pubAttribution.publication.zdbID = :publicationID
            AND pubAttribution.sourceType = :type
            AND feature.zdbID = pubAttribution.dataZdbID
            order by feature.abbreviationOrder
            """;
        Query<Feature> query = HibernateUtil.currentSession().createQuery(hql, Feature.class);
        query.setParameter("publicationID", publicationID);
        query.setParameter("type", RecordAttribution.SourceType.STANDARD);
        return query.getResultList();
    }

    ///change to SQL
    @Override
    public Marker getSingleAllelicGene(String featureZdbId) {

        String hql = "select distinct fmrel.marker from FeatureMarkerRelationship fmrel, Feature feature" +
                     " where fmrel.type in (:relation) and fmrel.feature = feature and feature.zdbID = :featureZdbId";

        Query<Marker> query = currentSession().createQuery(hql, Marker.class);
        query.setParameter("relation", FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF);
        query.setParameter("featureZdbId", featureZdbId);
        return query.uniqueResult();
    }


    @Override
    public List<Marker> getConstruct(String featureZdbId) {
        String hql = "select distinct fmrel1.marker from FeatureMarkerRelationship fmrel1" +
                     " where fmrel1.type in (:innocuous, :phenotypic) " +
                     " and fmrel1.feature.zdbID = :featureZdbId";

        Query<Marker> query = currentSession().createQuery(hql, Marker.class);
        query.setParameter("innocuous", FeatureMarkerRelationshipTypeEnum.CONTAINS_INNOCUOUS_SEQUENCE_FEATURE);
        query.setParameter("phenotypic", FeatureMarkerRelationshipTypeEnum.CONTAINS_PHENOTYPIC_SEQUENCE_FEATURE);
        query.setParameter("featureZdbId", featureZdbId);

        return query.list();
    }

    @Override
    public List<Feature> getSingleAffectedGeneAlleles() {
        return getSingleAffectedGeneAlleles(null);
    }

    public List<Feature> getSingleAffectedGeneAlleles(Feature feature) {
        String hql = """
            select distinct fmrel1.feature
            from FeatureMarkerRelationship fmrel1, Feature ftr
            where ftr = fmrel1.feature
            and ftr.type not in (:transversion, :deficiency, :inversion)
            """;
        if (feature != null) {
            hql += " and ftr = :feature ";
        }

        Query<Feature> query = currentSession().createQuery(hql, Feature.class);
        query.setParameter("transversion", FeatureTypeEnum.TRANSLOC);
        query.setParameter("deficiency", FeatureTypeEnum.DEFICIENCY);
        query.setParameter("inversion", FeatureTypeEnum.INVERSION);
        if (feature != null) {
            query.setParameter("feature", feature);
        }
        return query.list();
    }

    @Override
    public boolean isSingleAffectedGeneAlleles(Feature feature) {


        List<Feature> list = getSingleAffectedGeneAlleles(feature);
        if (list == null) {
            return false;
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy");
            if (feature.getFtrEntryDate() != null) {
                String s = formatter.format(feature.getFtrEntryDate());
                DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("MMM dd yyyy");
                LocalDate date1 = LocalDate.parse(s, formatter1);
                //Alliance loads may happen only twice  a year. checking if date of feature is greater than 6 months
                Date today = new Date();
                String currentDate = formatter.format(today);
                LocalDate date2 = LocalDate.parse(currentDate, formatter1);
                if (date2.toEpochDay() - date1.toEpochDay() > 180) {
                    return list.size() > 0;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

    }

    @Override
    public List<Feature> getFeaturesWithLocationOnAssembly11() {
        String hql = "select distinct fs.feature from FeatureLocation fs where fs.assembly like '%z11' ";
        Query<Feature> query = currentSession().createQuery(hql, Feature.class);
        return query.list();
    }

    @Override
    public List<Feature> getFeaturesWithGenomicMutDets() {
        String hql = "select distinct fs.feature from FeatureGenomicMutationDetail fs ";
        Query<Feature> query = currentSession().createQuery(hql, Feature.class);
        return query.list();
    }

    @Override
    public List<Feature> getNonSaFeaturesWithGenomicMutDets() {
        String hql = """
            select distinct fs.feature
            from FeatureGenomicMutationDetail fs
            where fs.feature.abbreviation not like 'sa%'
            """;

        Query<Feature> query = currentSession().createQuery(hql, Feature.class);
        return query.list();
    }

    @Override
    public List<Feature> getDeletionFeatures() {
        Session session = HibernateUtil.currentSession();
        String hql = "select feat from Feature feat  where feat.type = :type order by feat.abbreviationOrder";
        Query<Feature> query = session.createQuery(hql, Feature.class);
        query.setParameter("type", FeatureTypeEnum.DELETION);
        return query.list();
    }


    public List<FeatureMarkerRelationship> getFeatureRelationshipsByPublication(String publicationZdbID) {

        String hql = """
            select distinct f from FeatureMarkerRelationship f
            join f.feature feature
            left outer join feature.featureProteinMutationDetailSet
            left outer join feature.featureProteinMutationDetailSet
            join feature.publications attribution
            where attribution.publication.zdbID = :zdbID
            """;
        Query<FeatureMarkerRelationship> query = currentSession().createQuery(hql, FeatureMarkerRelationship.class);
        query.setParameter("zdbID", publicationZdbID);
        List<FeatureMarkerRelationship> featureMarkerRelationships = query.list();

        // order
        return featureMarkerRelationships.stream()
            .sorted(Comparator.comparing(o -> o.getFeature().getAbbreviationOrder()))
            .collect(Collectors.toList());
    }


    @Override
    public List<String> getRelationshipTypesForFeatureType(FeatureTypeEnum featureTypeEnum) {

        String sql = """ 
            SELECT DISTINCT
                fmreltype_name
            FROM
                feature_marker_relationship_type,
                feature_type_group,
                feature_type_group_member
            WHERE
                ftrgrpmem_ftr_type = :featureType
                AND ftrgrpmem_ftr_type_group = fmreltype_ftr_type_group
            UNION
            SELECT DISTINCT
                mreltype_name
            FROM
                marker_relationship_type,
                marker_type_group,
                marker_type_group_member
            WHERE
                mtgrpmem_mrkr_type = :featureType
                AND mtgrpmem_mrkr_type_group = mtgrp_name
                AND (mreltype_mrkr_type_group_1 = mtgrpmem_mrkr_type_group
                    OR mreltype_mrkr_type_group_2 = mtgrpmem_mrkr_type_group)
            """;
        return currentSession().createNativeQuery(sql)
            .setParameter("featureType", featureTypeEnum.name())
            .list();
    }

    @Override
    public FeatureTypeGroup getFeatureTypeGroupByName(String name) {
        Session session = HibernateUtil.currentSession();
        Query<FeatureTypeGroup> query = session.createQuery("SELECT ftg FROM FeatureTypeGroup ftg WHERE ftg.name = :name", FeatureTypeGroup.class);
        query.setParameter("name", name);
        return query.uniqueResult();
    }

    @Override
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


        List<String> markerZdbIds = (List<String>) HibernateUtil.currentSession().createNativeQuery(sql)
            .setParameter("pubZdbID", publicationZdbID)
            .setParameter("featureRelation", featureRelationshipName)
            .list();
        List<Marker> markers = new ArrayList<>();
        for (String zdbId : markerZdbIds) {
            Marker m = HibernateUtil.currentSession().get(Marker.class, zdbId);
            markers.add(m);
        }
        return markers;
    }


    @Override
    public List<String> getAllFeaturePrefixes() {
        return HibernateUtil.currentSession().createQuery(
            " select distinct fp.prefixString from FeaturePrefix fp order by fp.prefixString asc ", String.class).list();
    }

    @Override

    /*
     * This is rewritten for speed.
     * @return Gets a representation of all FeaturePrefixes with their associated labs.
     **/
    public List<FeaturePrefixLight> getFeaturePrefixWithLabs() {
        String sql = """
            SELECT fp.fp_prefix AS prefix,
                   fp.fp_institute_display,
                   l.zdb_id,
                   l.NAME       AS nam,
                   sfp.sfp_current_designation
            FROM   feature_prefix fp
                   JOIN source_feature_prefix sfp
                     ON fp.fp_pk_id = sfp.sfp_prefix_id
                   JOIN lab l
                     ON sfp.sfp_source_zdb_id = l.zdb_id
            UNION
            SELECT fp.fp_prefix AS prefix,
                   fp.fp_institute_display,
                   l.zdb_id,
                   l.NAME       AS nam,
                   sfp.sfp_current_designation
            FROM   feature_prefix fp
                   JOIN source_feature_prefix sfp
                     ON fp.fp_pk_id = sfp.sfp_prefix_id
                   JOIN company l
                     ON sfp.sfp_source_zdb_id = l.zdb_id
            GROUP  BY prefix,
                      fp.fp_institute_display,
                      l.zdb_id,
                      nam,
                      sfp.sfp_current_designation
            ORDER  BY prefix,
                      nam
            """;
        List<Object[]> results = HibernateUtil.currentSession().createNativeQuery(sql).list();
        List<FeaturePrefixLight> featurePrefixLightList = new ArrayList<>();
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
     * @param prefix prefix
     */
    @Override
    public List<FeatureLabEntry> getFeaturesForPrefix(String prefix) {
        String hql = """
            select distinct f, s  from Feature f
                                  join f.featurePrefix fp
                                  left join f.sources s
                                  where f.featurePrefix = fp
                                  and fp.prefixString = :featurePrefix
                                  order by  f.abbreviationOrder asc
                                 """;
        List<Object[]> featureEntryObjects = currentSession().createQuery(hql)
            .setParameter("featurePrefix", prefix)
            .list();
        List<FeatureLabEntry> featureLabEntries = new ArrayList<>();
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
        return currentSession().createQuery(hql, OrganizationFeaturePrefix.class).setParameter("featurePrefix", prefix).list();
    }

    @Override
    public List<Organization> getLabsWithFeaturesForPrefix(String prefix) {
        String hql = """ 
            select distinct lfp.organization
            from OrganizationFeaturePrefix lfp , Feature f, FeaturePrefix fp
            where f.featurePrefix = fp
            and fp.prefixString = :featurePrefix
            and lfp.featurePrefix = fp
            """;
        return currentSession().createQuery(hql, Organization.class).setParameter("featurePrefix", prefix).list();
    }

    public Organization getLabByFeature(Feature ftr) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct l  from  FeatureSource fs, Organization l" +
                     "     where fs.feature = :ftr " +
                     "    and fs.organization = l";


        Query<Organization> query = session.createQuery(hql, Organization.class);
        query.setParameter("ftr", ftr);
        return query.uniqueResult();

    }

    public FeatureLocation getLocationByFeature(Feature ftr) {
        Session session = HibernateUtil.currentSession();
        String hql = "select fs  from  FeatureLocation fs " +
                     "     where fs.feature = :feature and fs.assembly like '%z1%' order by fs.assembly desc  ";

        Query<FeatureLocation> query = session.createQuery(hql, FeatureLocation.class);
        query.setParameter("feature", ftr);
        query.setMaxResults(1);
        FeatureLocation fl = query.uniqueResult();

        if (fl == null) {
            String hql1 = "select fs  from  FeatureLocation fs " +
                          "     where fs.feature = :feature and fs.assembly like '%9%' order by fs.assembly desc ";

            Query<FeatureLocation> query1 = session.createQuery(hql1, FeatureLocation.class);
            query1.setParameter("feature", ftr);
            return query1.uniqueResult();
        }
        return fl;
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
        Query<String> queryLab = session.createQuery(hqlLab1, String.class);
        queryLab.setParameter("labZdbID", labZdbID);
        queryLab.setParameter("currentDesignation", true);
        return queryLab.uniqueResult();

    }


    public List<Organization> getLabsOfOriginWithPrefix() {
        String hqlLab = """ 
            select distinct lfp.organization from OrganizationFeaturePrefix lfp
            where lfp.featurePrefix is not null
            and lfp.organization.name is not null
            and lfp.currentDesignation = true
            """;
        Query<Organization> query = HibernateUtil.currentSession().createQuery(hqlLab, Organization.class);
        List<Organization> organizations = query.list();
        organizations.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        return organizations;
    }


    public List<FeaturePrefix> getLabPrefixes(String labName) {
        return getLabPrefixes(labName, true);
    }

    /**
     * Get the next zf line number
     *
     * @return
     */
    public String getNextZFLineNum() {
        return getNextLineNumberForLabPrefix(FeaturePrefix.ZF);
    }

    public String getNextLineNumberForLabPrefix(String labPrefix) {
        return getNextLineNumberForLabPrefix(getFeaturePrefixByPrefix(labPrefix));
    }

    public String getNextLineNumberForLabPrefix(FeaturePrefix labPrefix) {
        return getNextLineNumberForLabPrefixWithoutFeatureTrackingCollision(labPrefix);
    }

    /**
     * Get the next line number for lab. First look at the feature table and add 1 to the max value.
     * Then, check if there is a feature tracking entry with the same line number. If so, increment the line number
     * until there is no collision.
     *
     * @return next line number (without collision)
     */
    private String getNextLineNumberForLabPrefixWithoutFeatureTrackingCollision(FeaturePrefix labPrefix) {
        Integer nextLine = getNextLineNumberIntegerForLabPrefix(labPrefix);
        while (isExistingFeatureTrackingByAbbreviation(labPrefix.getAbbreviation() + nextLine)) {
            nextLine++;
        }
        return String.valueOf(nextLine);
    }

    /**
     * Get the next line number by looking at the feature table and adding 1 to the max value.
     * Omit from calculation any line numbers that aren't actually numbers
     *
     * @return next line number
     */
    private int getNextLineNumberIntegerForLabPrefix(FeaturePrefix labPrefix) {
        String sql = """
                SELECT
                    max(cast(coalesce(feature_line_number, '0') AS integer)) + 1
                FROM
                    feature
                WHERE
                    is_numeric(feature_line_number)
                    AND feature_lab_prefix_id = :labPrefix 
            """;
        Number result = (Number) (currentSession().createNativeQuery(sql)
            .setParameter("labPrefix", labPrefix.getFeaturePkID())
            .getSingleResult());
        if (result == null) {
            return 1;
        }
        return result.intValue();
    }

    public List<FeaturePrefix> getLabPrefixes(String labName, boolean assignIfEmpty) {
        String hqlLab1 = """
            select lfp from OrganizationFeaturePrefix lfp
            join lfp.organization lb
            where lb.name=:labName
            order by lfp.currentDesignation desc, lfp.featurePrefix.prefixString asc
            """;
        List<OrganizationFeaturePrefix> organizationFeaturePrefixes = currentSession().createQuery(hqlLab1, OrganizationFeaturePrefix.class)
            .setParameter("labName", labName).list();
        return generateFeaturePrefixes(organizationFeaturePrefixes, assignIfEmpty);
    }

    public List<FeaturePrefix> getLabPrefixesById(String labZdbID, boolean assignIfEmpty) {
        String hqlLab1 = " select lfp from OrganizationFeaturePrefix lfp  " +
                         " join lfp.organization lb " +
                         " where lb.zdbID=:labZdbID " +
                         " order by lfp.currentDesignation desc, lfp.featurePrefix.prefixString asc";

        List<OrganizationFeaturePrefix> organizationFeaturePrefixes = HibernateUtil.currentSession().createQuery(hqlLab1, OrganizationFeaturePrefix.class)
            .setParameter("labZdbID", labZdbID).list();

        return generateFeaturePrefixes(organizationFeaturePrefixes, assignIfEmpty);
    }


    public List<FeaturePrefix> getCurrentLabPrefixesById(String labZdbID, boolean assignIfEmpty) {
        String hqlLab1 = " select lfp from OrganizationFeaturePrefix lfp  " +
                         " join lfp.organization lb " +
                         " where lb.zdbID=:labZdbID " +
                         " and lfp.currentDesignation = true " +
                         " order by lfp.currentDesignation desc, lfp.featurePrefix.prefixString asc";

        List<OrganizationFeaturePrefix> organizationFeaturePrefixes = HibernateUtil.currentSession().createQuery(hqlLab1, OrganizationFeaturePrefix.class)
            .setParameter("labZdbID", labZdbID).list();

        return generateFeaturePrefixes(organizationFeaturePrefixes, assignIfEmpty);
    }

    /**
     * This is a helper method for getLabPrefixes and getLabPrefixesById
     *
     * @return featurePrefixes
     */
    private List<FeaturePrefix> generateFeaturePrefixes(List<OrganizationFeaturePrefix> organizationFeaturePrefixes, boolean assignIfEmpty) {
        List<FeaturePrefix> featurePrefixes = new ArrayList<>();
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
        String hql = "select distinct fp from  FeaturePrefix fp" +
                     "     where fp.prefixString = :desig ";

        return HibernateUtil.currentSession().createQuery(hql, FeaturePrefix.class)
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
        Query<FeatureAssay> query = HibernateUtil.currentSession().createQuery("select fa from FeatureAssay fa " +
                                                                               "where fa.feature = :feature", FeatureAssay.class);
        query.setParameter("feature", feature);
        query.setMaxResults(1);
        return query.getSingleResult();
    }

    public VariantSequence getFeatureVariant(Feature feature) {
        Session session = HibernateUtil.currentSession();
        String hqlSeq = " select vs from  VariantSequence vs where vs.vseqDataZDB =:ftrID";
        Query<VariantSequence> queryLab = session.createQuery(hqlSeq, VariantSequence.class);
        queryLab.setParameter("ftrID", feature.getZdbID());
        return (VariantSequence) queryLab.uniqueResult();
    }

    public String getAALink(Feature feature) {
        Session session = HibernateUtil.currentSession();
        String hqlSeq = " select af_file_location from  amsterdam_file ams  where ams.af_feature_zdb_id =:ftrID";
        Query queryLab = session.createNativeQuery(hqlSeq);
        queryLab.setParameter("ftrID", feature.getZdbID());
        return (String) queryLab.uniqueResult();
    }


    public FeatureLocation getAllFeatureLocationsOnGRCz11(Feature feature) {
        String hql = """
            select fl from FeatureLocation fl
            where fl.assembly = :assembly
            AND fl.feature = :feature
            """;
        Query<FeatureLocation> query = HibernateUtil.currentSession().createQuery(hql, FeatureLocation.class);
        query.setMaxResults(1);
        query.setParameter("assembly", "GRCz11");
        query.setParameter("feature", feature);
        return query.getResultList().stream().findFirst().orElse(null);
    }

    public List<FeatureGenomicMutationDetail> getAllFeatureGenomicMutationDetails() {
        return currentSession().createQuery("select fgm from FeatureGenomicMutationDetail fgm", FeatureGenomicMutationDetail.class).list();
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
                     " where mh.featureAlias.zdbID = :zdbID ";
        Query query = currentSession().createQuery(hql);
        query.setParameter("zdbID", alias.getZdbID());
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
        String hql = "select feature from Feature feature where feature.abbreviation = :abbrev";
        Query<Feature> query = currentSession().createQuery(hql, Feature.class);
        query.setParameter("abbrev", name);
        Optional<Feature> first = query.getResultList().stream().findFirst();
        return first.orElse(null);
    }

    @Override
    public FeatureTracking getFeatureByAbbreviationInTrackingTable(String featTrackingFeatAbbrev) {
        Session session = HibernateUtil.currentSession();
        String hqlFtrTrack = "from FeatureTracking where featTrackingFeatAbbrev = :featTrackingFeatAbbrev";
        Query<FeatureTracking> queryTracker = session.createQuery(hqlFtrTrack, FeatureTracking.class);
        queryTracker.setParameter("featTrackingFeatAbbrev", featTrackingFeatAbbrev);
        return queryTracker.uniqueResult();
    }

    public String getFeatureByIDInTrackingTable(String featTrackingFeatZdbID) {
        Session session = HibernateUtil.currentSession();
        String hqlFtrTrack = " select ft.featTrackingFeatAbbrev from  FeatureTracking ft where ft.feature.zdbID =:featTrackingFeatZdbID ";
        Query queryTracker = session.createQuery(hqlFtrTrack);
        queryTracker.setParameter("featTrackingFeatZdbID", featTrackingFeatZdbID);
        return (String) queryTracker.uniqueResult();
    }

    /**
     * Check if there exists an entry in the feature tracking table already for the given abbreviation
     *
     * @param abbreviation
     * @return
     */
    @Override
    public boolean isExistingFeatureTrackingByAbbreviation(String abbreviation) {
        String sql = "SELECT * FROM feature_tracking WHERE ft_feature_abbrev = :abbrev";
        List results = currentSession().createNativeQuery(sql).setParameter("abbrev", abbreviation).list();
        return !results.isEmpty();
    }

    public TreeSet<String> getFeatureLG(Feature feat) {
        Session session = HibernateUtil.currentSession();
        TreeSet<String> lgList = new TreeSet<String>();


        String hql = "select distinct mm.lg" +
                     "  from MappedMarker mm" +
                     "   where mm.marker.zdbID=:ftr ";
        Query query = session.createQuery(hql);
        query.setParameter("ftr", feat.getZdbID());
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
        Session session = currentSession();

        Query<Feature> query = session.createQuery("from Feature where abbreviation like '" + name + "%'" +
                                                   " order by abbreviationOrder", Feature.class);
        List<Feature> features = new ArrayList<>(query.list());

        Query<Feature> query2 = session.createQuery("from Feature where abbreviation like '%" + name + "%' " +
                                                    " and abbreviation not like '" + name + "%' " +
                                                    " order by abbreviationOrder", Feature.class);
        features.addAll(query2.list());
        return features;
    }

    public Feature getFeatureByPrefixAndLineNumber(String prefix, String lineNumber) {
        String hql = """
            select f from Feature f join f.featurePrefix fp
            where f.lineNumber = :lineNumber
            and fp.prefixString = :prefix
            """;
        List<Feature> features = currentSession().createQuery(hql, Feature.class)
            .setParameter("lineNumber", lineNumber)
            .setParameter("prefix", prefix)
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

    public List<Feature> getFeaturesForStandardAttribution(Publication publication) {
        String hql = "select f from PublicationAttribution pa , Feature f " +
                     " where pa.dataZdbID=f.zdbID and pa.publication.zdbID= :pubZdbID  " +
                     " and pa.sourceType= :sourceType  ";
        Query<Feature> query = HibernateUtil.currentSession().createQuery(hql, Feature.class);
        query.setParameter("pubZdbID", publication.getZdbID());
        query.setParameter("sourceType", PublicationAttribution.SourceType.STANDARD);
        return query.list();
    }

    public List<Marker> getMarkersByFeature(Feature feature) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct fmrel.marker from  FeatureMarkerRelationship fmrel, Marker m" +
                     "     where fmrel.feature.zdbID = :feat" +
                     " and fmrel.type in (:relation, :relationship1, :relationship2) " +
                     " and fmrel.marker=m ";


        Query<Marker> query = session.createQuery(hql, Marker.class);

        query.setParameter("feat", feature.getZdbID());
        query.setParameter("relation", FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF);
        query.setParameter("relationship1", FeatureMarkerRelationshipTypeEnum.MARKERS_PRESENT);
        query.setParameter("relationship2", FeatureMarkerRelationshipTypeEnum.MARKERS_MISSING);
        return query.list();
    }

    public List<Marker> getMarkerIsAlleleOf(Feature feature) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select distinct fmrel.marker from  FeatureMarkerRelationship fmrel, Marker m
                 where fmrel.feature.zdbID = :feat
             and fmrel.type = :relation
             and fmrel.marker=m
            """;

        Query<Marker> query = session.createQuery(hql, Marker.class);
        query.setParameter("feat", feature.getZdbID());
        query.setParameter("relation", FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF);
        return query.list();
    }

    @Override
    public List<Marker> getMarkersPresentForFeature(Feature feature) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct fmrel.marker from  FeatureMarkerRelationship fmrel, Marker m" +
                     "     where fmrel.feature.zdbID = :feat" +
                     " and fmrel.type=:relationship " +
                     " and fmrel.marker=m " +
                     " and m.markerType =:type";

        Query<Marker> query = session.createQuery(hql, Marker.class);
        query.setParameter("relationship", FeatureMarkerRelationshipTypeEnum.MARKERS_PRESENT);
        query.setParameter("feat", feature.getZdbID());


        //SEE ZFIN-8676 before uncommenting?
        //query.setParameter("type", Marker.Type.GENE);

        return (List<Marker>) query.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public String setCurrentPrefix(String organizationZdbId, String prefix) {
        List<OrganizationFeaturePrefix> organizationFeaturePrefixes = HibernateUtil.currentSession().createQuery("from OrganizationFeaturePrefix where organization.zdbID = :zdbID",
                OrganizationFeaturePrefix.class)
            .setParameter("zdbID", organizationZdbId)
            .list();
        String hql = " update source_feature_prefix  " +
                     " set sfp_current_designation = :currentDesignation " +
                     " where sfp_source_zdb_id = :organizationZdbID " +
                     " and sfp_prefix_id = :prefix ";
        Query query = HibernateUtil.currentSession().createNativeQuery(hql);
        query.setParameter("organizationZdbID", organizationZdbId);
        String returnedPrefix = null;
        for (OrganizationFeaturePrefix organizationFeaturePrefix : organizationFeaturePrefixes) {
            logger.info("feature prefix before: " + organizationFeaturePrefix);
            query.setParameter("prefix", organizationFeaturePrefix.getFeaturePrefix().getFeaturePkID());
            if (organizationFeaturePrefix.getFeaturePrefix().getPrefixString().equals(prefix)) {
                query.setParameter("currentDesignation", true);
                returnedPrefix = prefix;
            } else {
                query.setParameter("currentDesignation", false);
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


    @Override
    public List<Feature> getFeaturesForLab(String zdbID) {
        return getFeaturesForLab(zdbID, 0);
    }


    @Override
    public List<Feature> getFeaturesForLab(String zdbID, int numberOfRecords) {
        Pagination pagination = new Pagination();
        pagination.setLimit(numberOfRecords);
        PaginationResult<Feature> result = getFeaturesForLab(zdbID, pagination);
        return result.getPopulatedResults();
    }

    @Override
    public PaginationResult<Feature> getFeaturesForLab(String zdbID, Pagination pagination) {
        Session session = currentSession();
        String hql = """
            select distinct feature from Feature feature
            join feature.featureAssay
            join feature.sources source
            where source.organization.zdbID = :ID
            order By feature.abbreviationOrder
            """;
        Query<Feature> query = session.createQuery(hql, Feature.class);
        query.setParameter("ID", zdbID);

        ScrollableResults scrollableResults = query.scroll();
        List<Feature> list = new ArrayList<>();

        if (pagination.getStart() == 0) {
            scrollableResults.beforeFirst();
        } else {
            scrollableResults.setRowNumber(pagination.getStart());
        }

        while (scrollableResults.next() && ((pagination.getLimit() == 0) || (list.size() < pagination.getLimit()))) {
            list.add((Feature) scrollableResults.get());//TODO (ZFIN-9354): hibernate migration double check logic
        }

        if (!scrollableResults.isLast()) {
            scrollableResults.last();
        }
        int total = scrollableResults.getRowNumber() + 1;
        PaginationResult<Feature> paginationResult = new PaginationResult<>(total, list);
        scrollableResults.close();
        return paginationResult;
    }

    @Override
    public int setLabOfOriginForFeature(Organization lab, Feature feature) {
        String sql = " update int_data_source " +
                     " set ids_source_zdb_id = :newLabZdbId " +
                     " where ids_data_zdb_id  = :featureZdbId  ";
        Query query = HibernateUtil.currentSession().createNativeQuery(sql)
            .setParameter("newLabZdbId", lab.getZdbID())
            .setParameter("featureZdbId", feature.getZdbID());
        int recordsUpdated = query.executeUpdate();
        if (recordsUpdated != 1) {
            logger.error("A feature must have had multiple labs: " + feature.getZdbID()
                         + " records updated: " + recordsUpdated);
        }
        return recordsUpdated;
    }

    @Override
    public void deleteLabOfOriginForFeature(Feature feature) {
        String sql = " delete FROM int_data_source " +
                     " where ids_data_zdb_id  = :featureZdbId  ";
        Query query = HibernateUtil.currentSession().createNativeQuery(sql)
            .setParameter("featureZdbId", feature.getZdbID());
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
        Query query = HibernateUtil.currentSession().createNativeQuery(sql)
            .setParameter("newLabZdbId", labOfOrigin)
            .setParameter("featureZdbId", feature.getZdbID());
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
        Query<FeaturePrefix> queryLab = session.createQuery(hqlLab1, FeaturePrefix.class);
        queryLab.setParameter("prefix", prefix);
        return queryLab.uniqueResult();
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
                     "set sfp_current_designation = false " +
                     "where sfp_source_zdb_id=:labZdbId ";
        return HibernateUtil.currentSession().createNativeQuery(sql)
            .setParameter("labZdbId", zdbID)
            .executeUpdate();

    }

    @Override
    public List<PreviousNameLight> getPreviousNamesLight(final Genotype genotype) {
        String sql = """
            select da.dalias_alias, ra.recattrib_source_zdb_id, da.dalias_zdb_id
            from data_alias da
               join alias_group ag on da.dalias_group_id=ag.aliasgrp_pk_id
               left outer join record_attribution ra on ra.recattrib_data_zdb_id=da.dalias_zdb_id
               where dalias_data_zdb_id = :markerZdbID
               and aliasgrp_pk_id = dalias_group_id
                and aliasgrp_name = 'alias'
            """;
        Query qry = currentSession().createNativeQuery(sql)
                .setParameter("markerZdbID", genotype.getZdbID());

        List<PreviousNameLight> result = HibernateUpgradeHelper.setTupleResultAndListTransformer(qry,


                (Object[] tuple, String[] aliases) -> {
                    PreviousNameLight previousNameLight = new PreviousNameLight(genotype.getName());
                    previousNameLight.setMarkerZdbID(genotype.getZdbID());
                    previousNameLight.setAlias(tuple[0].toString());
                    previousNameLight.setAliasZdbID(tuple[2].toString());
                    if (tuple[1] != null) {
                        previousNameLight.setPublicationZdbID(tuple[1].toString());
                        previousNameLight.setPublicationCount(1);
                    }

                    return previousNameLight;
                },


                (List<Object> list) -> {
                    Map<String, PreviousNameLight> map = new HashMap<>();
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
                    return list;
                });
        Collections.sort(result);
        return result;
    }

    @Override
    public List<Feature> getFeaturesByMarker(Marker marker) {
        Session session = HibernateUtil.currentSession();

        String hql = """
                 select distinct fmrel.feature
                 from  FeatureMarkerRelationship fmrel
                 where fmrel.marker = :marker
                 and fmrel.type  = :relation
            """;

        Query<Feature> query = session.createQuery(hql, Feature.class);
        query.setParameter("marker", marker);
        query.setParameter("relation", FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF);
        return query.list();
    }


    public List<Feature> getFeaturesByConstruct(Marker marker) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct ftr from Feature ftr, FeatureMarkerRelationship fmrel" +
                     "     where fmrel.marker = :marker " +
                     " and fmrel.type in (:relation1, :relation2)  " +
                     " and fmrel.feature = ftr " +
                     " order by ftr.name ";

        Query<Feature> query = session.createQuery(hql, Feature.class);

        query.setParameter("marker", marker);
        query.setParameter("relation1", FeatureMarkerRelationshipTypeEnum.CONTAINS_INNOCUOUS_SEQUENCE_FEATURE);
        query.setParameter("relation2", FeatureMarkerRelationshipTypeEnum.CONTAINS_PHENOTYPIC_SEQUENCE_FEATURE);

        return query.list();
    }

    public List<Marker> getConstructsByFeature(Feature feature) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct marker from FeatureMarkerRelationship fmrel" +
                     "     where fmrel.feature = :feature " +
                     " and fmrel.type in (:relation1, :relation2) ";

        Query<Marker> query = session.createQuery(hql, Marker.class);

        query.setParameter("feature", feature);
        query.setParameter("relation1", FeatureMarkerRelationshipTypeEnum.CONTAINS_INNOCUOUS_SEQUENCE_FEATURE);
        query.setParameter("relation2", FeatureMarkerRelationshipTypeEnum.CONTAINS_PHENOTYPIC_SEQUENCE_FEATURE);

        return query.list();
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
        String hql = """
            select fmrel from FeatureMarkerRelationship fmrel
            where fmrel.featureMarkerRelationshipType.name = :createdBy
            and fmrel.marker.zdbID = :strZDBid
            """;

        Query<FeatureMarkerRelationship> query = session.createQuery(hql, FeatureMarkerRelationship.class);
        query.setParameter("createdBy", "created by");
        query.setParameter("strZDBid", sequenceTargetingReagent.getZdbID());
        List<FeatureMarkerRelationship> featureMarkerRelationships = query.list();

        if (featureMarkerRelationships == null || featureMarkerRelationships.size() == 0)
            return null;

        Set<Feature> featuresCreatedBySTR = new HashSet<>();
        for (FeatureMarkerRelationship fmr : featureMarkerRelationships) {
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
        if (feature.getFeatureGenomicMutationDetail() != null) {
            savePublicationAttribution(publication, feature.getFeatureGenomicMutationDetail().getZdbID());
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

        return (List<String>) HibernateUtil.currentSession().createNativeQuery(sql)
            .setParameter("featureType", featureTypeEnum.name())
            .list();

    }

    @Override
    public void update(Feature feature, Set<FeatureTranscriptMutationDetail> addTranscriptAttribution, String publicationID) {
        HibernateUtil.currentSession().saveOrUpdate(feature);
        HibernateUtil.currentSession().flush();
        currentSession().refresh(feature);
        if (feature.getFeatureTranscriptMutationDetailSet() != null) {
            for (FeatureTranscriptMutationDetail detail : feature.getFeatureTranscriptMutationDetailSet()) {
                if (addTranscriptAttribution.contains(detail))
                    infrastructureRepository.insertMutationDetailAttribution(detail.getZdbID(), publicationID);
            }
        }
    }

    @Override
    public void deleteFeatureProteinMutationDetail(FeatureProteinMutationDetail detail) {
        //I changed this to delete this record entirely from zdb active data so as to remove lingering record attributions as well.
        infrastructureRepository.deleteActiveDataByZdbID(detail.getZdbID());
    }

    public void deleteFeatureGenomicMutationDetail(FeatureGenomicMutationDetail detail) {
        //I changed this to delete this record entirely from zdb active data so as to remove lingering record attributions as well.
        infrastructureRepository.deleteActiveDataByZdbID(detail.getZdbID());
    }

    @Override
    public Long getFeaturesForLabCount(String zdbID) {
        String hql = """
            select count(*) from Feature f join f.sources s
                                 where s.organization.zdbID = :zdbID
                                 """;
        return (Long) currentSession().createQuery(hql)
            .setParameter("zdbID", zdbID).getSingleResult();
    }

    @Override
    public int getNumberOfFeaturesForConstruct(Marker construct) {
        String sql = """
            select count(distinct fmrel_ftr_zdb_id) as num
            from feature_marker_relationship
            where fmrel_mrkr_zdb_id = :zdbID
            and fmrel_type in (:relation1, :relation2)
            """;
        Query<Integer> query = currentSession().createNativeQuery(sql, Integer.class);
        query.setParameter("zdbID", construct.getZdbID());
        query.setParameter("relation1", FeatureMarkerRelationshipTypeEnum.CONTAINS_INNOCUOUS_SEQUENCE_FEATURE.toString()); //NativeQuery so we need to pass strings
        query.setParameter("relation2", FeatureMarkerRelationshipTypeEnum.CONTAINS_PHENOTYPIC_SEQUENCE_FEATURE.toString()); //NativeQuery so we need to pass strings
        return query.uniqueResult();
    }

    @Override
    public List<Feature> getAllFeatureList(int firstNIds) {
        Session session = HibernateUtil.currentSession();
        String hql = "select f from Feature f order by f.zdbID";
        Query<Feature> query = session.createQuery(hql, Feature.class);
        if (firstNIds > 0)
            query.setMaxResults(firstNIds);
        return query.list();
    }
}

