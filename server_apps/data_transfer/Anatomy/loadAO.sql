----------------------------------------------------------------
-- FILE: loadAO.sql
--
-- The script loads AO files produced by parser into ZFIN db.
--
-- INPUT:
--      anatitem_new.unl
--      anatitem_exist.unl
--      anatitem_merged.unl
--      anatitem_obsolete.unl
--      anatrel.unl
--      anatalias.unl
--      anatitem_ids.unl
--      stage_ids.unl
--
-- OUTPUT:
--     startStgInconsistent.err
--     endStgInconsistent.err
--     pub_incorrect.err
-- 		 for the above three, zero length means error free
--     annotationViolates.err
-- 		 Send content to curators 
--     
-- EFFECT:
--      anatomy_item: new/updated entries
--      anatomy_relationship: wipe off and reload
--      data_alias: new/updated entries
--      expression_result  : replace merged anatomy term 
--			     with the replacer
--      zdb_active_data: drop obsolete and merged anatomy term,
--                   old alias. Load in new terms, and data alias.
--      updates: record anatomy and anatomy relationship updates
--
----------------------------------------------------------------

!echo '====================================================='
!echo '===== Load data into temp table and Prepare       ==='
!echo '====================================================='

-----------------------------------------------------
-- Anatomy OBO id and ZDB id translation table
-----------------------------------------------------
!echo '===== anatomy_item id translation ====='
create temp table ao_id_translation (
	ait_obo_id	char(11),
	ait_zdb_id	varchar(50)
)with no log;
create unique index ao_id_translation_index 
	on ao_id_translation (ait_obo_id);
create unique index ao_id_translation_primary_key
	on ao_id_translation (ait_zdb_id);

load from "anatitem_ids.unl" insert into ao_id_translation;

-----------------------------------------------------
-- Stage OBO id and ZDB id translation table
-----------------------------------------------------
!echo '===== stage id translation ====='
create temp table stg_id_translation (
	sit_obo_id	char(11),
	sit_zdb_id	varchar(50)
)with no log;
create unique index stg_id_translation_index 
	on stg_id_translation (sit_obo_id);
create unique index stg_id_translation_primary_key
	on stg_id_translation (sit_zdb_id);

load from "stage_ids.unl" insert into stg_id_translation;


------------------------------------------------------
-- New anatomy items
--
-- create new_anatomy_item table, load in anatitem_new.unl.
-- Unload into anaitem_new.rev for curator review.
-- Prepare the zdb ids. 
-------------------------------------------------------
!echo '===== new_anatomy_item ====='
create temp table new_anatomy_item (
	n_anatitem_obo_id	varchar(50),
	n_anatitem_name		varchar(80),
	n_anatitem_start_stg_zdb_id	varchar(50), 
	n_anatitem_end_stg_zdb_id 	varchar(50), 
	n_anatitem_definition	lvarchar,
	n_anatitem_description	lvarchar,
	n_anatitem_zdb_id	varchar(50)
) with no log;

create unique index new_anatomy_item_name_index 
	on new_anatomy_item (n_anatitem_name);
create unique index new_anatomy_item_primary_key
	on new_anatomy_item (n_anatitem_obo_id);

!echo '== load anatitem_new.unl =='
load from "anatitem_new.unl" insert into new_anatomy_item;

!echo '== get anatomy_item zdb id ==' 
update new_anatomy_item
   set n_anatitem_zdb_id = get_id("ANAT");

!echo '== update stg obo id =='
update new_anatomy_item
   set n_anatitem_start_stg_zdb_id = 
		(select sit_zdb_id
		   from stg_id_translation
		  where n_anatitem_start_stg_zdb_id = sit_obo_id);
update new_anatomy_item
   set n_anatitem_end_stg_zdb_id = 
		(select sit_zdb_id
		   from stg_id_translation
		  where n_anatitem_end_stg_zdb_id = sit_obo_id);

--------------------------------------------------------
-- Existing/Updated anatomy items
-- 
-- create updated_anatomy_item table, load in anatitem_exist.unl.
-- Filter out the unchanged anatomy items, and unload the 
-- changed ones into anaitem_new.rev for curator review.
---------------------------------------------------------
!echo '===== updated_anatomy_item  ====='
create temp table updated_anatomy_item (
	u_anatitem_zdb_id	varchar(50),
	u_anatitem_name		varchar(80),
	u_anatitem_start_stg_zdb_id	varchar(50) not null,
	u_anatitem_end_stg_zdb_id 	varchar(50) not null,
	u_anatitem_definition	lvarchar,
	u_anatitem_description	lvarchar
) with no log;

create unique index u_anatomy_item_name_index 
	on updated_anatomy_item (u_anatitem_name);
create unique index u_anatomy_item_primary_key
	on updated_anatomy_item (u_anatitem_zdb_id);

!echo '=== load anatitem_exist.unl ==='
load from "anatitem_exist.unl" insert into updated_anatomy_item;

!echo '== update stg obo id =='
update updated_anatomy_item
   set u_anatitem_start_stg_zdb_id = 
		(select sit_zdb_id
		   from stg_id_translation
		  where u_anatitem_start_stg_zdb_id = sit_obo_id);
update updated_anatomy_item
   set u_anatitem_end_stg_zdb_id = 
		(select sit_zdb_id
		   from stg_id_translation
		  where u_anatitem_end_stg_zdb_id = sit_obo_id);

-------------------------------------------------------------------
-- Merged anatomy items
--
-- create merged_anatomy_item table and load in merged pairs from 
-- anatitem_merged.unl file. Update new terms' zdb id if needed.
-------------------------------------------------------------------
!echo '=====  merged anatomy items ====='
create temp table merged_anatomy_item(
	m_anatitem_new_zdb_id	varchar(50),
	m_anatitem_old_zdb_id 	varchar(50)
)with no log;

!echo '== load anatitem_merged.unl =='
load from "anatitem_merged.unl" insert into merged_anatomy_item;

!echo '== update obo id to zdb id on merged new term =='
update merged_anatomy_item
	set m_anatitem_new_zdb_id = (select ait_zdb_id 
			               from ao_id_translation
				      where m_anatitem_new_zdb_id = ait_obo_id)
      where m_anatitem_new_zdb_id in (select ait_obo_id
				        from ao_id_translation);

!echo '== update merged new term zdb id if it is a new term =='
update merged_anatomy_item
	set m_anatitem_new_zdb_id = (select n_anatitem_zdb_id
				       from new_anatomy_item
				      where n_anatitem_obo_id = m_anatitem_new_zdb_id)
	where m_anatitem_new_zdb_id in (select n_anatitem_obo_id
					  from new_anatomy_item);

!echo '== update obo id to zdb id on merged old term =='
update merged_anatomy_item
	set m_anatitem_old_zdb_id = (select ait_zdb_id 
			               from ao_id_translation
				      where m_anatitem_old_zdb_id = ait_obo_id)
      where m_anatitem_old_zdb_id in (select ait_obo_id
					from ao_id_translation);

-------------------------------------------------------------------
-- Obsolete anatomy items
--
-- create obsolete_anatomy_item table to hold terms that is marked
-- as obsolete.
-------------------------------------------------------------------
!echo '=====  obsolete anatomy items ====='
create temp table obsolete_anatomy_item(
	o_anatitem_zdb_id	varchar(50)
)with no log;

!echo '== load anatitem_obsolete.unl =='
load from "anatitem_obsolete.unl" insert into obsolete_anatomy_item;


-------------------------------------------------------------------
-- Anatomy term synonyms/alias 
--
-- create input_data_alias table, load anatalias.unl. Update new 
-- anatomy terms' zdb id if needed
-------------------------------------------------------------------
!echo '===== new_anatomy_alias ====='
create temp table input_data_alias (
	i_dalias_data_zdb_id	varchar(50),
	i_dalias_alias		varchar(255),
	i_dalias_attribution	varchar(50),
	i_dalias_zdb_id		varchar(50)
) with no log;

!echo '== load anatalias.unl =='
load from "anatalias.unl" insert into input_data_alias;

!echo '== update obo id to zdb id on anat term =='
update input_data_alias
	set i_dalias_data_zdb_id = (select ait_zdb_id 
			              from ao_id_translation
				     where i_dalias_data_zdb_id = ait_obo_id)
      where i_dalias_data_zdb_id in (select ait_obo_id
					 from ao_id_translation);

!echo '== update new anatomy term zdb id =='
update input_data_alias set i_dalias_data_zdb_id = 
			(select n_anatitem_zdb_id
			   from new_anatomy_item
		 	  where n_anatitem_obo_id = i_dalias_data_zdb_id)
	where  i_dalias_data_zdb_id in 
			(select n_anatitem_obo_id
			   from new_anatomy_item);

!echo '=== alias_attribution_temp ==='
create temp table alias_attribution_temp (
	a_data_zdb_id	varchar(50),
	a_source_zdb_id	varchar(50)
)with no log;


-------------------------------------------------------------------
-- Anatomy relationship
-- 
-- create new_anatomy_relationship, load in anatrel.unl. Upate 
-- new anatomy terms' zdb id if needed
-------------------------------------------------------------------
!echo '=====  new_anatomy_relationship  ====='
create temp table new_anatomy_relationship (
	n_anatrel_anatitem_1_zdb_id	varchar(50),
	n_anatrel_anatitem_2_zdb_id 	varchar(50),
	n_anatrel_dagedit_id		varchar(20)
)with no log;

!echo '== load anatrel.unl =='
load from 'anatrel.unl' insert into new_anatomy_relationship;

!echo '== update obo id to zdb id on parent term =='
update new_anatomy_relationship 
   set n_anatrel_anatitem_1_zdb_id = (select ait_zdb_id 
			               from ao_id_translation
				      where n_anatrel_anatitem_1_zdb_id = ait_obo_id)
 where n_anatrel_anatitem_1_zdb_id in (select ait_obo_id
					 from ao_id_translation);

!echo '== update obo id to zdb id on child term =='
update new_anatomy_relationship 
   set n_anatrel_anatitem_2_zdb_id = (select ait_zdb_id 
			               from ao_id_translation
				      where n_anatrel_anatitem_2_zdb_id = ait_obo_id)
 where n_anatrel_anatitem_2_zdb_id in (select ait_obo_id
					 from ao_id_translation);

!echo '==update parent term zdb id if it is new=='
update new_anatomy_relationship 
   set n_anatrel_anatitem_1_zdb_id = 
	   	  (select n_anatitem_zdb_id
                     from new_anatomy_item
	  	    where n_anatitem_obo_id = n_anatrel_anatitem_1_zdb_id)
  where n_anatrel_anatitem_1_zdb_id in 
		  (select n_anatitem_obo_id
                     from new_anatomy_item);

!echo '==update child term zdb id if it is new=='
 update new_anatomy_relationship 
    set n_anatrel_anatitem_2_zdb_id = 
	   	  (select n_anatitem_zdb_id
                     from new_anatomy_item
	  	    where n_anatitem_obo_id = n_anatrel_anatitem_2_zdb_id)
  where n_anatrel_anatitem_2_zdb_id in 
		  (select n_anatitem_obo_id
                     from new_anatomy_item);

!echo '==== generate anatomy relationship list for content comparison ===='

execute procedure create_anatomy_relationship_list();

create temp table anatomy_relationship_list_before (
	arlb_anatitem_zdb_id	varchar(50),
	arlb_contained_by	varchar(255),
	arlb_contains 		lvarchar,
	arlb_develops_from	varchar(255),
	arlb_develops_into	varchar(255)
)with no log;

-- anatomy_relationship_list_temp is a temp table created
-- inside create_anatomy_relationship_list(). As we will
-- reuse the routine to generate anatomy relationship list
-- for incoming data, save the old content to another table

insert into anatomy_relationship_list_before
	select * from anatomy_relationship_list_temp;


!echo "============================================================"
!echo "====   Load in /Delete from real tables                 ===="
!echo "============================================================"

begin work;

-----------------------------------------------------
-- Wipe off anatomy_relationship 
--
-- So that we get the freedom to update AO term stage
-- An alternative is to save it in a temp, if we need 
-- to access the content for comparison.
-----------------------------------------------------

!echo '== delete old anatomy_relationship =='
delete from anatomy_relationship;

------------------------------------------------------------
-- Update anatomy_item
-------------------------------------------------------------
create temp table tmp_ao_updates (
	--t_submitter_id	varchar(50),
	t_rec_id	varchar(50),
	t_field_name	varchar(50),
	t_new_value	html,
	t_old_value	html,
	t_when		datetime year to fraction(3),
	t_comments	lvarchar
	--sumbitter_name	varchar(80)
)with no log;	

!echo '== update anatitem_name =='

insert into tmp_ao_updates(t_rec_id,t_field_name,t_new_value,t_old_value,t_when) 
	select anatitem_zdb_id, "anatitem_name",
	       u_anatitem_name, anatitem_name, CURRENT
	  from anatomy_item join updated_anatomy_item
	       on anatitem_zdb_id = u_anatitem_zdb_id
         where anatitem_name <> u_anatitem_name;

update anatomy_item
   set anatitem_name = (select u_anatitem_name
			  from updated_anatomy_item
			 where anatomy_item.anatitem_zdb_id = u_anatitem_zdb_id)
  where exists (select 'x'
		  from updated_anatomy_item
	         where anatitem_zdb_id = u_anatitem_zdb_id
		   and anatitem_name <> u_anatitem_name);

!echo '== update anatitem_start_stg_zdb_id =='

insert into tmp_ao_updates (t_rec_id,t_field_name,t_new_value,t_old_value,t_when) 
	select anatitem_zdb_id, "anatitem_start_stg_zdb_id",
	       sn.stg_name, so.stg_name, CURRENT
	  from anatomy_item join updated_anatomy_item
	       on anatitem_zdb_id = u_anatitem_zdb_id
	       join stage so
	       on anatitem_start_stg_zdb_id = so.stg_zdb_id
               join stage sn
               on u_anatitem_start_stg_zdb_id = sn.stg_zdb_id
         where anatitem_start_stg_zdb_id <> u_anatitem_start_stg_zdb_id;

update anatomy_item
   set anatitem_start_stg_zdb_id = (select u_anatitem_start_stg_zdb_id
			  from updated_anatomy_item
			 where anatitem_zdb_id = u_anatitem_zdb_id)
  where anatitem_start_stg_zdb_id <> (select u_anatitem_start_stg_zdb_id
			    from updated_anatomy_item
			   where anatitem_zdb_id = u_anatitem_zdb_id);

!echo '== update anatitem_end_stg_zdb_id =='

insert into tmp_ao_updates (t_rec_id,t_field_name,t_new_value,t_old_value,t_when) 
	select anatitem_zdb_id, "anatitem_end_stg_zdb_id",
	       sn.stg_name, so.stg_name, CURRENT
	  from anatomy_item join updated_anatomy_item
	       on anatitem_zdb_id = u_anatitem_zdb_id
	       join stage so
	       on anatitem_end_stg_zdb_id = so.stg_zdb_id
               join stage sn
               on u_anatitem_end_stg_zdb_id = sn.stg_zdb_id
         where anatitem_end_stg_zdb_id <> u_anatitem_end_stg_zdb_id;

update anatomy_item
   set anatitem_end_stg_zdb_id = (select u_anatitem_end_stg_zdb_id
			  from updated_anatomy_item
			 where anatitem_zdb_id = u_anatitem_zdb_id)
  where anatitem_end_stg_zdb_id <> (select u_anatitem_end_stg_zdb_id
			    from updated_anatomy_item
			   where anatitem_zdb_id = u_anatitem_zdb_id);

!echo '== update anatitem_definition =='

insert into tmp_ao_updates (t_rec_id,t_field_name,t_new_value,t_old_value,t_when) 
	select anatitem_zdb_id, "anatitem_definition",
	       u_anatitem_definition, anatitem_definition, CURRENT
	  from anatomy_item join updated_anatomy_item
	       on anatitem_zdb_id = u_anatitem_zdb_id
         where anatitem_definition <> u_anatitem_definition;

update anatomy_item
   set anatitem_definition = (select u_anatitem_definition
			  from updated_anatomy_item
			 where anatitem_zdb_id = u_anatitem_zdb_id)
  where anatitem_definition <> (select u_anatitem_definition
			    from updated_anatomy_item
			   where anatitem_zdb_id = u_anatitem_zdb_id);

!echo '== add new anatitem_definition =='

insert into tmp_ao_updates (t_rec_id, t_field_name, t_new_value, t_when, t_comments)
	select anatitem_zdb_id, "anatitem_definition",
	       u_anatitem_definition, CURRENT, "Add definition."
	  from anatomy_item join updated_anatomy_item
	       on anatitem_zdb_id = u_anatitem_zdb_id
         where anatitem_definition is null
           and u_anatitem_definition is not null;

update anatomy_item
   set anatitem_definition = (select u_anatitem_definition
			  from updated_anatomy_item
			 where anatitem_zdb_id = u_anatitem_zdb_id)
  where exists (
	select u_anatitem_definition
	  from updated_anatomy_item
         where anatitem_zdb_id = u_anatitem_zdb_id
           and anatitem_definition is null
           and u_anatitem_definition is not null );

-- we are not interested in recording changes on comments
!echo '== update anatitem_description =='

update anatomy_item
   set anatitem_description = (select u_anatitem_description
			         from updated_anatomy_item
			        where anatitem_zdb_id = u_anatitem_zdb_id)
  where anatitem_description is not null
    and anatitem_description <> (select u_anatitem_description
		      	          from updated_anatomy_item
		     	         where anatitem_zdb_id = u_anatitem_zdb_id);

!echo '== add new anatitem_description =='
update anatomy_item
   set anatitem_description = (select u_anatitem_description
				 from updated_anatomy_item
			 	where anatitem_zdb_id = u_anatitem_zdb_id)
  where anatitem_description is null
    and exists (select u_anatitem_description
	          from updated_anatomy_item
         	 where anatitem_zdb_id = u_anatitem_zdb_id
           	   and u_anatitem_description is not null );


------------------------------------------------------------
-- Load new anatomy_item
-------------------------------------------------------------
!echo '== add new anatomy_item =='
insert into zdb_active_data (zactvd_zdb_id)
     select n_anatitem_zdb_id
       from new_anatomy_item;

insert into anatomy_item (anatitem_zdb_id, anatitem_obo_id, anatitem_name,
			  anatitem_start_stg_zdb_id, 
			  anatitem_end_stg_zdb_id,
			  anatitem_definition, anatitem_description)
      select n_anatitem_zdb_id, n_anatitem_obo_id, n_anatitem_name,
	     n_anatitem_start_stg_zdb_id, n_anatitem_end_stg_zdb_id, 
	     n_anatitem_definition, n_anatitem_description 
        from new_anatomy_item;


---------------------------------------------------------
-- Update expression data on merged terms
---------------------------------------------------------

!echo '=== update expression_result on merged terms ==='
-- Step 1
-- find out annotations exist on both new and old terms,
-- and delete the one on the to-be-merged(old) terms
-- with track record in zdb_replaced_data

select o.xpatres_zdb_id as txo_old_xpatres_zdb_id,
       n.xpatres_zdb_id as txo_new_xpatres_zdb_id
  from merged_anatomy_item
	join expression_result o
		on m_anatitem_old_zdb_id = o.xpatres_anat_item_zdb_id
	join expression_result n
		on m_anatitem_new_zdb_id = n.xpatres_anat_item_zdb_id
 where o.xpatres_xpatex_zdb_id = n.xpatres_xpatex_zdb_id 
   and o.xpatres_start_stg_zdb_id = n.xpatres_start_stg_zdb_id 
   and o.xpatres_end_stg_zdb_id = n.xpatres_end_stg_zdb_id 
   and o.xpatres_expression_found = n.xpatres_expression_found
-- xpatres_comment is not significant to distinguish two records
into temp tmp_xpatres_merge_pair with no log;

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id)
	select txo_old_xpatres_zdb_id,txo_new_xpatres_zdb_id
          from tmp_xpatres_merge_pair;

delete from zdb_active_data 
	where exists (
	   select 'x'
	     from tmp_xpatres_merge_pair
	    where zactvd_zdb_id = txo_old_xpatres_zdb_id);

-- Step 2
-- separate out annotations on old terms in a temp table, replace terms
-- with the corresponding new ones. 
create temp table tmp_xpatres_merge_record (
	txm_xpatres_zdb_id 	varchar(50),
	txm_xpatex_zdb_id	varchar(50),
	txm_start_stg_zdb_id	varchar(50),
	txm_end_stg_zdb_id	varchar(50),
	txm_anat_item_zdb_id	varchar(50),
	txm_xpat_found		boolean,
	txm_comments		varchar(255)
)with no log;

insert into tmp_xpatres_merge_record (txm_xpatex_zdb_id,
			              txm_start_stg_zdb_id, txm_end_stg_zdb_id,
		 	      	      txm_anat_item_zdb_id,txm_xpat_found,
			      	      txm_comments)
-- More than one term might be merged into the same term, thus 'distinct' is needed.
  select distinct xpatres_xpatex_zdb_id,xpatres_start_stg_zdb_id,
         xpatres_end_stg_zdb_id,m_anatitem_new_zdb_id,
         xpatres_expression_found, "from merge"
    from merged_anatomy_item
	 join expression_result
		on m_anatitem_old_zdb_id = xpatres_anat_item_zdb_id;

update tmp_xpatres_merge_record set txm_xpatres_zdb_id = get_id ("XPATRES");
insert into zdb_active_data (zactvd_zdb_id)
	select txm_xpatres_zdb_id from tmp_xpatres_merge_record;

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id)
	select xpatres_zdb_id, txm_xpatres_zdb_id
          from expression_result, merged_anatomy_item, tmp_xpatres_merge_record
	 where xpatres_anat_item_zdb_id = m_anatitem_old_zdb_id
           and txm_anat_item_zdb_id = m_anatitem_new_zdb_id
	   and xpatres_xpatex_zdb_id = txm_xpatex_zdb_id
	   and xpatres_start_stg_zdb_id = txm_start_stg_zdb_id
           and xpatres_end_stg_zdb_id = txm_end_stg_zdb_id
           and xpatres_expression_found = txm_xpat_found;
    
delete from zdb_active_data 
	where exists 
	       (select 'x'
	          from merged_anatomy_item join 
                       expression_result
			   on m_anatitem_old_zdb_id = xpatres_anat_item_zdb_id
	         where zactvd_zdb_id = xpatres_zdb_id);
  
insert into expression_result (xpatres_zdb_id, xpatres_xpatex_zdb_id,
			xpatres_start_stg_zdb_id,  xpatres_end_stg_zdb_id,
			xpatres_anat_item_zdb_id, xpatres_expression_found,
			xpatres_comments)
     select * 
       from tmp_xpatres_merge_record;

-- Step 3  Delete merged anatomy terms
-- 
-- Track anatomy term merge in zdb_replaced_data.
-- Delete merged anatomy items from zdb_active_data.
-- There is on-delete-cascade on synonyms, but that would have been 
-- transferred to the new terms already by Dagedit. There is(should be)  
-- no on-delete-cascade to annotations, while that should have been taken
-- care of by now.

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id)
	select m_anatitem_old_zdb_id, m_anatitem_new_zdb_id
	  from merged_anatomy_item;

insert into tmp_ao_updates(t_rec_id, t_field_name, t_old_value, t_when, t_comments)
    select m_anatitem_new_zdb_id, "merge "||m_anatitem_old_zdb_id, m_anatitem_old_zdb_id, 
           CURRENT, anatitem_name || " was merged in." 
      from merged_anatomy_item join anatomy_item
           on m_anatitem_old_zdb_id = anatitem_zdb_id;

delete from zdb_active_data 
      where exists (select 'x'
		      from merged_anatomy_item
		     where zactvd_zdb_id = m_anatitem_old_zdb_id);

		
-----------------------------------------------------------------
-- Delete dead alias/synonym, Keep zdb id on unchanged alias
-- Load in new ones. 
-----------------------------------------------------------------

!echo '== delete dead synonym from zdb_active_data =='
select dalias_zdb_id t_dalias_id, dalias_data_zdb_id t_dalias_data_id,
       dalias_alias t_dalias_alias
  from data_alias
 where dalias_data_zdb_id like "ZDB-ANAT-%"
   and not exists 
	(select 't'
	   from input_data_alias
	  where dalias_data_zdb_id = i_dalias_data_zdb_id
	    and dalias_alias = i_dalias_alias )
into temp tmp_obsolete_alias with no log;


insert into tmp_ao_updates(t_rec_id, t_field_name, t_old_value, t_when, t_comments)
      select t_dalias_data_id, "synonym "||t_dalias_id, t_dalias_alias, 
	     CURRENT, "Deleted."
        from tmp_obsolete_alias;

-- delete current synonym, cascade to record_attribution 
delete from zdb_active_data 
      where zactvd_zdb_id in 
		(select t_dalias_id
		   from tmp_obsolete_alias);

!echo '== validate record attribution  =='
unload to "pub_incorrect.err" 
   select i_dalias_data_zdb_id, i_dalias_alias, i_dalias_attribution
     from input_data_alias
    where i_dalias_attribution is not null
      and not exists ( select 't' 
			 from publication 
		        where zdb_id = i_dalias_attribution);

!echo '== get current synonym attribution =='
insert into alias_attribution_temp 
	select dalias_zdb_id, i_dalias_attribution
          from input_data_alias,data_alias
	 where dalias_data_zdb_id = i_dalias_data_zdb_id
	   and dalias_alias = i_dalias_alias
	   and i_dalias_attribution is not null;

!echo '== delete dead synonym attribution =='
delete from record_attribution 
      where exists (select 't'
                      from alias_attribution_temp 
                     where recattrib_data_zdb_id = a_data_zdb_id
                       and recattrib_source_zdb_id <> a_source_zdb_id);
!echo '== put in attribution on existing synonym =='
insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
	select * from alias_attribution_temp ;

!echo '== delete unchanged ones from the new input =='
delete from input_data_alias
      where exists (select 't'
                      from data_alias
		     where dalias_data_zdb_id = i_dalias_data_zdb_id
	   	       and dalias_alias = i_dalias_alias );

!echo '== get data alias zdb id and load =='
update input_data_alias set i_dalias_zdb_id = get_id("DALIAS");

insert into zdb_active_data(zactvd_zdb_id) 
	select i_dalias_zdb_id
	  from input_data_alias;

insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias)
	select i_dalias_zdb_id, i_dalias_data_zdb_id, i_dalias_alias
	  from input_data_alias;

insert into tmp_ao_updates(t_rec_id, t_field_name, t_new_value, t_when, t_comments)
      select i_dalias_data_zdb_id, "synonym "||i_dalias_zdb_id, i_dalias_alias, 
	     CURRENT, "New."
	from input_data_alias;

!echo '== put in attribution on new synonym =='
insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
	select i_dalias_zdb_id, i_dalias_attribution
          from input_data_alias
	 where i_dalias_attribution is not null;

----------------------------------------------------------------
-- Flag obsolete anatomy term
--
-- obsolete term is not supposed to have any additional information
-- besides name and id. We keep the stage range due to the database 
-- constraint. Ok to keep the constraint there, we just not showing 
-- them and using them any more. 
-- No need to keep record in updates, since we are not 
-- showing obsolete term from the web, only keep them for
-- DAG edit files. 
---------------------------------------------------------------
!echo '==  flag obsolete term =='
update anatomy_item set anatitem_is_obsolete = 't'
      where exists (select 'x'
		      from obsolete_anatomy_item
		     where anatitem_zdb_id = o_anatitem_zdb_id);


-----------------------------------------------------------------
-- Load anatomy_relationship
-----------------------------------------------------------------

!echo '== unload startStgInconsistent.err == '	
unload to "startStgInconsistent.err"
select pa.anatitem_name, ca.anatitem_name, n_anatrel_dagedit_id, p.stg_hours_start, c.stg_hours_start
  from new_anatomy_relationship, anatomy_item pa, anatomy_item ca, 
       stage p, stage c
 where n_anatrel_anatitem_1_zdb_id = pa.anatitem_zdb_id
   and n_anatrel_anatitem_2_zdb_id = ca.anatitem_zdb_id
   and pa.anatitem_start_stg_zdb_id = p.stg_zdb_id
   and ca.anatitem_start_stg_zdb_id = c.stg_zdb_id
   and p.stg_hours_start > c.stg_hours_start
   and c.stg_name <> "Unknown";

!echo '== unload endStgInconsistent.err == '	
unload to "endStgInconsistent.err"  
select pa.anatitem_name, ca.anatitem_name, n_anatrel_dagedit_id, p.stg_hours_end, c.stg_hours_end
  from new_anatomy_relationship, anatomy_item pa, anatomy_item ca, 
       stage p, stage c
 where n_anatrel_anatitem_1_zdb_id = pa.anatitem_zdb_id
   and n_anatrel_anatitem_2_zdb_id = ca.anatitem_zdb_id
   and pa.anatitem_end_stg_zdb_id = p.stg_zdb_id
   and ca.anatitem_end_stg_zdb_id = c.stg_zdb_id
   and p.stg_hours_end < c.stg_hours_end
   and n_anatrel_dagedit_id <> "develops_from";
  
!echo '== load in anatomy_relationship =='
insert into anatomy_relationship (anatrel_anatitem_1_zdb_id, 
				  anatrel_anatitem_2_zdb_id,
				  anatrel_dagedit_id)
     select * from new_anatomy_relationship ;

!echo '== generate anatomy relationship list  =='
execute procedure create_anatomy_relationship_list();

!echo '== track anatomy relationship updates =='
insert into tmp_ao_updates (t_rec_id,t_field_name,t_new_value,t_old_value,t_when)
	select arlt_anatitem_zdb_id, "contained_by", arlt_contained_by,
	       arlb_contained_by, CURRENT	
	  from anatomy_relationship_list_temp join
	       anatomy_relationship_list_before
               on arlt_anatitem_zdb_id = arlb_anatitem_zdb_id	
         where arlt_contained_by <>  arlb_contained_by;

insert into tmp_ao_updates (t_rec_id,t_field_name,t_new_value,t_old_value,t_when)
	select arlt_anatitem_zdb_id, "contains", arlt_contains,
	       arlb_contains, CURRENT	
	  from anatomy_relationship_list_temp join
	       anatomy_relationship_list_before
               on arlt_anatitem_zdb_id = arlb_anatitem_zdb_id	
         where arlt_contains <>  arlb_contains;

insert into tmp_ao_updates (t_rec_id,t_field_name,t_new_value,t_old_value,t_when)
	select arlt_anatitem_zdb_id, "develops_from", arlt_develops_from,
	       arlb_develops_from, CURRENT	
	  from anatomy_relationship_list_temp join
	       anatomy_relationship_list_before
               on arlt_anatitem_zdb_id = arlb_anatitem_zdb_id	
         where arlt_develops_from <>  arlb_develops_from;

insert into tmp_ao_updates (t_rec_id,t_field_name,t_new_value,t_old_value,t_when)
	select arlt_anatitem_zdb_id, "develops_into", arlt_develops_into,
	       arlb_develops_into, CURRENT	
	  from anatomy_relationship_list_temp join
	       anatomy_relationship_list_before
               on arlt_anatitem_zdb_id = arlb_anatitem_zdb_id	
         where arlt_develops_into <>  arlb_develops_into;


!echo '== track updates to updates table =='

insert into updates (submitter_id, submitter_name, rec_id,
                     field_name,new_value,old_value,when, comments)
    select "ZDB-PERS-000914-2", "Melissa Anne Haendel",
	   t_rec_id, t_field_name,
           t_new_value, t_old_value, t_when, t_comments
      from tmp_ao_updates;


!echo "============================================================"
!echo "====   Verify XPAT annotation with the new AO           ===="
!echo "============================================================"

unload to "annotationViolates.err"
	    select xpatex_source_zdb_id, 
		   s1.stg_abbrev, s1.stg_zdb_id,
                   s2.stg_abbrev, s2.stg_zdb_id,
		   anatitem_name, anatitem_zdb_id,
		   s3.stg_abbrev, s3.stg_zdb_id,
		   s4.stg_abbrev, s4.stg_zdb_id
	      from expression_result 
		   join expression_experiment
			on xpatres_xpatex_zdb_id = xpatex_zdb_id
                   join anatomy_item
                        on xpatres_anat_item_zdb_id = anatitem_zdb_id
                   join stage s1
                        on xpatres_start_stg_zdb_id = s1.stg_zdb_id
                   join stage s2
                        on xpatres_end_stg_zdb_id = s2.stg_zdb_id
                   join stage s3
                        on anatitem_start_stg_zdb_id = s3.stg_zdb_id
                   join stage s4
                        on anatitem_end_stg_zdb_id = s4.stg_zdb_id
             where anatitem_overlaps_stg_window(
                                     xpatres_anat_item_zdb_id,
                                     xpatres_start_stg_zdb_id,
                                     xpatres_end_stg_zdb_id
                                     ) = "f";

rollback work;

--if no error from the screen, numbers looks right, and except 
--annotationViolates.err the other three .err files are with zero length,
--then send content in annotationViolates.err to Ceri and Melissa, and 
--ask for a file AO_translation.unl from one of them with file format
-- ZDB-ANAT-XXXX|ZDB-STAGE-XXXX|ZDB-STAGE-XXXX|ZDB-ANAT-XXXX
-- 
--commit work;
 