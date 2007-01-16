begin work ;

drop table fish_image_anatomy ;

update statistics for procedure ;

drop table fish_image_stage ;

update statistics for procedure ;

drop table fish_image ;

update statistics for procedure ;

drop table fish_image_form ;

update statistics for procedure ;

drop table fish_image_direction ;

update statistics for procedure ;

drop table fish_image_view ;

update statistics for procedure ;

drop table fish_image_preparation ;

set constraints all deferred ;

drop table locus;

update statistics for procedure ;

drop table fish ;

update statistics  for procedure ;

drop table alteration ;

update statistics for procedure ;

drop table chromosome ;

update statistics for procedure ;

drop table int_fish_chromo ;

update statistics for procedure ;

drop table alteration_type ;

update statistics for procedure ;

drop table zirc_fish_line_alteration ;

update statistics for procedure ;

drop table zirc_fish_line_status ;

update statistics for procedure ;

drop table zirc_fish_line ;

update statistics for procedure ;

drop table zirc_fish_line_background ;

update statistics for procedure ;

drop table fish_status ;

update statistics for procedure ;

drop table fish_supplier_Status ;

update statistics for procedure ;

drop table locus_registration ;

update statistics for procedure ;

set constraints all immediate ;

update statistics for procedure ;

create unique index apatoinf_alternate_key_index
  on apato_infrastructure (api_entity_a_zdb_id,
			api_entity_b_zdb_id,
			api_quality_zdb_id,
			api_tag,
			api_pub_zdb_id)
  using btree in idxdbs3 ;

alter table apato_infrastructure 
  add constraint unique (api_entity_a_zdb_id,
			api_entity_b_zdb_id,
			api_quality_zdb_id,
			api_tag,
			api_pub_zdb_id)
  constraint apato_infrastructure_alternate_key ;

delete from marker_type_group_member
  where mtgrpmem_mrkr_type = 'GENE'
  and mtgrpmem_mrkr_type_group = 'CONSTRUCT' ;

delete from marker_type_group_member
  where mtgrpmem_mrkr_type = 'GENEP'
  and mtgrpmem_mrkr_type_group = 'CONSTRUCT' ;


set constraints all deferred ;

delete from data_alias
  where exists (select 'x'
		  from marker
		  where mrkr_zdb_id = dalias_data_zdb_id
		  and dalias_alias = mrkr_name
		  and mrkr_type in ('TGCONSTRCT','GENE')
		)
  and dalias_zdb_id != 'ZDB-DALIAS-040826-39'
  and dalias_zdb_id != 'ZDB-DALIAS-060505-14'
  and dalias_zdb_id != 'ZDB-DALIAS-060831-274';

select * from marker_history
  where mhist_dalias_zdb_id not in (select dalias_zdb_id from data_alias);

update statistics high for table data_alias ;
update statistics high for table marker_history ;
update statistics high for table zdb_active_data ;

set constraints all immediate ;

--update statistics high ;

--rollback work ;
commit work ;