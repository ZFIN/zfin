package org.zfin.mapping.repository;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.mapping.MappedMarker;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;

import java.util.List;
import java.util.TreeSet;

import static org.zfin.framework.HibernateUtil.currentSession;

public class HibernateLinkageRepository implements LinkageRepository {

    private Logger logger = Logger.getLogger(HibernateLinkageRepository.class);


    public List<String> getDirectMappedMarkers(Marker marker) {
        Session session = currentSession();

        String hql = " select distinct  mm.lg " +
                " from MappedMarker  mm where " +
                " mm.marker.zdbID = :markerZdbID order by mm.lg  ";
        Query query = session.createQuery(hql);
        query.setString("markerZdbID", marker.getZdbID());


        List<String> lgs = query.list();

        return lgs;
    }


    public TreeSet<String> getLG(Marker marker) {
        Session session = currentSession();
        TreeSet<String> lgList = new TreeSet<String>();

        // a) add self panel mapping
        if (marker.getDirectPanelMappings() != null) {
            for (MappedMarker mm : marker.getDirectPanelMappings()) {
                if (mm != null) {
                    lgList.add(mm.getLg());
                }
            }
        }

        // b) add related(second) marker panel mapping
        Query query = session.createQuery(
                "select mm.lg " +
                        "from MappedMarker mm, MarkerRelationship mr join mr.firstMarker as fm" +
                        "     join mr.secondMarker as sm        " +
                        " where fm.zdbID = :zdbID " +
                        "   and sm.zdbID = mm.marker.zdbID " +
                        " and  mr.type in (:firstRelationship, :secondRelationship, :thirdRelationship)");

        query.setParameter("zdbID", marker.getZdbID());
        query.setParameter("firstRelationship", MarkerRelationship.Type.CLONE_CONTAINS_GENE);
        query.setParameter("secondRelationship", MarkerRelationship.Type.CLONE_CONTAINS_SMALL_SEGMENT);
        query.setParameter("thirdRelationship", MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);

        lgList.addAll(query.list());

        // c) add related(first) marker panel mapping
        query = session.createQuery(
                "select mm.lg " +
                        "from MappedMarker mm, MarkerRelationship mr join mr.firstMarker as fm" +
                        "     join mr.secondMarker as sm        " +
                        " where sm.zdbID = :zdbId " +
                        "   and fm.zdbID = mm.marker.zdbID " +
                        "   and mr.type in (:firstRelationship, :secondRelationship) ");

        query.setParameter("zdbId", marker.getZdbID());
        query.setParameter("firstRelationship", MarkerRelationship.Type.CLONE_CONTAINS_GENE);
        query.setParameter("secondRelationship", MarkerRelationship.Type.CLONE_CONTAINS_SMALL_SEGMENT);

        lgList.addAll(query.list());

        // d) add allele panel mapping
        /*query = session.createQuery(
                "select mm.lg" +
                        "  from MappedMarker mm, FeatureMarkerRelationship fmr " +
                        " where fmr.marker.zdbID = :zdbId " +
                        "   and fmr.featureZdbId = mm.marker.zdbID " +
                        "   and fmr.type = :relationship ");

        query.setParameter("zdbId", marker.getZdbID());
        query.setParameter("relationship", FeatureMarkerRelationship.IS_ALLELE_OF);
        lgList.addAll(query.list());*/

        // e) add self linkage mapping
        query = session.createQuery(
                "select l.lg " +
                        "from Linkage l join l.linkageMemberMarkers as m " +
                        " where m.zdbID = :zdbId ");
        query.setParameter("zdbId", marker.getZdbID());
        lgList.addAll(query.list());

        // f) add related(second) marker linkage mapping
        query = session.createQuery(
                "select l.lg " +
                        "from Linkage l join l.linkageMemberMarkers as lm, MarkerRelationship mr join mr.firstMarker as fm" +
                        "     join mr.secondMarker as sm        " +
                        " where fm.zdbID = :zdbId " +
                        "   and sm.zdbID = lm.zdbID " +
                        "   and mr.type in (:firstRelationship, :secondRelationship, :thirdRelationship)");

        query.setParameter("zdbId", marker.getZdbID());
        query.setParameter("firstRelationship", MarkerRelationship.Type.CLONE_CONTAINS_GENE);
        query.setParameter("secondRelationship", MarkerRelationship.Type.CLONE_CONTAINS_SMALL_SEGMENT);
        query.setParameter("thirdRelationship", MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);

        lgList.addAll(query.list());

        // g) add related(first) marker linkage mapping
        query = session.createQuery(
                "select l.lg " +
                        "from Linkage l join l.linkageMemberMarkers as lm, MarkerRelationship mr join mr.firstMarker as fm" +
                        "     join mr.secondMarker as sm        " +
                        " where sm.zdbID = :zdbId " +
                        "   and fm.zdbID = lm.zdbID " +
                        "   and mr.type in (:firstRelationship, :secondRelationship) ");

        query.setParameter("zdbId", marker.getZdbID());
        query.setParameter("firstRelationship", MarkerRelationship.Type.CLONE_CONTAINS_GENE);
        query.setParameter("secondRelationship", MarkerRelationship.Type.CLONE_CONTAINS_SMALL_SEGMENT);

        lgList.addAll(query.list());

        // h) add allele linkage mapping
        query = session.createQuery(
                "select l.lg" +
                        "  from Linkage l join l.linkageMemberFeatures as lf, FeatureMarkerRelationship fmr " +
                        " where fmr.marker.zdbID = :zdbId " +
                        "   and fmr.feature.zdbID = lf.zdbID " +
                        "   and fmr.type = :relationship ");

        query.setParameter("zdbId", marker.getZdbID());
        query.setParameter("relationship", FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF);
        lgList.addAll(query.list());


        // i) case 7518 Add to clones
        /**
         * select lnkg_or_lg
         from marker_relationship gt
         join marker_relationship ct on gt.mrel_mrkr_2_zdb_id = ct.mrel_mrkr_2_zdb_id
         join linkage_member lm on lm.lnkgmem_member_zdb_id = ct.mrel_mrkr_1_zdb_id
         join linkage l on lm.lnkgmem_linkage_zdb_id = l.lnkg_zdb_id
         where gt.mrel_type = 'gene produces transcript'
         and ct.mrel_type = 'clone contains transcript'
         and gt.mrel_mrkr_1_zdb_id='ZDB-GENE-030131-5474'
         ;

         select lnkg_or_lg
         from marker_relationship gt, marker_relationship ct,
         linkage_member lm, linkage l
         where gt.mrel_type = 'gene produces transcript'
         and ct.mrel_type = 'clone contains transcript'
         and gt.mrel_mrkr_2_zdb_id = ct.mrel_mrkr_2_zdb_id
         and ct.mrel_mrkr_1_zdb_id = lm.lnkgmem_member_zdb_id
         and lm.lnkgmem_linkage_zdb_id = l.lnkg_zdb_id
         and gt.mrel_mrkr_1_zdb_id='ZDB-GENE-030131-5474'

         ;

         */
        if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
            query = session.createQuery(
                    "select l.lg " +
                            "from MarkerRelationship gt , MarkerRelationship ct, Linkage l join l.linkageMemberMarkers lm " +
                            " where gt.type = :firstRelationship " +
                            " and ct.type = :secondRelationship" +
                            " and gt.secondMarker.zdbID = ct.secondMarker.zdbID  " +
                            " and ct.firstMarker.zdbID = lm.zdbID  " +
                            " and gt.firstMarker.zdbID = :zdbID  " +
                            " ");

            query.setParameter("zdbID", marker.getZdbID());
            query.setParameter("firstRelationship", MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT);
            query.setParameter("secondRelationship", MarkerRelationship.Type.CLONE_CONTAINS_TRANSCRIPT);

            lgList.addAll(query.list());
        }

        /**
         * From mapping details
         07/28/03 we decided to show LG Unknown when LG is 0. If we have
         other evidence of LG not 0, only the non 0 will be shown in mini mode,
         but more in mapping detail page.
         */

        return lgList;
    }

}
