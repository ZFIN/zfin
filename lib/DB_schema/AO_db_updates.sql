begin work;
--------------------------------------------------------
-- Add anatomy_item(anatitem_obo_id), and populate
---------------------------------------------------------

--begin work;

alter table anatomy_item add (anatitem_obo_id varchar(20) before anatitem_name);
execute function populate_anatitem_obo_id();

create unique index anatitem_obo_id_index on anatomy_item (anatitem_obo_id) 
in idxdbs3;

alter table anatomy_item add constraint check (anatitem_obo_id is not null);

update statistics high for table anatomy_item;

--commit work;
--------------------------------------------------------
-- Add stage(stg_obo_id), and populate
---------------------------------------------------------

alter table stage add (stg_obo_id varchar(20) before stg_name);

execute function populate_stg_obo_id();

create unique index stage_obo_id_index on stage (stg_obo_id)
in idxdbs2;

alter table stage add constraint check (stg_obo_id is not null);

update statistics high for table stage;

--------------------------------------------------------
-- Move anatomy_item abbrevs to synonymns 
-- drop anatitem_abbrev and anatitem_abbrev_lower
---------------------------------------------------------

--begin work;

create temp table tmp_abbrev_to_syn (

  t_anatitem_zdb_id	varchar(50) primary key,
  t_anatitem_abbrev	varchar(20),
  t_dalias_zdb_id	varchar(50)
) with no log;

insert into tmp_abbrev_to_syn (t_anatitem_zdb_id, t_anatitem_abbrev)
	select anatitem_zdb_id, anatitem_abbrev
	  from anatomy_item
	 where anatitem_abbrev is not null
           and not exists 
		(select 't'
                   from data_alias
	          where dalias_data_zdb_id = anatitem_zdb_id
		    and dalias_alias = anatitem_abbrev) ;


update tmp_abbrev_to_syn set t_dalias_zdb_id = get_id ("DALIAS");


insert into zdb_active_data (zactvd_zdb_id)
	select t_dalias_zdb_id from tmp_abbrev_to_syn;


insert into data_alias(dalias_zdb_id,dalias_data_zdb_id,
			dalias_alias, dalias_group) 
	select t_dalias_zdb_id, t_anatitem_zdb_id,
	       t_anatitem_abbrev, "alias"
	  from tmp_abbrev_to_syn;

insert into updates (submitter_id, submitter_name, 
		     rec_id, field_name, new_value,
		     comments, when)
	select "Script", "Script", t_anatitem_zdb_id,
		"Synonymn", t_anatitem_abbrev, 
		"Move abbrevation to synonym.", CURRENT
          from tmp_abbrev_to_syn;

alter table anatomy_item drop anatitem_abbrev_lower;
alter table anatomy_item drop anatitem_abbrev;

update statistics high for table anatomy_item;

--rollback work ;
commit work;