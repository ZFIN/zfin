
  SELECT distinct or_lg as chromosome, marker_id as zdb_id
    FROM mapped_marker
      union
  SELECT distinct or_lg as chromosome, mrel_mrkr_1_zdb_id as zdb_id
    FROM mapped_marker, marker_relationship
    WHERE marker_id = mrel_mrkr_2_zdb_id
    and mrel_type in ('clone contains gene',
                      'clone contains small segment',
		      'gene encodes small segment')
      union
  SELECT distinct or_lg as chromosome, mrel_mrkr_2_zdb_id as zdb_id
    FROM mapped_marker, marker_relationship
    WHERE marker_id = mrel_mrkr_1_zdb_id
     and mrel_type in ('clone contains gene', 'clone contains small segment')
     union
  SELECT distinct lnkg_or_lg as chromosome, mrel_mrkr_1_zdb_id as zdb_id
    FROM linkage, linkage_member, marker_relationship
    WHERE lnkgmem_member_zdb_id = mrel_mrkr_2_zdb_id
    and mrel_type in ('gene encodes small segment', 'clone contains gene','clone contains small segment')
    and lnkgmem_linkage_zdb_id = lnkg_zdb_id
      union
  SELECT distinct lnkg_or_lg as chromosome, mrel_mrkr_2_zdb_id as zdb_id
    FROM linkage, linkage_member,marker_relationship
    WHERE lnkgmem_member_zdb_id = mrel_mrkr_1_zdb_id
    and mrel_type in ('clone contains gene', 'clone contains small segment')
    and lnkgmem_linkage_zdb_id = lnkg_zdb_id
      union
  select distinct lnkg_or_lg as chromosome, mrel_mrkr_1_zdb_id as zdb_id
    FROM linkage, linkage_member,marker_relationship
    WHERE lnkgmem_member_zdb_id = mrel_mrkr_2_zdb_id
    and mrel_type in ('gene encodes small segment', 'clone contains gene','clone contains small segment')
    and lnkgmem_linkage_zdb_id = lnkg_zdb_id
      union
  SELECT distinct lnkg_or_lg as chromosome, mrel_mrkr_2_zdb_id as zdb_id
    FROM linkage, linkage_member, marker_relationship
    WHERE lnkgmem_member_zdb_id = mrel_mrkr_1_zdb_id
    and mrel_type in ('clone contains gene','clone contains small segment')
    and lnkgmem_linkage_zdb_id = lnkg_zdb_id
       union
  SELECT distinct lnkg_or_lg as chromosome, gt.mrel_mrkr_1_zdb_id as zdb_id
    from marker_relationship gt
   join marker_relationship ct on ct.mrel_mrkr_2_zdb_id == gt.mrel_mrkr_2_zdb_id
    join linkage_member on ct.mrel_mrkr_1_zdb_id == lnkgmem_member_zdb_id
    join linkage on lnkgmem_linkage_zdb_id == lnkg_zdb_id
    where gt.mrel_type == 'gene produces transcript'
    and ct.mrel_type == 'clone contains transcript'
        union
  SELECT distinct lnkg_or_lg as chromosome, lnkgmem_member_zdb_id as zdb_id
    FROM linkage, linkage_member
    WHERE lnkg_zdb_id = lnkgmem_linkage_zdb_id
into temp tmp_union;


select chromosome, zdb_id from tmp_union
  union
 select chromosome, fmrel_mrkr_zdb_id
  from tmp_union, feature_marker_relationship
  where fmrel_type = "is allele of"
 and fmrel_ftr_zdb_id = zdb_id
into temp tmp_full;


insert into chromosome_search_temp (chmst_chromosome, chmst_mrkr_zdb_id)
select * from tmp_full;