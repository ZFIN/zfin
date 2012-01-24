begin work ;

delete from gene_feature_result_view;

delete from functional_annotation
 where fa_geno_name like 'ZDB-GENO%';

delete from fish_Annotation_search
 where fas_geno_name like 'ZDB-GENO%';

delete from fish_annotation_search
 where fas_gene_group is null
 and fas_morpholino_group is null
 and fas_feature_group is null
 and fas_construct_group is null;

delete from functional_annotation
 where fa_gene_group is null
 and fa_morpholino_group is null
 and fa_feature_group is null
 and fa_construct_group is null;

set pdqpriority 30;

update statistics high for table morpholino_group_member;
update statistics high for table affected_gene_group_member;
update statistics high for table feature_group_member;
update statistics high for table construct_group_member;

insert into gene_feature_result_view (gfrv_fa_id,
       gfrv_fas_id,
       gfrv_geno_handle,
       gfrv_affector_id,
       gfrv_affector_abbrev,
       gfrv_affector_abbrev_order,
       gfrv_affector_type_display,
       gfrv_gene_zdb_id,
       gfrv_gene_abbrev,
       gfrv_gene_abbrev_order)
select distinct fa_pk_id,
       fas_pk_id,
       fa_geno_handle,
       fgm_member_id,
       feature_abbrev,
       feature_abbrev_order,
       ftrtype_type_display,
       a.mrkr_zdb_id,
       a.mrkr_abbrev,
       a.mrkr_abbrev_order
 from functional_annotation,
       fish_annotation_search,
      feature_group,
      feature_group_member,
      feature,
      feature_type,
      outer (feature_marker_relationship c, outer marker a, outer feature_marker_relationship_type)
 where fa_geno_zdb_id = fg_geno_zdb_id
 and fa_genox_zdb_id is null
  and fgm_group_id = fg_group_pk_id
   and fgm_member_id = feature_zdb_id
 and feature_type = ftrtype_name
 and c.fmrel_ftr_zdb_id = feature_zdb_id
 and c.fmrel_mrkr_zdb_id = a.mrkr_zdb_id
 and c.fmrel_type = fmreltype_name
 and fmreltype_produces_affected_marker = 't'
 and c.fmrel_type in ('markers missing','markers absent','is allele of','markers moved')
 and feature_Type not in ('TRANSGENIC_INSERTION','TRANSGENIC_UNSPECIFIED')
 and fas_geno_handle = fa_geno_handle;


select * from gene_feature_result_view
  where gfrv_affector_abbrev = 'b250';

insert into gene_feature_result_view (gfrv_fa_id,
       gfrv_fas_id,
       gfrv_geno_handle,
       gfrv_affector_id,
       gfrv_affector_abbrev,
       gfrv_affector_abbrev_order,
       gfrv_affector_type_display,
       gfrv_gene_zdb_id,
       gfrv_gene_abbrev,
       gfrv_gene_abbrev_order)
select distinct fa_pk_id,
       fas_pk_id,
       fa_geno_handle,
       fgm_member_id,
       feature_abbrev,
       feature_abbrev_order,
       ftrtype_type_display,
       a.mrkr_zdb_id,
       a.mrkr_abbrev,
       a.mrkr_abbrev_order
 from functional_annotation,
       fish_annotation_search,
      feature_group,
      feature_group_member,
      feature,
      feature_type,
      outer (feature_marker_relationship c, outer marker a, outer feature_marker_relationship_type)
 where fa_genox_zdb_id = fg_genox_zdb_id
  and fgm_group_id = fg_group_pk_id
   and fgm_member_id = feature_zdb_id
 and feature_type = ftrtype_name
 and c.fmrel_ftr_zdb_id = feature_zdb_id
 and c.fmrel_mrkr_zdb_id = a.mrkr_zdb_id
 and c.fmrel_type = fmreltype_name
 and fmreltype_produces_affected_marker = 't'
 and c.fmrel_type in ('markers missing','markers absent','is allele of','markers moved')
 and feature_Type not in ('TRANSGENIC_INSERTION','TRANSGENIC_UNSPECIFIED') 
 and fas_geno_handle = fa_geno_handle;



insert into gene_feature_result_view (gfrv_fa_id,
       gfrv_fas_id,
       gfrv_geno_handle,
       gfrv_affector_id,
       gfrv_affector_abbrev,
       gfrv_affector_abbrev_order,
       gfrv_affector_type_display,
       gfrv_gene_zdb_id,
       gfrv_gene_abbrev,
       gfrv_gene_abbrev_order,
       gfrv_construct_zdb_id,
       gfrv_construct_name,
       gfrv_construct_abbrev_order)
select distinct fa_pk_id,
       fas_pk_id,
       fa_geno_handle,
       fgm_member_id,
       feature_abbrev,
       feature_abbrev_order,
       ftrtype_type_display,
       a.mrkr_zdb_id,
       a.mrkr_abbrev,
       a.mrkr_abbrev_order,
       b.mrkr_zdb_id,
       b.mrkr_name,
       b.mrkr_abbrev_order
 from functional_annotation,
       fish_annotation_search,
      feature_group,
      feature_group_member,
      feature,
      feature_type,
      outer (feature_marker_relationship c, outer marker a, outer feature_marker_relationship_type),
      outer (feature_marker_relationship d, outer marker b)     
 where fa_genox_zdb_id = fg_genox_zdb_id
  and fgm_group_id = fg_group_pk_id
   and fgm_member_id = feature_zdb_id
 and feature_type = ftrtype_name
 and c.fmrel_ftr_zdb_id = feature_zdb_id
 and c.fmrel_mrkr_zdb_id = a.mrkr_zdb_id
 and c.fmrel_type = fmreltype_name
 and fmreltype_produces_affected_marker = 't'
 and c.fmrel_type in ('markers missing','markers absent','is allele of','markers moved')
 and d.fmrel_ftr_zdb_id = feature_zdb_id
 and d.fmrel_mrkr_zdb_id = b.mrkr_zdb_id
 and d.fmrel_type like 'contains%'
 and fas_geno_handle = fa_geno_handle;

insert into gene_feature_result_view (gfrv_fa_id,
       gfrv_fas_id,
       gfrv_geno_handle,
       gfrv_affector_id,
       gfrv_affector_abbrev,
       gfrv_affector_abbrev_order,
       gfrv_affector_type_display,
       gfrv_gene_zdb_id,
       gfrv_gene_abbrev,
       gfrv_gene_abbrev_order,
       gfrv_construct_zdb_id,
       gfrv_construct_name,
       gfrv_construct_abbrev_order)
select distinct fa_pk_id,
       fas_pk_id,
       fa_geno_handle,
       fgm_member_id,
       feature_abbrev,
       feature_abbrev_order,
       ftrtype_type_display,
       a.mrkr_zdb_id,
       a.mrkr_abbrev,
       a.mrkr_abbrev_order,
       b.mrkr_zdb_id,
       b.mrkr_name,
       b.mrkr_abbrev_order
 from functional_annotation,
       fish_annotation_search,
      feature_group,
      feature_group_member,
      feature,
      feature_type,
      outer (feature_marker_relationship c, outer marker a, outer feature_marker_relationship_type),
      outer (feature_marker_relationship d, outer marker b)     
 where fa_geno_zdb_id = fg_geno_zdb_id
 and fa_genox_zdb_id is null
  and fgm_group_id = fg_group_pk_id
   and fgm_member_id = feature_zdb_id
 and feature_type = ftrtype_name
 and c.fmrel_ftr_zdb_id = feature_zdb_id
 and c.fmrel_mrkr_zdb_id = a.mrkr_zdb_id
 and c.fmrel_type = fmreltype_name
 and fmreltype_produces_affected_marker = 't'
 and c.fmrel_type in ('markers missing','markers absent','is allele of','markers moved')
 and d.fmrel_ftr_zdb_id = feature_zdb_id
 and d.fmrel_mrkr_zdb_id = b.mrkr_zdb_id
 and d.fmrel_type like 'contains%'
 and fas_geno_handle = fa_geno_handle;


----MORPHS------
insert into gene_feature_result_view (gfrv_fa_id,
gfrv_fas_id,
       gfrv_geno_handle,
       gfrv_affector_id,
       gfrv_affector_abbrev,
       gfrv_affector_abbrev_order,
       gfrv_affector_type_display,
       gfrv_gene_zdb_id,
       gfrv_gene_abbrev,
       gfrv_gene_abbrev_order)
select distinct fa_pk_id,
       fas_pk_id,
       fa_geno_handle,
       morphgm_member_id,
       c.mrkr_abbrev,
       c.mrkr_abbrev_order,
       'Morpholino',
       a.mrkr_zdb_id,
       a.mrkr_abbrev,
       a.mrkr_abbrev_order    
  from functional_annotation,
       fish_annotation_search,
      morpholino_Group_member,
      morpholino_group,
      marker c,
      marker_relationship, marker a
  where fa_genox_zdb_id = morphg_genox_zdb_id
  and morphgm_group_id = morphg_group_pk_id
 and fa_morpholino_group is not null
 and morphgm_member_id = c.mrkr_zdb_id
 and c.mrkr_zdb_id = mrel_mrkr_1_zdb_id
 and a.mrkr_Zdb_id = mrel_mrkr_2_zdb_id
and fas_geno_handle = fa_geno_handle;


insert into gene_feature_result_view (gfrv_fa_id,
gfrv_fas_id,
       gfrv_geno_handle,
       gfrv_affector_id,
       gfrv_affector_abbrev,
       gfrv_affector_abbrev_order,
       gfrv_affector_type_display)
select distinct fa_pk_id,
       fas_pk_id,
       fa_geno_handle,
       fgm_member_id,
       feature_abbrev,
       feature_abbrev_order,
       ftrtype_type_display
  from functional_annotation,
       fish_annotation_search,
      feature_group,
      feature_group_member,
      feature,
      feature_type
  where fa_geno_zdb_id = fg_geno_zdb_id
  and fgm_group_id = fg_group_pk_id
 and fa_feature_group is not null
 and fgm_member_id = feature_zdb_id
 and feature_type = ftrtype_name
 and exists (Select 'x' from feature_marker_Relationship where fmrel_ftr_zdb_id = fgm_member_id and get_obj_type(fmrel_mrkr_zdb_id)='SSLP')
 and not exists (Select 'x' from feature_marker_Relationship where fmrel_ftr_zdb_id = fgm_member_id and get_obj_type(fmrel_mrkr_zdb_id)='GENE')
and fas_geno_handle = fa_geno_handle
;


update gene_feature_Result_view
set gfrv_gene_abbrev = null
 where gfrv_gene_zdb_id not like 'ZDB-GENE%';

update gene_feature_Result_view
set gfrv_gene_abbrev_order = null
 where gfrv_gene_zdb_id not like 'ZDB-GENE%';

update gene_feature_Result_view
set gfrv_gene_zdb_id = null
 where gfrv_gene_zdb_id not like 'ZDB-GENE%';

select a.gfrv_pk_id as id 
       from gene_feature_result_View b, gene_feature_result_view a
  	       	       where b.gfrv_geno_handle = a.gfrv_geno_handle
		       and b.gfrv_gene_zdb_id is not null
		       and a.gfrv_gene_zdb_id is null
		       and b.gfrv_affector_id = a.gfrv_affector_id
		       and a.gfrv_pk_id != b.gfrv_pk_id
		       and a.gfrv_construct_name is null
		       and b.gfrv_construct_name is null	          
into temp tmp_deletes;

create index id_index on tmp_deletes (id)
 using btree in idxdbs3;

delete from gene_feature_result_view
 where exists (Select 'x' from tmp_deletes where id = gfrv_pk_id);

select distinct gfrv_fas_id, gfrv_geno_handle, gfrv_affector_id,
       gfrv_affector_abbrev,
       gfrv_affector_abbrev_order,
       gfrv_affector_type_display,
gfrv_gene_zdb_id,
       gfrv_gene_abbrev,
       gfrv_gene_abbrev_order,
       gfrv_construct_zdb_id,
       gfrv_construct_name,
       gfrv_construct_abbrev_order
 from gene_feature_result_view
into temp tmp_gfrv;



delete from gene_feature_result_view;

insert into gene_feature_result_view (gfrv_fas_id, gfrv_geno_handle, gfrv_affector_id,
       gfrv_affector_abbrev,
       gfrv_affector_abbrev_order,
       gfrv_affector_type_display,
gfrv_gene_zdb_id,
       gfrv_gene_abbrev,
       gfrv_gene_abbrev_order,
       gfrv_construct_zdb_id,
       gfrv_construct_name,
       gfrv_construct_abbrev_order)
  select gfrv_fas_id, gfrv_geno_handle, gfrv_affector_id,
       gfrv_affector_abbrev,
       gfrv_affector_abbrev_order,
       gfrv_affector_type_display,
gfrv_gene_zdb_id,
       gfrv_gene_abbrev,
       gfrv_gene_abbrev_order,
       gfrv_construct_zdb_id,
       gfrv_construct_name,
       gfrv_construct_abbrev_order
    from tmp_gfrv;


select * from gene_feature_result_view
where gfrv_geno_handle = 'hi459Tg[2,1,1]';

select * from gene_feature_result_view
where gfrv_geno_handle ='sb15[U,U,U] t24412[2,1,1]TU';

select * from gene_feature_result_view
where gfrv_geno_handle ='p0[U,U,U] tm110b[2,2,U]';

select * from gene_feature_result_view
where gfrv_affector_abbrev ='b250';


!echo "records in fish_annotation_search not in gene_feature_result_view";
select * from fish_annotation_search
 where fas_pk_id not in (Select gfrv_fas_id from gene_feature_Result_view);

select * from gene_feature_result_view
 where gfrv_geno_handle = 'Df(LG03)c1033/c1033 (AB)';

!echo 'leftovers missing 1';
select count(*) from fish_annotation_search, gene_feature_result_view
where
fas_pk_id = gfrv_fas_id
and fas_geno_handle like '%[%[%'
and (Select count(*) from gene_feature_result_view a
                where a.gfrv_fas_id = fas_pk_id) < 2;

!echo 'leftovers missing 2';
select count(*) from fish_annotation_search, gene_feature_result_view
where
fas_pk_id = gfrv_fas_id
and fas_geno_handle like '%[%[%[%'
and (Select count(*) from gene_feature_result_view a
                where a.gfrv_fas_id = fas_pk_id) < 3;

!echo 'leftovers missing 3';
select count(*) from fish_annotation_search, gene_feature_result_view
where
fas_pk_id = gfrv_fas_id
and fas_geno_handle like '%[%[%[%['
and (Select count(*) from gene_feature_result_view a
                where a.gfrv_fas_id = fas_pk_id) < 4;


--rollback work ;
commit work ;