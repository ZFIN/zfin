create temp table tmp_syns (term_id varchar(30),synonym varchar(255),scoper varchar(30),type varchar(30), syn varchar(30))
with no log;

!echo "start of the synonym loading";
load from term_synonyms.unl
  insert into tmp_syns;

--GO:0001525|blood vessel formation from pre-existing blood vessels|EXACT|systematic_synonym|synonym|
--GO:0001527|extended fibrils|EXACT|[]|synonym|

update tmp_syns
  set type = null
  where trim(type) like "[%";

update tmp_syns
  set type = 'alias'
  where type is null ;

create index tmp_syn_synonym_index
  on tmp_syns(synonym)
  using btree in idxdbs3;

--update statistics high for table tmp_syns;
--update statistics high for table data_alias;
--update statistics high for table term;
--update statistics high for table tmp_term_onto_no_dups;

insert into alias_scope (aliasscope_scope)
  select distinct scoper
  	 from tmp_syns
  	 where not  exists (Select 'x'
	       	    	   	   from alias_scope
				   where aliasscope_scope = scoper);

!echo "tmp_syn_delete";

update tmp_syns
  set synonym = trim(synonym);

update data_alias
  set dalias_alias = trim(Dalias_alias)
 where length(dalias_alias) != octet_length(dalias_alias)
 and dalias_data_zdb_id like 'ZDB-TERM%';

delete from tmp_syns
  where exists (Select 'x' from data_alias,
  	       	       term,
		       tmp_term_onto_no_dups,
  	       	       alias_group
  	       	       where term_zdb_id = dalias_data_zdb_id
		       and term_ont_id = term_id
		       and dalias_group_id = aliasgrp_pk_id
		       and dalias_alias = synonym);


--update data_alias
--  set dalias_group_id = (select aliasgrp_pk_id
--      		      	    from tmp_syns,
--      		      		term,
--				alias_group
--      		    	    where term_ont_id = term_id
--			    and aliasgrp_name = type
--			    and dalias_data_zdb_id = term_zdb_id)
--  where exists (Select 'x' from tmp_syns, term
--      		    	    where term_ont_id = term_id
--			    and dalias_alias = synonym
--			    and dalias_data_zdb_id = term_zdb_id);


create temp table tmp_syns_with_ids (zdb_id varchar(50), term_id varchar(30),synonym varchar(255),type varchar(30), scoper varchar(30), data_id varchar(50))
with no log;


insert into tmp_syns_with_ids (term_id, synonym, type,scoper)
  select distinct term_id, synonym, type,scoper
    from tmp_syns;

update tmp_syns_with_ids
  set type = trim(type);

update tmp_syns_with_ids
  set data_id = (Select term_zdb_id
      	      		from term
			where term_id = term_ont_id);

select count(*) from tmp_syns_with_ids where data_id is not null;

delete from tmp_syns_with_ids where data_id is null;


delete from tmp_syns_with_ids where not exists (Select 'x' from alias_group
       	    		      	    	       where aliasgrp_name = type);

delete from tmp_syns_with_ids where not exists (Select 'x' from alias_scope
       	    		      	    	       where aliasscope_scope = scoper);

select distinct type from tmp_syns_with_ids;

select distinct scoper from tmp_syns_with_ids where scoper != type;

update tmp_syns_with_ids
  set zdb_id = get_id("DALIAS");

insert into zdb_active_data
  select zdb_id from tmp_syns_with_ids;

select distinct type
  from tmp_syns_with_ids
  where not exists (Select 'x'
  	    	   	   from alias_group
			   where aliasgrp_name = type);

insert into data_alias (dalias_zdb_id, dalias_alias, dalias_group_id, dalias_scope_id, dalias_data_zdb_id)
  select zdb_id, synonym, (select aliasgrp_pk_id from alias_group where aliasgrp_name = type), (select aliasscope_pk_id from alias_scope where trim(aliasscope_scope)=scoper),data_id
    from tmp_syns_with_ids;

select count(*),dalias_data_zdb_id, dalias_group_id, dalias_alias
  from data_alias
 group by dalias_data_zdb_id, dalias_group_id, dalias_alias
 having count(*) > 1;

--!!  COMMENT THIS BACK IN WHEN XREFS are REQUESTED.  Also in the works is a new table for TERM xrefs
--!!  outside of db_link. Currently, for the GO load, this loads 1.5 million rows into db_link and adds
--!!  over 3 hours to the load.

--create temp table tmp_xrefs(id varchar(30), xref_db varchar(100),xref_id varchar(100), xref varchar(10))
-- with no log;

--load from term_xref.unl
--  insert into tmp_xrefs;

--select first 10* from tmp_xrefs;

--create temp table tmp_xrefs_with_fdbcont_dblink (id varchar(30), xref_db varchar(100),xref_id varchar(100), xref varchar(10), fdbcont_id varchar(50), dblink_id varchar(50))
-- with no log;

--select distinct xref_db from tmp_xrefs where not exists
--(Select 'x' from foreign_db
--  	    	   	   where fdb_db_name = xref_db);
--delete from tmp_xrefs
--  where not exists (Select 'x' from foreign_db
--  	    	   	   where fdb_db_name = xref_db);

--insert into tmp_xrefs_with_fdbcont_dblink (id, xref_db, xref_id, xref,fdbcont_id)
--  select id, xref_db, xref_id, xref, fdbcont_Zdb_id
--    from foreign_db, tmp_Xrefs, foreign_Db_Contains
--   where fdb_db_name = xref_db;

--delete from tmp_xrefs_with_fdbcont_dblink
--  where exists (Select 'x' from db_link, term
--  	       	       where dblink_acc_num = xref_id
--		       and dblink_fdbcont_zdb_id = fdbcont_id
--		       and term_zdb_id = dblink_linked_recid
--		       and term_ont_id = id);

--delete from db_link
 --      where not exists (select 'x' from tmp_xrefs_with_fdbcont_dblink
--       	     	 		where dblink_acc_num = xref_id
--		       and dblink_fdbcont_zdb_id = fdbcont_id
--		       and term_zdb_id = dblink_linked_recid
--		       and term_ont_id = id)
--  and dblink_linked_recid like 'ZDB-TERM%'
--  and exists (select 'x' from term, tmp_term_onto_no_dups
--      	     	     where term_ont_id = term_id
--		     and term_zdb_id = dblink_linked_recid
--		     and dblink_fdbcont_zdb_id = fdbcont_id);

--PROBLEM_ID
--delete from tmp_xrefs_with_fdbcont_dblink
--  where not exists (Select 'x' from term
 -- 	    	   	   where term_ont_id = id);

--update tmp_xrefs_with_fdbcont_dblink
--  set dblink_id = get_id('DBLINK');

--insert into zdb_Active_data
--  select dblink_id from tmp_xrefs_with_fdbcont_dblink;

--insert into db_link (dblink_zdb_id, dblink_linked_recid, dblink_acc_num, dblink_fdbcont_zdb_id, dblink_acc_num_display)
--  select dblink_id, (select term_zdb_id from term where term_ont_id = id),
--  	 xref_id,fdbcont_id,xref_id
--    from tmp_xrefs_with_fdbcont_dblink;

