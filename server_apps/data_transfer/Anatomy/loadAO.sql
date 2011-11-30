----------------------------------------------------------------
-- FILE: loadAO.sql
--
-- The script loads AO files produced by parser into ZFIN db.
--
-- INPUT:
--      anatitem_new.unl  : terms without xref_analog line
--      anatitem_exist.unl: terms with xref_analog line
--      anatitem_merged.unl: ZDB-ANAT-######-##|ZDB-ANAT-######-##
--      anatitem_obsolete.unl: ZDB-ANAT-######-##|
--      anatrel.unl  : ZFA:#######(parent)|ZFA:#######(child)|rel_type|
--      anatalias.unl: ZFA:#######|synonym|ZDB-PUB-######-##|
--      dblink_ids.unl    : ZFA:#######|CL/CARO:#######|
--      anatitem_def_attrib.unl    : ZFA:#######|ZDB-PUB-######-##|
--
-- OUTPUT:
--     start_startInconsistent.err
--     end_endInconsistent.err
--     end_startInconsistent.err
--     pub_incorrect.err
--     obsolete_anat_with_xpat.err
--     obsolete_anat_with_pato.err
--     annotationViolates.err
-- 		 Send content to curators 
--     
-- EFFECT:
--      anatomy_item: new/updated entries
--      anatomy_relationship: wipe off and reload
--      data_alias: new/updated entries
--      record_attribution: new definition entries
--      expression_result  : replace merged anatomy term 
--			     with the replacer
--      zdb_active_data: drop obsolete and merged anatomy term,
--                   old alias. Load in new terms, and data alias.
--      updates: record anatomy and anatomy relationship updates
--
----------------------------------------------------------------

begin work;


!echo '====================================================='
!echo '===== Load data into temp table and Prepare       ==='
!echo '====================================================='


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
		(select stg_zdb_id
		   from stage
		  where n_anatitem_start_stg_zdb_id = stg_obo_id);
update new_anatomy_item
   set n_anatitem_end_stg_zdb_id = 
		(select stg_zdb_id
		   from stage
		  where n_anatitem_end_stg_zdb_id = stg_obo_id);
 
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

select u_anatitem_name from updated_anatomy_item where not exists 
                 (select stg_zdb_id
		   from stage
		  where u_anatitem_start_stg_zdb_id = stg_obo_id);
select u_anatitem_name from updated_anatomy_item where not exists 
                 (select stg_zdb_id
		   from stage
		  where u_anatitem_end_stg_zdb_id = stg_obo_id);

update updated_anatomy_item
   set u_anatitem_start_stg_zdb_id = 
		(select stg_zdb_id
		   from stage
		  where u_anatitem_start_stg_zdb_id = stg_obo_id);
update updated_anatomy_item
   set u_anatitem_end_stg_zdb_id = 
		(select stg_zdb_id
		   from stage
		  where u_anatitem_end_stg_zdb_id = stg_obo_id);

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
	set m_anatitem_new_zdb_id = (select anatitem_zdb_id 
			               from anatomy_item
				      where m_anatitem_new_zdb_id = anatitem_obo_id)
      where m_anatitem_new_zdb_id in (select anatitem_obo_id
				        from anatomy_item);

!echo '== update merged new term zdb id if it is a new term =='
update merged_anatomy_item
	set m_anatitem_new_zdb_id = (select n_anatitem_zdb_id
				       from new_anatomy_item
				      where n_anatitem_obo_id = m_anatitem_new_zdb_id)
	where m_anatitem_new_zdb_id in (select n_anatitem_obo_id
					  from new_anatomy_item);


delete from merged_anatomy_item where not exists (select * from anatomy_item where anatitem_zdb_id = m_anatitem_old_zdb_id);

select * from merged_anatomy_item;

select n.anatitem_name, o.anatitem_name
from anatomy_item n, anatomy_item o, merged_anatomy_item
where m_anatitem_new_zdb_id = n.anatitem_zdb_id
  and m_anatitem_old_zdb_id = o.anatitem_zdb_id;

select * from merged_anatomy_item where not exists (select * from anatomy_item where m_anatitem_old_zdb_id = anatitem_zdb_id);
  


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

-- please add p_anatitem_obo_id to the temp table to identify the matching term records
unload to "obsolete_anat_with_xpat.err" 
   select distinct o_anatitem_zdb_id
     from obsolete_anatomy_item, term, anatomy_item, expression_result
    where xpatres_superterm_zdb_id = term_zdb_id
      and term_ont_id = anatitem_obo_id
      and o_anatitem_zdb_id = anatitem_zdb_id;


unload to "obsolete_anat_with_pato_entity1.err" 
   select o_anatitem_zdb_id
     from obsolete_anatomy_item
    where exists (select 'x' 
                    from phenotype_statement, term, anatomy_item
                   where o_anatitem_zdb_id = anatitem_zdb_id 
                   and anatitem_obo_id = term_ont_id
                   and (phenos_entity_1_subterm_zdb_id = term_zdb_id
                      or phenos_entity_1_superterm_zdb_id = term_zdb_id) 
                  );


unload to "obsolete_anat_with_pato_entity2.err" 
   select o_anatitem_zdb_id
     from obsolete_anatomy_item
    where exists (select 'x' 
                    from phenotype_statement, term, anatomy_item
                   where o_anatitem_zdb_id = anatitem_zdb_id 
                   and anatitem_obo_id = term_ont_id
                   and (phenos_entity_2_subterm_zdb_id = term_zdb_id
                      or phenos_entity_2_superterm_zdb_id = term_zdb_id) 
                  );
                      
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
	i_dalias_group		varchar(20),
	i_dalias_attribution	varchar(50),
	i_dalias_zdb_id		varchar(50)
) with no log;

!echo '== load anatalias.unl =='
load from "anatalias.unl" insert into input_data_alias;

!echo '== update obo id to zdb id on anat term =='
update input_data_alias
	set i_dalias_data_zdb_id = (select anatitem_zdb_id
			              from anatomy_item
				     where i_dalias_data_zdb_id = anatitem_obo_id)
      where i_dalias_data_zdb_id in (select anatitem_obo_id
				       from anatomy_item);

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
-- Anatomy term dblinks  (CL:## or CARO:##)
--
-- create input_db_link table, load dblink_ids.unl. 
-------------------------------------------------------------------
!echo '===== new_anatomy_alias ====='
create temp table input_db_link (
	i_dblink_acc_num	varchar(30),
	i_dblink_data_zdb_id	varchar(30),
	i_dblink_fdb_name	varchar(30),
	i_dblink_zdb_id		varchar(30)

) with no log;

!echo '== load dblink_ids.unl =='
load from "dblink_ids.unl" insert into input_db_link;

!echo '== update obo id to zdb id on anat term =='
update input_db_link
	set i_dblink_data_zdb_id = (select anatitem_zdb_id
			              from anatomy_item
				     where i_dblink_data_zdb_id = anatitem_obo_id)
      where i_dblink_data_zdb_id in (select anatitem_obo_id
				       from anatomy_item);

!echo '== update new anatomy term zdb id =='
update input_db_link set i_dblink_data_zdb_id = 
			(select n_anatitem_zdb_id
			   from new_anatomy_item
		 	  where n_anatitem_obo_id = i_dblink_data_zdb_id)
	where  i_dblink_data_zdb_id in 
			(select n_anatitem_obo_id
			   from new_anatomy_item);

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
   set n_anatrel_anatitem_1_zdb_id = (select anatitem_zdb_id
			                from anatomy_item
				       where n_anatrel_anatitem_1_zdb_id = anatitem_obo_id)
 where n_anatrel_anatitem_1_zdb_id in (select anatitem_obo_id
					 from anatomy_item);

!echo '== update obo id to zdb id on child term =='
update new_anatomy_relationship 
   set n_anatrel_anatitem_2_zdb_id = (select anatitem_zdb_id 
			                from anatomy_item
				      where n_anatrel_anatitem_2_zdb_id = anatitem_obo_id)
 where n_anatrel_anatitem_2_zdb_id in (select anatitem_obo_id
					 from anatomy_item);

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

-- unload anatomy terms with newly updated name for case2059
!echo 'unload anatomy terms with newly updated name for case2059'
 unload to "name_updated_anatitem.rpt"
   select u_anatitem_name, anatitem_name, anatitem_zdb_id, ss.stg_abbrev, se.stg_abbrev
     from anatomy_item join updated_anatomy_item
	       on anatitem_zdb_id = u_anatitem_zdb_id
          join stage ss on anatitem_start_stg_zdb_id = ss.stg_zdb_id
          join stage se on anatitem_end_stg_zdb_id = se.stg_zdb_id
    where anatitem_name <> u_anatitem_name;

-- in case the update statement gives non-unique error, uncomment the
-- two "set ... disabled" and "select" statements to diagnose

--set constraints anatitem_name_unique disabled;
--set indexes anatitem_name_index, anatitem_name_lower_index disabled;

select u_anatitem_name, u_anatitem_zdb_id from updated_anatomy_item where  exists 
( select * from anatomy_item where u_anatitem_name = anatitem_name and anatitem_zdb_id != u_anatitem_zdb_id);

update anatomy_item
   set anatitem_name = (select u_anatitem_name
			  from updated_anatomy_item
			 where anatomy_item.anatitem_zdb_id = u_anatitem_zdb_id)
  where exists (select 'x'
		  from updated_anatomy_item
	         where anatitem_zdb_id = u_anatitem_zdb_id
		   and anatitem_name <> u_anatitem_name);

--select anatitem_name
--  from anatomy_item
-- group by anatitem_name
--having count(anatitem_name) > 1;

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

-- disable trigger is needed for some cases of both stages are to be changed
set triggers anatomy_items_update_trigger disabled;

update anatomy_item
   set anatitem_start_stg_zdb_id = (select u_anatitem_start_stg_zdb_id
			  from updated_anatomy_item
			 where anatitem_zdb_id = u_anatitem_zdb_id)
  where anatitem_start_stg_zdb_id <> (select u_anatitem_start_stg_zdb_id
			    from updated_anatomy_item
			   where anatitem_zdb_id = u_anatitem_zdb_id);
-- enable trigger
set triggers anatomy_items_update_trigger enabled;

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
-- unload new anatomy terms for case2059
!echo 'unload new anatomy terms for case2059'

unload to "new_name_anatitem.rpt"
select n_anatitem_name, n_anatitem_zdb_id, ss.stg_abbrev, se.stg_abbrev
  from new_anatomy_item 
       join stage ss on n_anatitem_start_stg_zdb_id = ss.stg_zdb_id
       join stage se on n_anatitem_end_stg_zdb_id = se.stg_zdb_id;

!echo '== add new anatomy_item =='
insert into zdb_active_data (zactvd_zdb_id)
     select n_anatitem_zdb_id
       from new_anatomy_item;

select n_anatitem_zdb_id, n_anatitem_name, n_anatitem_obo_id, anatitem_zdb_Id, anatitem_name, anatitem_obo_id
from anatomy_item, new_anatomy_item
where anatitem_zdb_id != n_anatitem_zdb_id
  and anatitem_obo_id = n_anatitem_obo_id;
  

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

-- expression_pattern_figure is an extension of expression_result records
-- We have to take it into careful consideration. When later, more info
-- becomes more extensions, we have to take them all into consideration!

-- create four temp tables: xresPre, xfigPre, xresMerge, xfigMerge
-- Pre tables keep track of records that exists before the load for
-- terms that will be merged into other terms. 
-- Merge tables have updated ZDB-IDs of the pre tables. 
-- xres tables are expression_result tables and xfig tables are 
-- expression_pattern_figure tables.

create temp table xresPre (
	p_xpatres_zdb_id 	varchar(50),
	p_xpatex_zdb_id		varchar(50),
	p_start_stg_zdb_id	varchar(50),
	p_end_stg_zdb_id	varchar(50),
	p_superterm_zdb_id	varchar(50),
	p_subterm_zdb_id	varchar(50),
	p_anatitem_zdb_id	varchar(50),
	p_expression_found	boolean,
	p_comments		varchar(255)
)with no log;

create temp table xresMerge (
	m_xpatres_zdb_id 	varchar(50),
	m_update_xpatres_zdb_id	varchar(50),
	m_xpatex_zdb_id		varchar(50),
	m_start_stg_zdb_id	varchar(50),
	m_end_stg_zdb_id	varchar(50),
	m_superterm_zdb_id	varchar(50),
	m_subterm_zdb_id	varchar(50),
	m_anatitem_zdb_id	varchar(50),
	m_expression_found	boolean,
	m_is_new 		boolean default 't',
	m_comments		varchar(255)
)with no log;

create temp table xfigPre (
	p_xpatfig_xpatres_zdb_id	varchar(50),
	p_xpatfig_fig_zdb_id		varchar(50)
)with no log;

create temp table xfigMerge (
	m_xpatfig_xpatres_zdb_id	varchar(50),
	m_xpatfig_fig_zdb_id		varchar(50)
)with no log;

-- PRE Step 1
-- find the current records for terms that will be merged and 
-- create records in the pre tables.

insert into xresPre (
	p_xpatres_zdb_id 	,
	p_xpatex_zdb_id		,
	p_start_stg_zdb_id	,
	p_end_stg_zdb_id	,
	p_superterm_zdb_id	,
	p_subterm_zdb_id	,
	p_expression_found	,
	p_comments		,
	p_anatitem_zdb_id	)
select 
	xpatres_zdb_id 	,
	xpatres_xpatex_zdb_id		,
	xpatres_start_stg_zdb_id	,
	xpatres_end_stg_zdb_id	,
	xpatres_superterm_zdb_id	,
	xpatres_subterm_zdb_id	,
	xpatres_expression_found		,
	xpatres_comments		,
	anatitem_zdb_id	
from merged_anatomy_item, expression_result, anatomy_item, term
where m_anatitem_old_zdb_id = anatitem_zdb_id
  and anatitem_obo_id = term_ont_id
  and term_zdb_id = xpatres_superterm_zdb_id;
	


insert into xfigPre (
	p_xpatfig_xpatres_zdb_id ,
	p_xpatfig_fig_zdb_id	)
select xpatfig_xpatres_zdb_id, xpatfig_fig_zdb_id
  from xresPre, expression_pattern_figure
 where p_xpatres_zdb_id = xpatfig_xpatres_zdb_id;


-- Merge Step 1
-- put the Pre records into the merge table. 


insert into xresMerge (
	m_xpatres_zdb_id 	,
	m_xpatex_zdb_id		,
	m_start_stg_zdb_id	,
	m_end_stg_zdb_id	,
	m_superterm_zdb_id	,
	m_subterm_zdb_id	,
	m_expression_found	,
	m_comments		,
	m_anatitem_zdb_id	)
select 
	p_xpatres_zdb_id 	,
	p_xpatex_zdb_id		,
	p_start_stg_zdb_id	,
	p_end_stg_zdb_id	,
	p_superterm_zdb_id	,
	p_subterm_zdb_id	,
	p_expression_found	,
	p_comments		,
	p_anatitem_zdb_id	
from xresPre;

insert into xfigMerge (
	m_xpatfig_xpatres_zdb_id ,
	m_xpatfig_fig_zdb_id	)
select p_xpatfig_xpatres_zdb_id, p_xpatfig_fig_zdb_id
  from xfigPre;



-- update the TERM ids in the xresMerge table

update xresMerge set m_superterm_zdb_id = 
(select term_zdb_id from term, anatomy_item, merged_anatomy_item
where m_anatitem_zdb_id = m_anatitem_old_zdb_id
  and anatitem_zdb_id = m_anatitem_new_zdb_id
  and anatitem_obo_id = term_ont_id);

-- if any of the xresMerge records already exist in xpatres, then 
-- update the xresMerge new_xpatres_zdb_id value, and 
-- update the xfigMerge xpatres_zdb_id value

update xresMerge set m_update_xpatres_zdb_id = 
(select xpatres_zdb_id from expression_result
 where 	m_xpatex_zdb_id     = xpatres_xpatex_zdb_id
   and  m_start_stg_zdb_id  = xpatres_start_stg_zdb_id
   and  m_end_stg_zdb_id    = xpatres_end_stg_zdb_id
   and  m_superterm_zdb_id  = xpatres_superterm_zdb_id
   and  m_subterm_zdb_id    = xpatres_subterm_zdb_id
   and  m_expression_found  = xpatres_expression_found)
where exists 
(select * from expression_result
 where 	m_xpatex_zdb_id     = xpatres_xpatex_zdb_id
   and  m_start_stg_zdb_id  = xpatres_start_stg_zdb_id
   and  m_end_stg_zdb_id    = xpatres_end_stg_zdb_id
   and  m_superterm_zdb_id  = xpatres_superterm_zdb_id
   and  m_subterm_zdb_id    = xpatres_subterm_zdb_id
   and  m_expression_found  = xpatres_expression_found)
;


update xresMerge set m_is_new = 'f'
where m_update_xpatres_zdb_id is not null;

update xresMerge set m_update_xpatres_zdb_id = get_id('XPATRES')
where m_is_new = 't';

update xfigMerge set m_xpatfig_xpatres_zdb_id = 
(select m_update_xpatres_zdb_id from xresMerge
  where m_xpatfig_xpatres_zdb_id = m_xpatres_zdb_id);


-- delete duplicate records from xfigMerge

delete from xfigMerge
where exists (
	select * from expression_pattern_figure
	where m_xpatfig_xpatres_zdb_id = xpatfig_xpatres_zdb_id
	  and m_xpatfig_fig_zdb_id = xpatfig_fig_zdb_id);

-- delete the old records from expression_result and expression_pattern_figure

delete from zdb_active_data where zactvd_zdb_id in (select p_xpatres_zdb_id from xresPre);

-- insert the newly merged records from xresMerge and xfigMerge

insert into zdb_active_data (zactvd_zdb_id) 
select m_update_xpatres_zdb_id
from xresMerge
where m_is_new = 't';

insert into expression_result(
	xpatres_zdb_id 	,
	xpatres_xpatex_zdb_id		,
	xpatres_start_stg_zdb_id	,
	xpatres_end_stg_zdb_id	,
	xpatres_superterm_zdb_id	,
	xpatres_subterm_zdb_id	,
	xpatres_expression_found		,
	xpatres_comments )
select
	m_update_xpatres_zdb_id 	,
	m_xpatex_zdb_id		,
	m_start_stg_zdb_id	,
	m_end_stg_zdb_id	,
	m_superterm_zdb_id	,
	m_subterm_zdb_id	,
	m_expression_found	,
	m_comments		
from xresMerge
where m_is_new = 't';


insert into expression_pattern_figure (xpatfig_xpatres_zdb_id, xpatfig_fig_zdb_id)
select m_xpatfig_xpatres_zdb_id, m_xpatfig_fig_zdb_id
from xfigMerge;



-- Now  Delete merged anatomy terms
-- 
-- Track anatomy term merge in zdb_replaced_data.
-- Delete merged anatomy items from zdb_active_data.
-- There is on-delete-cascade on synonyms, but that would have been 
-- transferred to the new terms already by Dagedit. There is(should be)  
-- no on-delete-cascade to annotations, while that should have been taken
-- care of by now.

delete from merged_anatomy_item
where exists( select * from zdb_replaced_data
              where zrepld_old_zdb_id = m_anatitem_old_zdb_id
                and zrepld_new_zdb_id = m_anatitem_new_zdb_id);

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
-- Delete dead Alias/Synonym, Keep zdb id on unchanged alias
-- Load in new ones. 
-----------------------------------------------------------------


delete from data_alias
where exists (select * from anatomy_item
              where dalias_data_zdb_id=anatitem_zdb_id
                and anatitem_is_obsolete = 't');


update input_data_alias set i_dalias_group = "alias" where lower(i_dalias_group) in ("exact alias","exact plural","related alias","related plural");


update input_data_alias
set i_dalias_group = (select aliasgrp_pk_id from alias_group where i_dalias_group = aliasgrp_name)
;

unload to "dalias_update.err" select i_dalias_data_zdb_id, i_dalias_alias, count(*)
from input_data_alias group by 1,2 having count(*) >1;

!echo '== data alias group second try =='


select * from data_alias where not exists (select i_dalias_group from input_data_alias
                                        where dalias_data_zdb_id=i_dalias_data_zdb_id
                                          and i_dalias_alias=dalias_alias)
 and dalias_data_zdb_id like "ZDB-ANAT-%"
   and dalias_group_id != 7
   and not exists (select * from anatomy_item where anatitem_zdb_id = dalias_data_zdb_id
                   and anatitem_is_obsolete = 't');


update data_alias set dalias_group_id = (select i_dalias_group from input_data_alias
                                        where dalias_data_zdb_id=i_dalias_data_zdb_id
                                          and i_dalias_alias=dalias_alias)
 where dalias_data_zdb_id like "ZDB-ANAT-%"
   and dalias_group_id != 7
   and not exists (select * from anatomy_item where anatitem_zdb_id = dalias_data_zdb_id
                   and anatitem_is_obsolete = 't');




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

-- delete dead synonym, cascade to record_attribution 
delete from zdb_active_data 
      where zactvd_zdb_id in 
		(select t_dalias_id
		   from tmp_obsolete_alias);

!echo '== delete unchanged ones from the new input =='
delete from input_data_alias
      where exists (select 't'
                      from data_alias, outer record_attribution
		     where dalias_data_zdb_id = i_dalias_data_zdb_id
	   	       and dalias_alias = i_dalias_alias
		       and dalias_zdb_id = recattrib_data_zdb_id
                       and i_dalias_attribution = recattrib_source_zdb_id);

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


!echo '== get data alias zdb id and load =='
update input_data_alias set i_dalias_zdb_id = get_id("DALIAS");

insert into zdb_active_data(zactvd_zdb_id) 
	select i_dalias_zdb_id
	  from input_data_alias;

select * from input_data_alias;

select * from input_data_alias where not exists (select * from anatomy_item where anatitem_zdb_id = i_dalias_data_zdb_id);


insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
	select i_dalias_zdb_id, i_dalias_data_zdb_id, i_dalias_alias, i_dalias_group
	  from input_data_alias;

insert into tmp_ao_updates(t_rec_id, t_field_name, t_new_value, t_when, t_comments)
      select i_dalias_data_zdb_id, "synonym "||i_dalias_zdb_id, i_dalias_alias, 
	     CURRENT, "New."
	from input_data_alias;

select * from input_data_alias where i_dalias_attribution is not null and not exists (select * from publication where zdb_id = i_dalias_attribution); 
delete from input_data_alias where i_dalias_attribution is not null and not exists (select * from publication where zdb_id = i_dalias_attribution); 


!echo '== put in attribution on new synonym =='
insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
	select i_dalias_zdb_id, i_dalias_attribution
          from input_data_alias
	 where i_dalias_attribution is not null;
				
-----------------------------------------------------------------
-- Delete dead Dblink on CL or CARO, Keep zdb id on unchanged alias
-- Load in new ones. 
-----------------------------------------------------------------

!echo '== delete dead synonym from zdb_active_data =='
-- we didn't use all the AK fields, sloppy, but we don't need to.
select dblink_zdb_id t_dblink_id, dblink_linked_recid t_dblink_data_id,
       dblink_acc_num t_dblink_acc_num
  from db_link
 where dblink_linked_recid like "ZDB-ANAT-%"
   and not exists 
	(select 't'
	   from input_db_link
	  where dblink_linked_recid = i_dblink_data_zdb_id
	    and dblink_acc_num = i_dblink_acc_num )
into temp tmp_obsolete_dblink with no log;


insert into tmp_ao_updates(t_rec_id, t_field_name, t_old_value, t_when, t_comments)
      select t_dblink_data_id, "dblink "||t_dblink_id, t_dblink_acc_num, 
	     CURRENT, "Deleted."
        from tmp_obsolete_dblink;

-- delete dead synonym, cascade to record_attribution 
delete from zdb_active_data 
      where zactvd_zdb_id in 
		(select t_dblink_id
		   from tmp_obsolete_dblink);

!echo '== delete unchanged ones from the new input =='
-- we didn't use all the AK fields, sloppy, but we don't need to.
delete from input_db_link
      where exists (select 't'
                      from db_link
		     where dblink_linked_recid = i_dblink_data_zdb_id
	   	       and dblink_acc_num = i_dblink_acc_num);

!echo '== get db link zdb id and load =='
update input_db_link set i_dblink_zdb_id = get_id("DBLINK");

insert into zdb_active_data(zactvd_zdb_id) 
	select i_dblink_zdb_id
	  from input_db_link;
select *
  from input_db_link
 where i_dblink_data_zdb_id not in (select anatitem_zdb_id from anatomy_item);

insert into db_link (dblink_zdb_id, dblink_linked_recid, dblink_acc_num, dblink_fdbcont_zdb_id)
	select i_dblink_zdb_id, i_dblink_data_zdb_id, i_dblink_acc_num, fdbcont_zdb_id
	  from input_db_link, foreign_db_contains, foreign_db
         where i_dblink_fdb_name = fdb_db_name
	 and fdbcont_fdb_db_id = fdb_db_pk_id;

insert into tmp_ao_updates(t_rec_id, t_field_name, t_new_value, t_when, t_comments)
      select i_dblink_data_zdb_id, "dblink "||i_dblink_zdb_id, i_dblink_acc_num, 
	     CURRENT, "New."
	from input_db_link;

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
unload to "start_startInconsistent.err"
select pa.anatitem_name, ca.anatitem_name, n_anatrel_dagedit_id, p.stg_name, c.stg_name
  from new_anatomy_relationship, anatomy_item pa, anatomy_item ca, 
       stage p, stage c
 where n_anatrel_anatitem_1_zdb_id = pa.anatitem_zdb_id
   and n_anatrel_anatitem_2_zdb_id = ca.anatitem_zdb_id
   and pa.anatitem_start_stg_zdb_id = p.stg_zdb_id
   and ca.anatitem_start_stg_zdb_id = c.stg_zdb_id
   and p.stg_hours_start > c.stg_hours_start
   and c.stg_name <> "Unknown";

unload to "end_startInconsistent.err"
select pa.anatitem_name, ca.anatitem_name, n_anatrel_dagedit_id, p.stg_name, c.stg_name
  from new_anatomy_relationship, anatomy_item pa, anatomy_item ca, 
       stage p, stage c
 where n_anatrel_anatitem_1_zdb_id = pa.anatitem_zdb_id
   and n_anatrel_anatitem_2_zdb_id = ca.anatitem_zdb_id
   and pa.anatitem_end_stg_zdb_id = p.stg_zdb_id
   and ca.anatitem_start_stg_zdb_id = c.stg_zdb_id
   and (p.stg_hours_end + 0.1) < c.stg_hours_start
   and n_anatrel_dagedit_id = "develops_from"
   and c.stg_name <> "Unknown";

!echo '== unload endStgInconsistent.err == '	
unload to "end_endInconsistent.err"  
select pa.anatitem_name, ca.anatitem_name, n_anatrel_dagedit_id, p.stg_name, c.stg_name
  from new_anatomy_relationship, anatomy_item pa, anatomy_item ca, 
       stage p, stage c
 where n_anatrel_anatitem_1_zdb_id = pa.anatitem_zdb_id
   and n_anatrel_anatitem_2_zdb_id = ca.anatitem_zdb_id
   and pa.anatitem_end_stg_zdb_id = p.stg_zdb_id
   and ca.anatitem_end_stg_zdb_id = c.stg_zdb_id
   and p.stg_hours_end < c.stg_hours_end
   and n_anatrel_dagedit_id <> "develops_from"
   and c.stg_name <> "Unknown";

select n_anatrel_anatitem_1_zdb_id from new_anatomy_relationship 
where not exists (select * from anatomy_item where anatitem_zdb_id = n_anatrel_anatitem_1_zdb_id);

select n_anatrel_anatitem_2_zdb_id from new_anatomy_relationship 
where not exists (select * from anatomy_item where anatitem_zdb_id = n_anatrel_anatitem_2_zdb_id);


--*************************************

-- When three terms are merged into one, duplicate records for anatomy_relationships are created. 
-- This finds that case and deletes one of the duplicates.

select distinct n_anatrel_anatitem_1_zdb_id tar1, n_anatrel_anatitem_2_zdb_id tar2, count(*) as tar3
from new_anatomy_relationship 
group by 1,2
having count(*) > 1
into temp tanatrel_duplicate;

insert into tanatrel_duplicate (tar1, tar2, tar3)
select anatrel_anatitem_1_zdb_id, anatrel_anatitem_2_zdb_id, 'x'
from anatomy_relationship, new_anatomy_relationship
where anatrel_anatitem_1_zdb_id = n_anatrel_anatitem_1_zdb_id
  and anatrel_anatitem_2_zdb_id = n_anatrel_anatitem_2_zdb_id;


unload to 'duplicate_anatomy_relationship.unl'
select * from tanatrel_duplicate;

delete from new_anatomy_relationship
where exists (
    select * 
    from tanatrel_duplicate 
    where tar1 = n_anatrel_anatitem_1_zdb_id
      and tar2 = n_anatrel_anatitem_2_zdb_id);

select * from new_anatomy_relationship
where not exists ( select * from anatomy_item where anatitem_zdb_id = n_anatrel_anatitem_1_zdb_id);

select * from new_anatomy_relationship
where not exists ( select * from anatomy_item where anatitem_zdb_id = n_anatrel_anatitem_2_zdb_id);  
  
  
!echo '== load in anatomy_relationship =='
insert into anatomy_relationship (anatrel_anatitem_1_zdb_id, 
				  anatrel_anatitem_2_zdb_id,
				  anatrel_dagedit_id)
     select distinct n_anatrel_anatitem_1_zdb_id, n_anatrel_anatitem_2_zdb_id, n_anatrel_dagedit_id
     from new_anatomy_relationship ;

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



-------------------------------------------------------------------
-- Definition attribution
-- 
-- create new attributions for anatomy definitions
-------------------------------------------------------------------

!echo '=====  new_definition_attributions  ====='
create temp table new_record_attribution (
	n_recattrib_data_zdb_id 	varchar(50),
	n_recattrib_source_zdb_id 	varchar(50)
)with no log;

create temp table new_anatitem_def_attribution (
	n_recattrib_data_zdb_id 	varchar(50),
	n_recattrib_source_zdb_id 	varchar(50)
)with no log;


!echo '== load anatitem_def_attrib.unl =='
load from 'anatitem_def_attrib.unl' insert into new_record_attribution;

!echo '== remove white space =='
update new_record_attribution 
   set (n_recattrib_source_zdb_id, n_recattrib_data_zdb_id) = 
   (scrub_char(n_recattrib_source_zdb_id), scrub_char(n_recattrib_data_zdb_id));
   
   
!echo '== update obo id to zdb id =='
update new_record_attribution 
   set n_recattrib_data_zdb_id = (select anatitem_zdb_id
			                from anatomy_item
				       where n_recattrib_data_zdb_id = anatitem_obo_id)
 where n_recattrib_data_zdb_id in (select anatitem_obo_id
					 from anatomy_item);


!echo '==  remove existing records =='
delete from new_record_attribution where exists ( select *
                                          from record_attribution
                                          where recattrib_data_zdb_id = n_recattrib_data_zdb_id
                                            and recattrib_source_zdb_id = n_recattrib_source_zdb_id
                                            and recattrib_source_type = "anatomy definition");


!echo '== unload if obo_id not found in zfin =='
 unload to "obo_id_not_found.rpt"
   select *
     from new_record_attribution 
    where n_recattrib_data_zdb_id not like "ZDB%";

-- '== delete record if obo_id not found in zfin =='
delete from new_record_attribution where n_recattrib_data_zdb_id not like "ZDB%";


!echo '== unload if PUB id not found in zfin =='
 unload to "pub_id_not_found.rpt"
   select *
     from new_record_attribution 
    where not exists (select * from publication where zdb_id = n_recattrib_source_zdb_id);

-- '== delete record if obo_id not found in zfin =='
delete from new_record_attribution 
    where not exists (select * from publication where zdb_id = n_recattrib_source_zdb_id);


-- '== remove duplicates by copying distinct =='
insert into new_anatitem_def_attribution (
    n_recattrib_data_zdb_id,
    n_recattrib_source_zdb_id)
select distinct
    n_recattrib_data_zdb_id,
    n_recattrib_source_zdb_id
from new_record_attribution;


!echo '==  create new records =='
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id,
    recattrib_source_type)
select 
    n_recattrib_data_zdb_id,
    n_recattrib_source_zdb_id,
    "anatomy definition"
from new_anatitem_def_attribution;



!echo "============================================================"
!echo "====   Verify XPAT annotation with the new AO           ===="
!echo "============================================================"
{
unload to "annotationViolates.err"
	    select xpatex_source_zdb_id, 
		   s1.stg_abbrev, 
                   s2.stg_abbrev, 
		   anatitem_name, 
		   s3.stg_abbrev, 
		   s4.stg_abbrev
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

}


--rollback work;

--if no error from the screen, numbers looks right, and except 
--annotationViolates.err the other three .err files are with zero length,
--then send content in annotationViolates.err to Ceri and Melissa, and 
--ask for a file AO_translation.unl from one of them with file format
-- ZDB-ANAT-XXXX|ZDB-STAGE-XXXX|ZDB-STAGE-XXXX|ZDB-ANAT-XXXX

commit work;
 
