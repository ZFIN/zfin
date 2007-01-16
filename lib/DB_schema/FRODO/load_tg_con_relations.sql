begin work ;

create temp table tmp_tgconloaders (fish_id varchar(50),
					construct varchar(60),
					tgcon_gene_abbrev varchar(20),
					tgcon_cds varchar(20))
with no log ;


load from tg_parsed
  insert into tmp_tgconloaders ;

create index fish_id_index
  on tmp_tgconloaders(fish_id)
  using btree in idxdbs4 ;

update statistics high for table tmp_tgconloaders ;

update tmp_tgconloaders
  set fish_id = (select zrepld_new_zdb_id
			from zdb_replaced_data
			where zrepld_old_zdb_id = fish_id)
  where exists (select 'x'
		  from zdb_replaced_data
			where zrepld_old_zdb_id = fish_id);


create temp table tmp_nodups (construct varchar(60),
					tgcon_gene_abbrev varchar(20),
					tgcon_cds varchar(20),
					gene_zdb_id varchar(50),
					cds_zdb_id varchar(50),
					construct_zdb_id varchar(50),
					mrel_id varchar(50),
					mrel2_id varchar(50))
with no log ;

insert into tmp_nodups (construct, tgcon_gene_abbrev, tgcon_cds)
  select distinct construct, tgcon_gene_abbrev, tgcon_cds
   from tmp_tgconloaders ;


--unload to tmp_tgcons
--  select * from tmp_tgconloaders ;

--select *
--  from tmp_tgconloaders
--  where not exists (select 'x' from marker where mrkr_abbrev=tgcon_gene_abbrev)
--  and not exists (select 'x' from data_alias where dalias_alias=tgcon_gene_abbrev);


--get the promoters

create temp table tmp_new_marker (mzdb_id varchar(50),mname varchar(255),mabbrev varchar(20),mtype varchar(30),mowner varchar(50)) 
with no log;

insert into tmp_new_marker 
  values (get_id('EFG'), 'EGFP', 
			'egfp','EFG',
		'ZDB-PERS-980622-10');

insert into tmp_new_marker 
  values (get_id('EFG'), 'GFP', 
			'gfp','EFG',
		'ZDB-PERS-980622-10');

insert into tmp_new_marker
  values (get_id('EFG'), 'RFP', 
			'rfp','EFG',
		'ZDB-PERS-980622-10');

insert into tmp_new_marker
  values (get_id('EFG'), 'shGFP', 
			'shgfp','EFG',
		'ZDB-PERS-980622-10');

insert into zdb_active_data
  select mzdb_id
   from tmp_new_marker	
	where not exists (select 'x'
			    from zdb_active_data
				where zactvd_zdb_id = mzdb_id);

insert into marker (mrkr_zdb_id, mrkr_name, mrkr_abbrev, mrkr_type, mrkr_owner)
  select mzdb_id, mname, mabbrev, mtype, mowner
    from tmp_new_marker ;


--select distinct a.mrkr_zdb_id
--		   from marker a, tmp_nodups;
--		   where a.mrkr_abbrev = tgcon_cds ;


update tmp_nodups
  set construct_zdb_id = (select mrkr_zdb_id
				from marker
				where mrkr_name = construct);
select * from marker
 where mrkr_name like 'Tg(dld:GFP)1.3%' ;


update tmp_nodups
  set cds_zdb_id = (select mrkr_zdb_id
			from marker
			where mrkr_name = tgcon_cds);

update tmp_nodups
  set gene_zdb_id = (select mrkr_zdb_id
			from marker
			where mrkr_abbrev = tgcon_gene_abbrev);

update tmp_nodups
  set cds_zdb_id = (select mrkr_zdb_id
			from marker, data_alias
			where mrkr_abbrev = tgcon_cds
			and mrkr_zdb_id = dalias_data_zdb_id
			and tgcon_cds = dalias_alias)
  where cds_zdb_id is null;


update tmp_nodups
  set gene_zdb_id = (select mrkr_zdb_id
			from marker, data_alias
			where mrkr_abbrev = tgcon_gene_abbrev
			and mrkr_zdb_id = dalias_data_zdb_id
			and tgcon_cds = dalias_alias)
  where gene_zdb_id is null;

unload to erik_dump.txt
  select construct,construct_zdb_id,
	tgcon_gene_abbrev,gene_zdb_id,
	tgcon_cds,cds_zdb_id
	 from tmp_nodups ;

delete from tmp_nodups
 where gene_zdb_id is null
 and cds_zdb_id is null
  ;

delete from tmp_nodups
 where gene_zdb_id = ''
  ;

update tmp_nodups
  set mrel_id = get_id('MREL')
  where gene_zdb_id is not null;

update tmp_nodups
  set mrel2_id = get_id('MREL')
  where cds_zdb_id is not null;


insert into zdb_active_data
  select mrel_id from tmp_nodups
   where mrel_id is not null;

insert into zdb_active_data
  select mrel2_id from tmp_nodups
   where mrel2_id is not null ;

--delete from tmp_nodups 
-- where construct_zdb_id in ('ZDB-TGCONSTRCT-060728-96','ZDB-TGCONSTRCT-060728---62','ZDB-TGCONSTRCT-060728-17'); 

delete from tmp_nodups
  where gene_zdb_id is null
   or gene_zdb_id = '' ;

unload to tmp_nodups
select construct_zdb_id,gene_zdb_id from tmp_nodups;


select distinct substr(construct_zdb_id, 4,10) as mrkr1
  from tmp_nodups ;

select * from tmp_nodups
where construct_zdb_id like 'ZDB-GENE-%';

select distinct get_obj_type(gene_zdb_id) as mrkr2
  from tmp_nodups ;

select * from marker_Relationship_type
where mreltype_name = 'promoter of';


insert into marker_relationship (mrel_zdb_id,
				  mrel_mrkr_1_zdb_id,
				  mrel_mrkr_2_zdb_id,
				  mrel_type,
				  mrel_comments)
  select mrel_id,
	construct_zdb_id,
	gene_zdb_id,
	'promoter of',
	'manual parsing of TG Loci revealed this promoter' 
    from tmp_nodups 
    where gene_zdb_id is not null
    and mrel_id is not null
    and gene_zdb_id != '';

insert into marker_relationship (mrel_zdb_id,
				  mrel_mrkr_1_zdb_id,
				  mrel_mrkr_2_zdb_id,
				  mrel_type,
				  mrel_comments)
  select mrel2_id,
	construct_Zdb_id,
	cds_zdb_id,
	'coding sequence of',
	'manual parsing of TG Loci revealed this coding sequence'
    from tmp_nodups 
    where cds_zdb_id is not null
    and mrel2_id is not null
    and cds_zdb_id != '';


update statistics high for table marker_relationship;

insert into zdb_active_data
  select mrel_zdb_id
   from marker_relationship
	where not exists (select 'x'
			    from zdb_active_data
				where zactvd_zdb_id = mrel_zdb_id);


update statistics high for table zdb_active_data ;
update statistics high for table marker_relationship ;


--set constraints all immediate ;

commit work ;
--rollback work ;