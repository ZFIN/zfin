
  SELECT distinct mm_chromosome as chromosome, marker_id as zdb_id, 'directMappedMarker' as subsource
    FROM mapped_marker
      union
  SELECT distinct mm_chromosome as chromosome, mrel_mrkr_1_zdb_id as zdb_id, 'mappedMarkerGenePullThroughToClone' as subsource
    FROM mapped_marker, marker_relationship
    WHERE marker_id = mrel_mrkr_2_zdb_id
    and mrel_type in ('clone contains gene',
                      'clone contains small segment',
		      'gene encodes small segment')
      union
  SELECT distinct lnkg_chromosome as chromosome, mrel_mrkr_1_zdb_id as zdb_id, 'linkageGenePullThroughToClone' as subsource
    FROM linkage, linkage_membership_search, marker_relationship, linkage_membership
    WHERE lms_member_1_zdb_id = mrel_mrkr_2_zdb_id
    and lms_lnkgmem_id = lnkgm_pk_id
    and mrel_type in ('gene encodes small segment', 'clone contains gene','clone contains small segment')
    and lnkgm_linkage_zdb_id = lnkg_zdb_id
      union
  SELECT distinct lnkg_chromosome as chromosome, gt.mrel_mrkr_1_zdb_id as zdb_id, 'linkageTranscriptPullThroughtoGeneOrClone' as subsource
    from marker_relationship gt
   join marker_relationship ct on ct.mrel_mrkr_2_zdb_id == gt.mrel_mrkr_2_zdb_id
    join linkage_membership_search on ct.mrel_mrkr_1_zdb_id == lms_member_1_zdb_id
    join linkage_membership on lms_lnkgmem_id == lnkgm_pk_id
    join linkage on lnkgm_linkage_zdb_id == lnkg_zdb_id
    where gt.mrel_type == 'gene produces transcript'
    and ct.mrel_type == 'clone contains transcript'
        union
  SELECT distinct lnkg_chromosome as chromosome, lms_member_1_zdb_id as zdb_id, 'linkagesDirectMem1' as subsource
    FROM linkage, linkage_membership_search, linkage_membership
   WHERE lnkg_zdb_id = lnkgm_linkage_zdb_id
    and lms_lnkgmem_id = lnkgm_pk_id
        union
  SELECT distinct lnkg_chromosome as chromosome, lms_member_2_zdb_id as zdb_id, 'linkagesDirectMem2' as subsource
    FROM linkage, linkage_membership_search, linkage_membership
   WHERE lnkg_zdb_id = lnkgm_linkage_zdb_id
    and lms_lnkgmem_id = lnkgm_pk_id 
union
  select distinct or_lg as chromosome, zdb_id as zdb_id, 'paneledMarkersDirect' as subsource
    from paneled_markers
into temp tmp_union;

select chromosome as chromosome , zdb_id as zdb_id,'other map location' as source, subsource from tmp_union
  union
 select chromosome as chromosome , fmrel_mrkr_zdb_id,'other map location' as source, 'geneLocationPullThruFromAllele' as subsource
  from tmp_union, feature_marker_relationship
  where fmrel_type in ("is allele of","markers present","markers absent")
 and fmrel_ftr_zdb_id = zdb_id
union
select distinct or_lg as chromosome, fmrel_ftr_zdb_id as zdb_id, 'other map location' as source, 'featureLocationPullThruFromGenePaneledMarkers' as subsource
  from paneled_markers, feature_marker_relationship
 where zdb_id = fmrel_mrkr_zdb_id
union
select lnkg_chromosome as chromosome , fmrel_ftr_zdb_id as zdb_id, 'other map location' as source, 'featureLocationPullThruFromGeneLinkage' as subsource
     FROM linkage, linkage_membership_search, feature_marker_relationship, linkage_membership
    WHERE lms_member_1_zdb_id = fmrel_mrkr_zdb_id
    and lms_lnkgmem_id = lnkgm_pk_id
    and fmrel_type in ('is allele of', 'markers present','markers absent')
    and lnkgm_linkage_zdb_id = lnkg_zdb_id
union
SELECT  mm_chromosome                                 AS chromosome,
                fmrel_ftr_zdb_id                              AS zdb_id,
                'other map location'                          AS source,
                'Feature location obtained from related gene that has mapped_marker info' AS subsource
FROM   mapped_marker,
       feature_marker_relationship
WHERE  marker_id = fmrel_mrkr_zdb_id
union
SELECT lnkg_chromosome as chromosome ,
       lsingle_member_zdb_id as zdb_id,
       'General Load' as source,
       'load data [singleton]' as subsource
FROM   linkage_single,
       linkage
WHERE  lsingle_lnkg_zdb_id = lnkg_zdb_id
UNION
SELECT lnkg_chromosome as chromosome , 
       mrkr_zdb_id as zdb_id, 
       'General Load' as source,
       'Clone Location obtained from gene with clone-contains-gene relationship [singleton]' as subsource
FROM   linkage_single, 
       marker, 
       linkage, 
       marker_relationship
WHERE  lsingle_lnkg_zdb_id = lnkg_zdb_id 
       AND mrel_type = 'clone contains gene' 
       AND mrel_mrkr_2_zdb_id = lsingle_member_zdb_id 
       AND mrel_mrkr_1_zdb_id = mrkr_zdb_id
       AND linkage.lnkg_source_zdb_id in ('ZDB-PUB-030703-1','ZDB-PUB-020822-1')

UNION
SELECT lnkg_chromosome as chromosome ,
       mrkr_zdb_id as zdb_id,
       'other map location' as source,
       'Clone Location obtained from gene with clone-contains-gene relationship [singleton]' as subsource
FROM   linkage_single,
       marker,
       linkage,
       marker_relationship
WHERE  lsingle_lnkg_zdb_id = lnkg_zdb_id
       AND mrel_type = 'clone contains gene'
       AND mrel_mrkr_2_zdb_id = lsingle_member_zdb_id
       AND mrel_mrkr_1_zdb_id = mrkr_zdb_id
       AND linkage.lnkg_source_zdb_id not in ('ZDB-PUB-030703-1','ZDB-PUB-020822-1')

UNION
SELECT  lnkg_chromosome as chromosome ,
                marker.mrkr_zdb_id as zdb_id,
                'other map location' as source,
                'Gene location obtained from a clone-contains-gene relationship inference' as subsource
FROM   marker as marker, marker as clone, 
       linkage_single as ch,
       linkage,
       marker_relationship
WHERE  mrel_type = 'clone contains gene'
 AND mrel_mrkr_1_zdb_id = clone.mrkr_zdb_id
 and mrel_mrkr_2_zdb_Id = marker.mrkr_zdb_id
 and ch.lsingle_member_zdb_id = clone.mrkr_zdb_id
and lnkg_zdb_id = lsingle_lnkg_zdb_id
 and lnkg_chromosome != 0
union
SELECT lnkg_chromosome as chromosome ,
       mrkr_zdb_id as zdb_id,
       'other map location' as source,
       'Gene Location obtained from is-allele-of relationship with feature '||feature_abbrev||' ['||fmrel_ftr_zdb_id||']' as subsource
FROM   marker, linkage_single, linkage, feature_marker_relationship, feature
where mrkr_zdb_id = fmrel_mrkr_zdb_id 
 and lsingle_member_zdb_id = fmrel_ftr_zdb_id
  and  fmrel_type = 'is allele of' 
and lnkg_Zdb_id = lsingle_lnkg_zdb_id
and feature_zdb_id = fmrel_ftr_zdb_id
union
SELECT lnkg_chromosome AS chromosome,
       marker.mrkr_zdb_id AS zdb_id,
       'General Load'  AS source,
       'Clone location obtained from a clone-contains-clone relationship inference' AS subsource
FROM   marker AS marker,
       marker AS clone,
       linkage_single AS ch,
       linkage,
       marker_relationship
WHERE  mrel_type = 'clone contains small segment'
       AND mrel_mrkr_1_zdb_id = clone.mrkr_zdb_id
       AND mrel_mrkr_2_zdb_id = marker.mrkr_zdb_id
       AND ch.lsingle_member_zdb_id = clone.mrkr_zdb_id
       AND lnkg_zdb_id = lsingle_lnkg_zdb_id
       AND lnkg_chromosome != '0'
into temp tmp_full;


insert into sequence_feature_chromosome_location_generated_temp (sfclg_chromosome, sfclg_data_zdb_id, sfclg_location_source, sfclg_location_subsource)
select distinct * from tmp_full;

!echo "Remove AB, U and 0 from chrromosome mart";
 
delete from sequence_feature_chromosome_location_generated_temp
 where sfclg_chromosome in ('AB','U','0');


insert into linkage_membership_search_temp (lms_member_1_zdb_id, lms_member_2_zdb_id, lms_lnkgmem_id, lms_distance, lms_units, lms_lod, lms_lnkg_zdb_Id)
  select lnkgm_member_1_zdb_id, lnkgm_member_2_zdb_id, lnkgm_pk_id, lnkgm_distance, lnkgm_metric, lnkgm_lod, lnkgm_linkage_zdb_id
   from linkage_membership;

insert into linkage_membership_search_temp (lms_member_1_zdb_id, lms_member_2_zdb_id, lms_lnkgmem_id, lms_distance, lms_units, lms_lod, lms_lnkg_zdb_Id)
  select distinct lnkgm_member_2_zdb_id, lnkgm_member_1_zdb_id, lnkgm_pk_id, lnkgm_distance, lnkgm_metric, lnkgm_lod, lnkgm_linkage_zdb_id
   from linkage_membership;
