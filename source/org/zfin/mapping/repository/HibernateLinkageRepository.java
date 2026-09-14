package org.zfin.mapping.repository;

import jakarta.persistence.Tuple;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.Updates;
import org.zfin.infrastructure.ZdbID;
import org.zfin.mapping.*;
import org.zfin.mapping.importer.AGPEntry;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.sequence.gff.Assembly;

import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;

@Repository
public class HibernateLinkageRepository implements LinkageRepository {

    private Logger logger = LogManager.getLogger(HibernateLinkageRepository.class);

    @Autowired
    private ProfileService profileService;

    public HibernateLinkageRepository() {
    }

    public List<String> getDirectMappedMarkers(Marker marker) {
        Session session = currentSession();

        String hql = " select distinct  mm.lg " +
                     " from MappedMarker  mm where " +
                     " mm.marker.zdbID = :markerZdbID order by mm.lg  ";
        Query query = session.createQuery(hql);
        query.setParameter("markerZdbID", marker.getZdbID());


        List<String> lgs = query.list();

        return lgs;
    }


    public TreeSet<String> getChromosomeLocations(Marker marker) {
        List<MarkerGenomeLocation> genomeLocationList = getGenomeLocation(marker);
        TreeSet<String> locations = new TreeSet<>();
        for (MarkerGenomeLocation chromosome : genomeLocationList) {
            locations.add(chromosome.getChromosome());
        }
        return locations;
    }

    /**
     * Retrieve all mapping panels.
     *
     * @return list of panels.
     */
    public List<Panel> getAllPanels() {
        Session session = HibernateUtil.currentSession();
        return (List<Panel>) session.createQuery("from Panel").list();
    }

    @Override
    public List<MeioticPanel> getMeioticPanels() {
        Session session = HibernateUtil.currentSession();
        return (List<MeioticPanel>) session.createQuery("from MeioticPanel").list();
    }

    @Override
    public List<RadiationPanel> getRadiationPanels() {
        Session session = HibernateUtil.currentSession();
        return (List<RadiationPanel>) session.createQuery("from RadiationPanel").list();
    }

    @Override
    public Panel getPanelByName(String name) {
        Session session = HibernateUtil.currentSession();
        String hql = "from Panel where name = :name";
        Query query = session.createQuery(hql);
        query.setParameter("name", name);
        return (Panel) query.uniqueResult();
    }

    @Override
    public Panel getPanelByAbbreviation(String name) {
        Session session = HibernateUtil.currentSession();
        String hql = "from Panel where abbreviation = :name";
        Query query = session.createQuery(hql);
        query.setParameter("name", name);
        return (Panel) query.uniqueResult();
    }

    @Override
    public List<MappedMarker> getMappedMarkers(ZdbID marker) {
        Query query = HibernateUtil.currentSession().createQuery("from MappedMarker where marker.zdbID = :marker");
        query.setParameter("marker", marker.getZdbID());
        return (List<MappedMarker>) query.list();
    }

    @Override
    public List<Linkage> getLinkagesForMarker(Marker marker) {
        Query query = HibernateUtil.currentSession().createQuery("" +
                                                                 "select link from Linkage as link, LinkageMember as linkageMember " +
                                                                 "where linkageMember member of link.linkageMemberSet " +
                                                                 "and linkageMember.markerOneZdbId = :ID ");
        query.setParameter("ID", marker.getZdbID());
        return (List<Linkage>) query.list();
    }

    @Override
    public List<Marker> getMappedClonesContainingGene(Marker marker) {
        Query query = HibernateUtil.currentSession().createQuery(
            "select m from  Marker as m, MarkerRelationship as rel " +
            "where rel.secondMarker = :marker  and " +
            "rel.type in :relationshipTypes and " +
            "m = rel.firstMarker ");
        query.setParameter("marker", marker);
        query.setParameterList("relationshipTypes", new MarkerRelationship.Type[]{MarkerRelationship.Type.CLONE_CONTAINS_SMALL_SEGMENT,
            MarkerRelationship.Type.CLONE_CONTAINS_GENE, MarkerRelationship.Type.CLONE_CONTAINS_TRANSCRIPT});
        List<Marker> list = (List<Marker>) query.list();
        if (list == null) {
            list = new ArrayList<>();
        }

        Query query2 = HibernateUtil.currentSession().createQuery(
            "select distinct m from  Marker as m, MarkerRelationship as rel1, MarkerRelationship as rel2  " +
            "where rel1.firstMarker = :marker  and " +
            "rel1.type = :relationshipType1 and " +
            "rel2.type = :relationshipType2 and " +
            "rel2.secondMarker = rel1.secondMarker and " +
            "m = rel2.firstMarker ");
        query2.setParameter("marker", marker);
        query2.setParameter("relationshipType1", MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT);
        query2.setParameter("relationshipType2", MarkerRelationship.Type.CLONE_CONTAINS_TRANSCRIPT);
        List<Marker> list2 = (List<Marker>) query2.list();
        if (list2 != null) {
            list.addAll(list2);
        }
        Set<Marker> set = new HashSet<>(list.size());
        set.addAll(list);
        List<Marker> objects = new ArrayList<>();
        objects.addAll(set);
        return objects;
    }

    @Override
    public List<MappedMarker> getMappedMarkers(Panel panel, ZdbID marker, String lg) {

        String hql = "from MappedMarker ";
        List<String> hqlClauses = new ArrayList<>();
        HashMap<String, Object> parameterMap = new HashMap<>();


        if (lg != null) {
            hqlClauses.add("lg = :lg");
            parameterMap.put("lg", lg);
        }
        if (marker != null) {
            hqlClauses.add("entityID = :entityID");
            parameterMap.put("entityID", marker.getZdbID());
        }
        if (panel != null) {
            hqlClauses.add("panel = :panel");
            parameterMap.put("panel", panel);
        }
        hql += " where " + String.join(" and ", hqlClauses);
        Query<MappedMarker> query = HibernateUtil.currentSession().createQuery(hql, MappedMarker.class);
        parameterMap.forEach(query::setParameter);
        return query.list();

    }

    @Override
    public List<PrimerSet> getPrimerSetList(Marker marker) {
        Query query = HibernateUtil.currentSession().createQuery("from PrimerSet where marker = :marker");
        query.setParameter("marker", marker);
        return (List<PrimerSet>) query.list();
    }

    @Override
    public List<Marker> getMarkersEncodedByMarker(Marker marker) {
        Query query = HibernateUtil.currentSession().createQuery(
            "select m from  Marker as m, MarkerRelationship as rel " +
            "where rel.firstMarker = :marker  and " +
            "rel.type in :relationshipTypes and " +
            "m = rel.secondMarker and " +
            " ( exists (from MappedMarker where marker = m) or " +
            " exists (from LinkageMember as linkageMember " +
            "where linkageMember.markerOneZdbId = :markerID and linkageMember.markerTwoZdbId = m.zdbID))");
        query.setParameter("markerID", marker.getZdbID());
        query.setParameter("marker", marker);
        query.setParameter("relationshipTypes", MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);
        List<Marker> list = (List<Marker>) query.list();
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    @Override
    public List<Marker> getMarkersContainedIn(Marker marker) {
        Query query = HibernateUtil.currentSession().createQuery(
            "select distinct m from  Marker as m, MarkerRelationship as rel, MarkerGenomeLocation as loc " +
            "where rel.firstMarker = :marker  and " +
            "rel.type in :relationshipTypes and " +
            "m = rel.secondMarker and " +
            "loc.marker = rel.secondMarker ");
        query.setParameter("marker", marker);
        query.setParameterList("relationshipTypes", new MarkerRelationship.Type[]{MarkerRelationship.Type.CLONE_CONTAINS_SMALL_SEGMENT,
            MarkerRelationship.Type.CLONE_CONTAINS_GENE});
        List<Marker> list = (List<Marker>) query.list();
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    public List<FeatureGenomeLocation> getFeatureLocations(Marker marker) {
        String queryString = """
            select  loc from FeatureGenomeLocation loc,FeatureMarkerRelationship fmrel
            where fmrel.marker = :marker and
            fmrel.type  = :relation and
            loc.feature = fmrel.feature and loc.assembly in ('GRCz12tu','GRCz11','GRCz10','Zv9') order by loc.feature.abbreviationOrder asc,substring(loc.assembly,4) desc
            """;
        Query<FeatureGenomeLocation> query1 = HibernateUtil.currentSession().createQuery(queryString, FeatureGenomeLocation.class);
        query1.setParameter("marker", marker);
        query1.setParameter("relation", FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF);
        return query1.list();
    }

    /**
     * Return the GenomeLocation for the marker if provided, or the feature if that's provided.
     *
     * @param markerOrFeature (should be an instance of either Marker class or Feature class)
     * @return
     */
    @Override
    public List<GenomeLocation> getGenericGenomeLocation(ZdbID markerOrFeature) {
        List<GenomeLocation> results = new ArrayList<>();
        if (markerOrFeature instanceof Marker) {
            getGenomeLocation((Marker) markerOrFeature).stream().forEach(location -> results.add(location));
        } else if (markerOrFeature instanceof Feature) {
            getGenomeLocation((Feature) markerOrFeature).stream().forEach(location -> results.add(location));
        }
        return results;
    }

    @Override
    public List<LinkageMember> getLinkageMemberForMarker(Marker marker) {
        Query query = HibernateUtil.currentSession().createQuery(
            "from LinkageMember as linkageMember " +
            "where linkageMember.markerOneZdbId = :ID " +
            "order by linkageMember.linkage.publication.shortAuthorList");
        query.setParameter("ID", marker.getZdbID());
        return (List<LinkageMember>) query.list();
    }

    @Override
    public List<MarkerGenomeLocation> getGenomeLocation(Marker marker) {
        Query query = HibernateUtil.currentSession().createQuery(
            "from MarkerGenomeLocation where marker = :marker ");
        query.setParameter("marker", marker);
        return (List<MarkerGenomeLocation>) query.list();
    }

    @Override
    public List<MarkerGenomeLocation> getGenomeLocationByMarkerAndAssembly(Marker marker, Assembly assembly){
        String queryString = """
            from MarkerGenomeLocation
            where marker = :marker
               """;
        if(assembly != null){
            queryString += " AND assembly = :assembly";
        }
        Query<MarkerGenomeLocation> query = HibernateUtil.currentSession().createQuery(queryString, MarkerGenomeLocation.class);
        query.setParameter("marker", marker);
        if(assembly != null) {
            query.setParameter("assembly", assembly.getName());
        }
        return query.list();
    }

    @Override
    public List<MarkerGenomeLocation> getGenomeLocationWithCoordinates(Marker marker) {
        Query<MarkerGenomeLocation> query = HibernateUtil.currentSession().createQuery(
            "from MarkerGenomeLocation mgl where marker = :marker " +
            " and mgl.start is not null and mgl.end is not null", MarkerGenomeLocation.class);
        query.setParameter("marker", marker);
        return query.list();
    }

    @Override
    public List<MarkerGenomeLocation> getGenomeLocation(Marker marker, GenomeLocation.Source... sources) {
        String hql = """
            from MarkerGenomeLocation
            where marker = :marker
            AND source in (:source)
            order by chromosome
            """;
        Query<MarkerGenomeLocation> query = HibernateUtil.currentSession().createQuery(hql, MarkerGenomeLocation.class);
        query.setParameter("marker", marker);
        query.setParameterList("source", sources);
        return query.list();
    }

    @Override
    public List<FeatureGenomeLocation> getGenomeLocation(Feature feature) {
        Query<FeatureGenomeLocation> query = HibernateUtil.currentSession().createQuery(
            "from FeatureGenomeLocation fgl where feature = :feature order by substring(fgl.assembly,4) desc", FeatureGenomeLocation.class);
        query.setParameter("feature", feature);
        return query.list();
    }

    @Override
    public List<FeatureGenomeLocation> getGenomeLocation(Feature feature, GenomeLocation.Source... sources) {
        Query<FeatureGenomeLocation> query = HibernateUtil.currentSession().createQuery("from FeatureGenomeLocation where feature = :feature and source in (:source)", FeatureGenomeLocation.class);
        query.setParameter("feature", feature);
        query.setParameterList("source", sources);
        return query.list();
    }

    @Override
    public List<MarkerGenomeLocation> getPhysicalGenomeLocations(Marker marker) {
        Query<MarkerGenomeLocation> query = HibernateUtil.currentSession().createQuery(
            "from MarkerGenomeLocation as loc where marker = :marker " +
            "and loc.source != :sourceDB", MarkerGenomeLocation.class);
        query.setParameter("marker", marker);
        query.setParameter("sourceDB", GenomeLocation.Source.OTHER_MAPPING);
        return query.list();
    }

    @Override
    public List<FeatureGenomeLocation> getPhysicalGenomeLocations(Feature feature) {
        Query query = HibernateUtil.currentSession().createQuery(
            "from FeatureGenomeLocation as loc where feature = :feature " +
            "and loc.source != :sourceDB");
        query.setParameter("feature", feature);
        query.setParameter("sourceDB", GenomeLocation.Source.OTHER_MAPPING);
        return (List<FeatureGenomeLocation>) query.list();
    }

    @Override
    public List<LinkageMember> getLinkagesForFeature(Feature feature) {
        Query query = HibernateUtil.currentSession().createQuery(
            "from LinkageMember as linkageMember " +
            "where linkageMember.markerOneZdbId = :ID ");
        query.setParameter("ID", feature.getZdbID());
        return (List<LinkageMember>) query.list();
    }

    @Override
    public List<Marker> getESTContainingSnp(Marker snp) {

        Query query = HibernateUtil.currentSession().createQuery(
            "select m from  Marker as m, MarkerRelationship as rel, MarkerType as mtype  " +
            "where rel.secondMarker = :marker  and " +
            "rel.type in :relationshipTypes and " +
            "m = rel.firstMarker and " +
            "m.markerType = mtype and " +
            "mtype.name = :mtype and " +
            " ( exists (from MappedMarker where marker = m) or " +
            " exists (from LinkageMember   " +
            "where markerOneZdbId = :markerID))");
        query.setParameter("marker", snp);
        query.setParameter("markerID", snp.getZdbID());
        query.setParameter("mtype", Marker.Type.EST.toString());
        query.setParameterList("relationshipTypes", new MarkerRelationship.Type[]{MarkerRelationship.Type.CONTAINS_POLYMORPHISM});
        List<Marker> list = (List<Marker>) query.list();
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    @Override
    public List<Marker> getGeneContainingSnp(Marker snp) {
        Query query = HibernateUtil.currentSession().createQuery(
            "select m from  Marker as m, MarkerRelationship as rel, MarkerType as mtype " +
            "where rel.secondMarker = :marker  and " +
            "rel.type in :relationshipTypes and " +
            "m = rel.firstMarker  and " +
            "m.markerType = mtype and " +
            "mtype.name = :mtype ");
        query.setParameter("marker", snp);
        query.setParameter("mtype", Marker.Type.GENE.toString());
        query.setParameterList("relationshipTypes", new MarkerRelationship.Type[]{MarkerRelationship.Type.CONTAINS_POLYMORPHISM});
        List<Marker> list = (List<Marker>) query.list();
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    @Override
    public List<SingletonLinkage> getSingletonLinkage(ZdbID zdbID) {
        Query query = HibernateUtil.currentSession().createQuery(
            "from SingletonLinkage " +
            "where zdbID = :zdbID ");
        query.setParameter("zdbID", zdbID.getZdbID());
        return (List<SingletonLinkage>) query.list();
    }

    @Override
    public Linkage getLinkage(String linkageID) {
        return (Linkage) HibernateUtil.currentSession().load(Linkage.class, linkageID);
    }

    @Override
    public void saveLinkageComment(Linkage linkage, String newComment) {
        if (!profileService.isCurrentSecurityUserRoot()) {
            throw new RuntimeException("No User with permissions found");
        }
        Updates updates = new Updates();
        updates.setOldValue(linkage.getComments());
        updates.setNewValue(newComment);
        updates.setSubmitter(ProfileService.getCurrentSecurityUser());
        updates.setSubmitterName(ProfileService.getCurrentSecurityUser().getFullName());
        updates.setFieldName("Linkage.comments");
        updates.setComments("Updated Linkage Comment field");
        updates.setRecID(linkage.getZdbID());
        updates.setWhenUpdated(new Date());
        linkage.setComments(newComment);
        HibernateUtil.currentSession().save(linkage);
        HibernateUtil.currentSession().save(updates);
    }

    @Override
    public void saveAGPEntry(AGPEntry entry) {
        HibernateUtil.currentSession().save(entry);
    }

    @Override
    public void deleteAllAGPEntries() {
        String hql = "delete from AGPEntry";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.executeUpdate();
    }

    @Override
    public void deleteAllGenomeLocationsBySource(GenomeLocation.Source source) {
        Query query = HibernateUtil.currentSession().createQuery(
            "delete from MarkerGenomeLocation where  " +
            " source = :source ");
        query.setParameter("source", source);
        query.executeUpdate();
    }

    @Override
    public void saveMarkerGenomeLocation(MarkerGenomeLocation markerGenomeLocation) {
        HibernateUtil.currentSession().save(markerGenomeLocation);
    }

    @Override
    public boolean hasGenomeLocation(Marker gene, GenomeLocation.Source source) {
        Query query = HibernateUtil.currentSession().createQuery(
            "from MarkerGenomeLocation where marker = :marker " +
            "AND source = :source ");
        query.setParameter("marker", gene);
        query.setParameter("source", source);
        List list = query.list();
        return CollectionUtils.isNotEmpty(list);
    }

    @Override
    public List<EntityZdbID> getMappedEntitiesByPub(Publication publication) {
        Set<EntityZdbID> list = new HashSet<>();
        Query query = HibernateUtil.currentSession().createQuery(
            "from LinkageMember where linkage.publication = :publication ");
        query.setParameter("publication", publication);
        List<LinkageMember> linkageMemberList = query.list();
        for (LinkageMember linkageMember : linkageMemberList) {
            list.add(linkageMember.getEntityOne());
        }
        Query query2 = HibernateUtil.currentSession().createQuery(
            "from SingletonLinkage where linkage.publication = :publication ");
        query2.setParameter("publication", publication);
        List<SingletonLinkage> linkageMemberListSingle = query2.list();
        for (SingletonLinkage linkage : linkageMemberListSingle) {
            list.add(linkage.getEntity());
        }
        List<EntityZdbID> uniqueList = new ArrayList<>(list);
        uniqueList.sort(
            Comparator.nullsLast(
                Comparator.comparing(EntityZdbID::getEntityType, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(EntityZdbID::getAbbreviationOrder, Comparator.nullsLast(Comparator.naturalOrder())))
        );
        return uniqueList;
    }

    @Override
    public Panel getPanel(String panelID) {
        return HibernateUtil.currentSession().get(Panel.class, panelID);
    }

    @Override
    public List<PanelCount> getPanelCount(Panel panel) {
        if (panel.getAbbreviation().equals("ZMAP")) {
            String sql = """
                 SELECT name,
                                panel_date,
                                ptype,
                                mtype,
                                target_abbrev,
                                zmap_chromosome,
                                count(*)
                        FROM  panels a,zmap_pub_pan_mark b
                  WHERE a.abbrev = b.target_abbrev
                  AND a.abbrev = 'ZMAP' AND zmap_chromosome <> '0'
                        GROUP BY name,panel_date,ptype,target_id,mtype,target_abbrev, zmap_chromosome
                """;
            List<Object[]> list = HibernateUtil.currentSession().createNativeQuery(sql).list();
            List<PanelCount> panelCountList = new ArrayList<>(list.size());
            for (Object[] row : list) {
                PanelCount panelCount = new PanelCount();
                panelCount.setPanel(panel);
                panelCount.setLg((String) row[5]);
                panelCount.setMarkerType((String) row[3]);
                panelCount.setCount((long) row[6]);
                panelCountList.add(panelCount);
            }
            return panelCountList;
        } else {
            Session session = HibernateUtil.currentSession();
            String hql = "from PanelCount where id.panel = :panel order by id.lg";
            Query query = session.createQuery(hql);
            query.setParameter("panel", panel);
            return (List<PanelCount>) query.list();
        }
    }

    /**
     * Every NCBILoader-style genome location whose (accession, entity) pair db_link no longer
     * carries. Native SQL: the two derived columns are set-oriented work the database does far
     * better than a walk over entities, and nothing here needs the rows as objects.
     */
    private static final String DRIFTED_GENOME_LOCATIONS_SQL = """
            with drifted as (
              select l.*,
                     (select string_agg(distinct d.dblink_linked_recid, ',' order by d.dblink_linked_recid)
                        from db_link d
                       where d.dblink_acc_num = l.sfclg_acc_num
                         and d.dblink_fdbcont_zdb_id = :fdbcont) as current_genes
                from sequence_feature_chromosome_location_generated l
               where l.sfclg_location_source = :source
                 and not exists (select 1 from db_link d
                                  where d.dblink_acc_num = l.sfclg_acc_num
                                    and d.dblink_fdbcont_zdb_id = :fdbcont
                                    and d.dblink_linked_recid = l.sfclg_data_zdb_id)
            )
            select d.sfclg_pk_id, d.sfclg_data_zdb_id, d.sfclg_acc_num,
                   d.sfclg_chromosome, d.sfclg_start, d.sfclg_end, d.current_genes,
                   -- Would moving this row onto d.current_genes be refused? Mirrors
                   -- uq_sfclg_unique_location, the constraint that binds for these rows:
                   -- every one of its columns, with IS NOT DISTINCT FROM for its
                   -- NULLS NOT DISTINCT semantics.
                   exists (select 1
                             from sequence_feature_chromosome_location_generated t
                            where t.sfclg_data_zdb_id = d.current_genes
                              and t.sfclg_pk_id <> d.sfclg_pk_id
                              and t.sfclg_acc_num           is not distinct from d.sfclg_acc_num
                              and t.sfclg_chromosome        is not distinct from d.sfclg_chromosome
                              and t.sfclg_start             is not distinct from d.sfclg_start
                              and t.sfclg_end               is not distinct from d.sfclg_end
                              and t.sfclg_location_source   is not distinct from d.sfclg_location_source
                              and t.sfclg_location_subsource is not distinct from d.sfclg_location_subsource
                              and t.sfclg_fdb_db_id         is not distinct from d.sfclg_fdb_db_id
                              and t.sfclg_pub_zdb_id        is not distinct from d.sfclg_pub_zdb_id
                              and t.sfclg_assembly          is not distinct from d.sfclg_assembly
                              and t.sfclg_gbrowse_track     is not distinct from d.sfclg_gbrowse_track
                              and t.sfclg_evidence_code     is not distinct from d.sfclg_evidence_code
                              and t.sfclg_strand            is not distinct from d.sfclg_strand) as would_collide
              from drifted d
             order by d.sfclg_data_zdb_id, d.sfclg_acc_num
            """;

    @Override
    public List<Tuple> getDriftedGenomeLocations(String source, String foreignDbContainerID) {
        return currentSession().createNativeQuery(DRIFTED_GENOME_LOCATIONS_SQL, Tuple.class)
            .setParameter("fdbcont", foreignDbContainerID)
            .setParameter("source", source)
            .list();
    }

    @Override
    public void reassignMarkerGenomeLocation(long locationID, String geneZdbID) {
        // entityID, not marker: the marker association maps the same column read-only.
        currentSession().createMutationQuery(
                "update MarkerGenomeLocation set entityID = :gene where ID = :pk")
            .setParameter("gene", geneZdbID)
            .setParameter("pk", locationID)
            .executeUpdate();
    }

    @Override
    public void deleteMarkerGenomeLocation(long locationID) {
        currentSession().createMutationQuery(
                "delete from MarkerGenomeLocation where ID = :pk")
            .setParameter("pk", locationID)
            .executeUpdate();
    }
}
