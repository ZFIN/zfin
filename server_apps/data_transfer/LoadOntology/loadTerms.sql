begin work;

create temp table tmp_header (format_ver varchar(10), data_ver varchar(10), datet varchar(20), saved_by varchar(10), auto varchar(50), default_namespace varchar(30), remark varchar(100))
with no log;

load from ontology_header.unl
  insert into tmp_header;

create temp table tmp_syndef (namespace varchar(30), type varchar(30), def varchar(100), scoper varchar(30), syntypedefs varchar(20))
with no log;

load from syntypedefs_header.unl
  insert into tmp_syndef;

update tmp_syndef
  set scoper = trim(scoper);

update tmp_syndef
  set scoper = type
  where scoper is null;

update tmp_syndef
  set scoper = 'exact alias'
 where scoper = 'EXACT';

update tmp_syndef
  set scoper = 'plural'
  where scoper = 'PLURAL';

update tmp_syndef
  set type = 'plural'
  where type = 'PLURAL';

select * from tmp_syndef;

delete from tmp_syndef
  where exists (select 'x' from alias_group
  	       	       where aliasgrp_name = type
		       and aliasgrp_definition = def);

--select * from tmp_syndef
--  where not exists (Select 'x' from alias_scope
--  	    	   	   where aliasscope_scope = scoper);

insert into alias_scope (aliasscope_scope)
  select distinct scoper from tmp_syndef
  where not exists (Select 'x' from alias_scope
  	    	   	   where aliasscope_scope = scoper)
  and scoper is not null;

update alias_group
  set aliasgrp_definition  = (select def from tmp_syndef
      			     	     where type = aliasgrp_name);

!echo "alias_group new group addition";
insert into alias_group (aliasgrp_name, aliasgrp_significance, aliasgrp_definition, aliasgrp_aliasscope_id)
   select trim(type), 
   	  (select max(aliasgrp_significance)+1 from alias_group),
   	  trim(def),
	  (select aliasscope_pk_id from alias_scope where aliasscope_scope = trim(scoper))
     from tmp_syndef
     where not exists (Select 'x' from alias_group 
     	       	      	      where aliasgrp_name = type);

create temp table tmp_suggestion (id varchar(30), suggested_id varchar(30), consider varchar(10))
with no log;

load from term_consider.unl 
 insert into tmp_suggestion;


create temp table tmp_term_onto_with_dups (
		term_id			varchar(50),
		term_name		varchar(255),
		term_onto		varchar(30),
		term_definition		lvarchar,
		term_comment		lvarchar,
		term_is_obsolete	boolean default 'f'
	)with no log;

--load the ontology file

load from term_parsed.unl insert into tmp_term_onto_with_dups;

update tmp_term_onto_with_dups
  set term_onto = (Select default_namespace 
      		  	  from tmp_header
			  )
where term_onto is null;

create temp table tmp_term_onto_no_dups (
		term_id			varchar(50),
		term_name		varchar(255),
		term_onto		varchar(30),
		term_definition		lvarchar,
		term_comment		lvarchar,
		term_is_obsolete	boolean default 'f'
	)with no log;

insert into tmp_term_onto_no_dups
  select distinct term_id, 
			trim(term_name), 
			term_onto, 
			trim(term_definition),
			term_comment, 
			term_is_obsolete
	from tmp_term_onto_with_dups ;


create index rterm_name_index 
  on tmp_term_onto_no_dups (term_name)
  using btree in idxdbs3 ;

create index rterm_id_index 
  on tmp_term_onto_no_dups (term_id)
  using btree in idxdbs2 ;

update statistics high for table tmp_term_onto_no_dups ;


unload to 'terms_missing_obo_id.txt'
select * from term
  where term_ont_id like 'ZDB-TERM-%' ;

create temp table tmp_term 	(
			 term_zdb_id		varchar(50),
			 term_id		varchar(50),
			 term_name		varchar(255),
			 term_ontology		varchar(30),
			 term_definition	lvarchar,
			 term_comment		lvarchar,
			 term_is_obsolete	boolean default 'f'
	)
with no log;

insert into tmp_term (term_zdb_id, 
			term_id, 
			term_name, 
			term_ontology,
			term_is_obsolete,
			term_definition,
			term_comment)
  select get_id('TERM'),
	term_id,
	term_name,
	term_onto,
	term_is_obsolete,
	term_definition,
	term_comment
    from tmp_term_onto_no_dups ;
	
unload to 'new_terms.unl'
  select term_id, term_name
    from tmp_term
	where not exists (Select 'x'
			   from term
			   where term.term_ont_id = tmp_term.term_id);


select * from tmp_term_onto_with_dups
 where term_id = 'GO:0000758' ;

select * from tmp_term_onto_no_dups
 where term_id = 'GO:0000758' ;

select * from term
 where term_ont_id = 'GO:0000758' ;


unload to 'updated_terms.unl'
  select n.term_name, g.term_name, g.term_ont_id 
    from tmp_term_onto_no_dups n, term g 
    where n.term_id = g.term_ont_id 
    and n.term_name not like g.term_name;

!echo "update the term table with new names where the term id is the same term id in the obo file" ;

update statistics high for table tmp_term; 

update term
  set term_name = (select a.term_name 
			from tmp_term_onto_no_dups a
			where term_ont_id = term_id)
  where exists (select 'x'
			from tmp_term_onto_no_dups a
			where term_ont_id = term_id)
  and term_is_obsolete = 'f'
 and term_is_Secondary = 'f';


--select count(*) from term 
-- where exists (select a.term_definition 
--			from tmp_term_onto_no_dups a
--			where term_ont_id = term_id);

--select first 5* from tmp_term_onto_no_dups;

update term
  set term_definition = (select a.term_definition 
			from tmp_term_onto_no_dups a
			where term_ont_id = term_id
			)
  where exists (select 'x'
			from tmp_term_onto_no_dups a
			where term_ont_id = term_id)
  and term_is_obsolete = 'f'
 and term_is_secondary = 'f';


!echo "insert the new term_ids into zdb_active_data" ;

delete from tmp_term
  where exists (select 'x' from term where term_ont_id = term_id);

insert into zdb_active_data 
  select term_Zdb_id
    from tmp_term 
    where not exists (Select 'x'
			from zdb_active_data
			where zactvd_zdb_id = term_zdb_id);

--!echo "insert the new terms into the term table, obsolete and all" ;

update tmp_term
  set term_is_obsolete = 'f'
  where term_is_obsolete is null ; 

update ontology
  set ont_format_version = (Select format_ver from tmp_header where default_namespace = ont_default_namespace)
  where exists (Select 'x' from tmp_header where ont_default_namespace = default_namespace);

update ontology
  set ont_data_version = (Select data_ver from tmp_header where default_namespace = ont_default_namespace)
  where exists (Select 'x' from tmp_header where ont_default_namespace = default_namespace);

update ontology
  set ont_current_date = (Select datet from tmp_header where default_namespace = ont_default_namespace)
  where exists (Select 'x' from tmp_header where ont_default_namespace = default_namespace);

update ontology
  set ont_saved_by = (Select saved_by from tmp_header where default_namespace = ont_default_namespace)
  where exists (Select 'x' from tmp_header where ont_default_namespace = default_namespace);

update ontology
  set ont_import = (Select auto from tmp_header where default_namespace = ont_default_namespace)
  where exists (Select 'x' from tmp_header where ont_default_namespace = default_namespace);

update ontology
  set ont_remark = (Select remark from tmp_header where default_namespace = ont_default_namespace)
  where exists (Select 'x' from tmp_header where ont_default_namespace = default_namespace);


!echo "this is where new ontologies are born.";
insert into ontology (ont_ontology_name, 
       	    	     ont_order, 
		     ont_format_version, 
		     ont_default_namespace, 
		     ont_data_version, 
		     ont_saved_by, 
		     ont_remark)
  select distinct term_ontology, 
  	 	  (select max(ont_order)+1 from ontology),
		  format_ver, 
		  default_namespace, 
		  data_ver,
		  saved_by, 
		  remark
   from tmp_term, tmp_header
  where not exists (Select 'x' from ontology where ont_ontology_name = term_ontology);

insert into term (term_zdb_id, 
			term_ont_id, 
			term_name, 
			term_ontology,
			term_is_obsolete,
			term_comment,
			term_definition)
  select term_zdb_id,
	term_id,
	term_name,
	term_ontology,
	term_is_obsolete,
	term_comment,
	term_definition
    from tmp_term ;

update statistics high for table term ;

create temp table tmp_obsoletes (term_id varchar(50))
with no log ;

load from term_obsolete.unl
  insert into tmp_obsoletes ;

update term
  set term_is_obsolete = 't'
  where exists (select 'x'
			from tmp_obsoletes
			where term_ont_id = term_id);

--secondary terms.


create temp table sec_dups 
  (
    prim_id varchar(50),
    sec_id varchar(50)
  );

load from term_secondary.unl
  insert into sec_dups;

create temp table sec_oks
  (
    prim_id varchar(50),
    sec_id varchar(50), 
    prim_zdb_id varchar(50), 
    sec_zdb_id varchar(50)
  );

--insert only the distinct secondary terms

insert into sec_oks (sec_id, prim_id)
  select distinct sec_id, prim_id
    from sec_dups ;

update sec_oks
  set prim_zdb_id = (select term_zdb_id 
      		       from term
		       where term_ont_id = prim_id);

update sec_oks
  set sec_zdb_id = (select term_zdb_id 
      		       from term
		       where term_ont_id = sec_id);

create temp table sec_unload 
  (
    prim_id varchar(50),
    sec_id varchar(50)
  );

--update the secondary terms in ZFIN

!echo "here is the secondary update for  terms" ;

update term
  set term_is_secondary = 't'
  where exists (Select 'x'
		  from sec_oks
		  where term_ont_id = sec_id) ;

unload to term_no_longer_secondary.txt
  select term_name, term_ont_id, term_zdb_id
    from term
    where term_is_secondary = 't'
    and not exists (Select 'x'
		  from sec_oks
		  where term_ont_id = sec_id) 
    and exists (Select 'x' from tmp_term_onto_with_dups
    	       	       where term_id = term_ont_id
		       and term_ontology = term_onto);

--set these back to primary for now

update term
  set term_is_secondary = 'f'
  where not exists (Select 'x'
		  from sec_oks
		  where term_ont_id = sec_id) 
  and term_is_secondary = 't';

!echo "now deal with relationships" ;

create temp table tmp_rels (
	termrel_term_1_id varchar(50),
	termrel_term_2_id varchar(50),
	termrel_type varchar(100)
 ) with no log ;


load from term_relationships.unl
  insert into tmp_rels ;

select distinct termrel_type from tmp_rels;

insert into term_relationship_type (termreltype_name)
  select distinct termrel_type from tmp_rels
  	 where not exists (Select 'x' from term_relationship_type
	       	   	  	  where termrel_type = termreltype_name);

create temp table tmp_rels_zdb (
	ttermrel_term_1_zdb_id varchar(50),
	ttermrel_term_2_zdb_id varchar(50),
	ttermrel_ont_id_1 varchar(50),
	ttermrel_ont_id_2 varchar(50),
	ttermrel_type varchar(100),
	ttermrel_ontology varchar(30)
 ) with no log ;

insert into tmp_rels_zdb (ttermrel_ont_id_1, ttermrel_ont_id_2, ttermrel_type)
  select termrel_term_1_id, termrel_term_2_id, termrel_type
   from tmp_rels;


create index rtermrels_term_1_id_index
  on tmp_rels_zdb (ttermrel_term_1_zdb_id)
  using btree in idxdbs1 ;

create index rtermrels_term_2_id_index
  on tmp_rels_zdb (ttermrel_term_2_zdb_id)
  using btree in idxdbs2 ;

update tmp_rels_zdb
  set ttermrel_term_1_zdb_id = (Select term_Zdb_id from term
      			      	      where term_ont_id = ttermrel_ont_id_1);

update tmp_rels_zdb
  set ttermrel_term_2_zdb_id = (Select term_Zdb_id from term
      			      	      where term_ont_id = ttermrel_ont_id_2);

update statistics high for table tmp_rels ;
update statistics high for table term ;

delete from tmp_rels_zdb 
 where exists (Select 'x' from term_relationship a
       	      	      where ttermrel_term_1_zdb_id = a.termrel_term_1_zdb_id
		      and ttermrel_term_2_zdb_id = a.termrel_term_2_zdb_id
		      and ttermrel_type = a.termrel_type);


create temp table tmp_zfin_rels  (
	termrel_zdb_id varchar(50),
	termrel_term_1_zdb_id varchar(50),
	termrel_term_2_zdb_id varchar(50),
	termrel_type varchar(100)
) with no log ;


insert into tmp_zfin_rels(
	termrel_term_1_zdb_id,
	termrel_term_2_zdb_id,
	termrel_type)
  select distinct
  	ttermrel_term_1_zdb_id,
	ttermrel_term_2_zdb_id,
	ttermrel_type
	from tmp_rels_zdb ;

update tmp_zfin_rels
  set termrel_zdb_id = get_id("TERMREL");


create index tmp_rel_1_index 
  on tmp_zfin_rels (termrel_term_1_zdb_id)
  using btree in idxdbs2;

create index tmp_rel_2_index 
  on tmp_zfin_rels (termrel_term_2_zdb_id)
  using btree in idxdbs2;

create index tmp_reltype_index_zfin_rels 
  on tmp_zfin_rels (termrel_type)
  using btree in idxdbs2;


create index tmp_rels_1_index 
  on tmp_rels (termrel_term_1_id)
  using btree in idxdbs3;

create index tmp_rels_2_index 
  on tmp_rels (termrel_term_2_id)
  using btree in idxdbs3;

create index tmp_reltype_index_rels
  on tmp_rels (termrel_type)
  using btree in idxdbs3;


update statistics high for table zdb_active_data;
update statistics high for table tmp_zfin_rels ;
update statistics high for table tmp_rels_zdb;
update statistics high for table tmp_rels;

!echo "add any new term relationship types" ;

insert into term_relationship_type 
  select distinct termrel_type
		from tmp_zfin_rels
		where not exists (Select 'x'
					from term_relationship_type
					where termreltype_name = termrel_type); 

!echo "term relationships with null term_2s?";

delete from tmp_zfin_rels
  where termrel_term_2_zdb_id is null;

insert into zdb_active_data
  select termrel_zdb_id
    from tmp_zfin_rels 
	where not exists (select 'x'
				from zdb_active_data
				where zactvd_zdb_id = termrel_zdb_id);



insert into term_relationship (termrel_zdb_id,
    				termrel_term_1_zdb_id,
    				termrel_term_2_zdb_id,
    				termrel_type)
  select termrel_zdb_id,
	termrel_term_1_zdb_id,
	termrel_term_2_zdb_id,
	termrel_type
    from tmp_zfin_rels ;

update statistics high for table term_relationship ;

--!!! NOT OBVIOUS logic: if the second term in the relationship belongs to this ontology load, then it is
--!!! safe to check for deletions. Don't want to delete other load relationships.

!echo "delete from term relationship";
delete from term_relationship
 where not exists (Select 'x' from term a, term b, tmp_rels
       	   	  	  where a.term_ont_id = termrel_term_1_id
			  and b.term_ont_id = termrel_term_2_id
			  and termrel_term_1_Zdb_id = a.term_zdb_id
			  and termrel_term_2_zdb_id = b.term_zdb_id
			  and term_relationship.termrel_type = tmp_rels.termrel_type)
 and exists (select 'x' from tmp_term_onto_no_dups, term
     	    	    	where term_id = term_ont_id
			and term_zdb_id = termrel_term_2_zdb_id);


delete from term_relationship
 where not exists (Select 'x' from term a, term b, tmp_rels
       	   	  	  where a.term_ont_id = termrel_term_1_id
			  and b.term_ont_id = termrel_term_2_id
			  and termrel_term_1_Zdb_id = a.term_zdb_id
			  and termrel_term_2_zdb_id = b.term_zdb_id
			  and term_relationship.termrel_type = tmp_rels.termrel_type)
 and exists (select 'x' from tmp_term_onto_no_dups, term
     	    	    	where term_id = term_ont_id
			and term_zdb_id = termrel_term_1_zdb_id);

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
  set scoper = 'narrow alias'
  where scoper ='NARROW';

update tmp_syns
  set scoper = 'exact alias'
  where scoper = 'EXACT';

update tmp_syns
  set scoper = 'broad alias'
  where scoper = 'BROAD';

update tmp_syns
  set scoper = 'related alias'
  where scoper = 'RELATED';

update tmp_syns
  set type = scoper
  where type is null ;

create index tmp_syn_synonym_index
  on tmp_syns(synonym)
  using btree in idxdbs3;

update statistics high for table tmp_syns;
update statistics high for table data_alias;
update statistics high for table term;
update statistics high for table tmp_term_onto_no_dups;

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

select distinct type from tmp_syns_with_ids;

select distinct scoper from tmp_syns_with_ids;

update tmp_syns_with_ids
  set zdb_id = get_id("DALIAS");

insert into zdb_active_data
  select zdb_id from tmp_syns_with_ids;

select distinct type 
  from tmp_syns_with_ids
  where not exists (Select 'x' 
  	    	   	   from alias_group 
			   where aliasgrp_name = type);
			   
insert into data_alias (dalias_zdb_id, dalias_alias, dalias_group_id, dalias_data_zdb_id)
  select zdb_id, synonym, (select aliasgrp_pk_id from alias_group where trim(aliasgrp_name)=type),data_id
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


!echo "load term replacements";

create temp table tmp_replaced (replaced_id varchar(50), term_id varchar(50), termrep varchar(20))
with no log;

load from term_replaced.unl
  insert into tmp_replaced;

delete from obsolete_term_replacement
  where exists (Select 'x' from term, tmp_term_onto_with_dups
  	       	       where term_ont_id = term_id
		       and term_zdb_id = obstermrep_term_zdb_id);

select count(*) from obsolete_term_replacement;

insert into obsolete_term_replacement (obstermrep_term_zdb_id, obstermrep_term_replacement_zdb_id)
  select a.term_zdb_id, b.term_zdb_id
    from term a, term b, tmp_replaced
    where a.term_ont_id = term_id
    and b.term_ont_id = replaced_id;

!echo "LOAD SUGGESTIONS";

delete from obsolete_term_suggestion
  where exists (Select 'x' from term, tmp_term_onto_with_dups
  	       	       where term_Zdb_id = obstermsug_term_zdb_id
		       and term_ont_id = term_id);

insert into obsolete_term_suggestion (obstermsug_term_zdb_id, obstermsug_term_suggestion_zdb_id)
  select a.term_zdb_id, b.term_zdb_id
    from term a, term b, tmp_suggestion
    where a.term_ont_id = id
    and b.term_ont_id = suggested_id;


!echo "load term_subset";

create temp table tmp_subset (id varchar(30), subset_name varchar(40), subset_def varchar(100), subset varchar(10))
with no log;

load from subsetdefs_header.unl
  insert into tmp_subset;

delete from tmp_subset
  where exists (Select 'x' from ontology_subset
  	       	       where osubset_subset_name = subset_name
		       and osubset_subset_definition =subset_def);

update ontology_subset
   set osubset_subset_definition = (select subset_def 
       				   	   from tmp_subset 
					   where subset_name = osubset_subset_name)
   where exists (Select 'x' from tmp_subset
   	 		where subset_name =osubset_subset_name
			and subset_def != osubset_subset_definition); 

insert into ontology_subset(osubset_subset_name, osubset_subset_definition)
  select distinct subset_name, subset_def
    from tmp_subset
   where not exists (Select 'x' from ontology_subset
   	     	    	    where osubset_subset_name = subset_name
			    and subset_def = osubset_subset_definition);


create temp table tmp_term_subset (term_id varchar(40), subset_name varchar(40), subset varchar(10))
 with no log;

load from term_subset.unl
  insert into tmp_term_subset;

!echo "delete from term_subset";

delete from term_subset
  where not exists (Select 'x' from tmp_term_subset, term, ontology_subset
  	    	   	   where term_id = term_ont_id 
			   and termsub_subset_id = osubset_pk_id
			   and subset_name = osubset_subset_name) 
  and exists (Select 'x' from tmp_term, term
      	     	     where term_ont_id = term_id
		     );


delete from tmp_term_subset
  where exists (select 'x' from term_subset, ontology_subset, term
  	       	       where term_ont_id = term_id
		       and termsub_subset_id = osubset_pk_id
		       and subset_name = osubset_subset_name);


insert into term_subset (termsub_term_zdb_id, termsub_subset_id)
  select distinct term_zdb_id, osubset_pk_id
    from term, tmp_term_subset, ontology_Subset
    where term_ont_id = term_id
    and osubset_subset_name = subset_name;


--rollback work;
commit work;

