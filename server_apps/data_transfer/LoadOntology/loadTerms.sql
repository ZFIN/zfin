begin work;

create temp table tmp_header (format_ver varchar(10), data_ver varchar(10), datet varchar(20), saved_by varchar(10), auto varchar(50), default_namespace varchar(30), remark varchar(100))
with no log;

load from ontology_header.unl
  insert into tmp_header;

select * from tmp_header;

create temp table tmp_syndef (namespace varchar(30), type varchar(30), def varchar(100), scoper varchar(30), syntypedefs varchar(20))
with no log;

load from syntypedefs_header.unl
  insert into tmp_syndef;

update tmp_syndef
  set scoper = trim(scoper);

--update tmp_syndef
--  set scoper = type
--  where scoper is null;

unload to debug
    select * from tmp_syndef;

delete from tmp_syndef
  where exists (select 'x' from alias_group
  	       	       where aliasgrp_name = type
		       and aliasgrp_definition = def);

--select * from tmp_syndef
--  where not exists (Select 'x' from alias_scope
--  	    	   	   where aliasscope_scope = scoper);

!echo "alias_scope new addition.";
unload to new_scopes.unl
  select distinct scoper from tmp_syndef
  where not exists (Select 'x' from alias_scope
  	    	   	   where aliasscope_scope = scoper)
  and scoper is not null;

-- update alias definition if it has changed.

update alias_group
  set aliasgrp_definition  = (select def from tmp_syndef
      			     	     where type = aliasgrp_name)
  where aliasgrp_definition != (select def from tmp_syndef
      			     	     where type = aliasgrp_name);

!echo "alias_group new group addition.";
unload to new_aliases.unl
   select trim(type) as aliasType,
   	  (select max(aliasgrp_significance)+1 from alias_group) as aliasGroupSignificance,
   	  trim(def) as definition
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

--update statistics high for table tmp_term_onto_no_dups ;


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
			 term_is_obsolete	boolean default 'f',
			 term_ontology_id  int8
	)
with no log;

insert into tmp_term (
			term_id,
			term_name,
			term_ontology,
			term_is_obsolete,
			term_definition,
			term_comment,
			term_ontology_id)
	select term_id,
	term_name,
	term_onto,
	term_is_obsolete,
	term_definition,
	term_comment,
	(select ont_pk_id from ontology, tmp_header where ont_ontology_name = default_namespace)
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

--update statistics high for table tmp_term;

-- filter out term records that need to be update because
-- the name has changed

create temp table tmp_term_name_changed 	(
			 term_id		varchar(50),
			 term_name		varchar(255)
	)
with no log;

--unload to debug
--  select * from tmp_term_onto_no_dups;

insert into tmp_term_name_changed 
  select
  	no_dups.term_id,
  	no_dups.term_name
  from tmp_term_onto_no_dups no_dups, TERM term
  where 
  	term.term_ont_id = no_dups.term_id AND
  	term.term_name != no_dups.term_name AND
        term.term_is_Secondary = 'f';

unload to modified_term_names.unl
  select newTerm.term_name, oldTerm.term_name, oldTerm.term_ont_id 
  from tmp_term_name_changed newTerm, TERM oldTerm;

unload to debug
  select term_name, term_id from tmp_term_name_changed;

unload to debug
  select a.term_name, term.term_name, term.term_ont_id
  from tmp_term_name_changed a, term term
		   where term.term_ont_id = a.term_id;

update term
  set term_name = (select a.term_name
   		   from tmp_term_name_changed a
		   where term_ont_id = a.term_id)
where exists (select 'x'
		from tmp_term_name_changed a
		where term_ont_id = term_id);

-- filter out term records with modified definitions
create temp table tmp_term_definition_changed 	(
			 term_definition		lvarchar,
			 term_definition_old		lvarchar,
			 term_id		varchar(50)
	)
with no log;

insert into tmp_term_definition_changed 
  select
  	no_dups.term_definition,
  	term.term_definition,
  	no_dups.term_id
  from tmp_term_onto_no_dups no_dups, TERM term
  where 
  	term.term_ont_id = no_dups.term_id AND
  	term.term_definition != no_dups.term_definition AND
        term.term_is_Secondary = 'f';

unload to modified_term_definitions.unl
  select * from tmp_term_definition_changed;

update term
  set term_definition = (select a.term_definition
   		   from tmp_term_definition_changed a
		   where term_ont_id = a.term_id)
where exists (select 'x'
		from tmp_term_definition_changed a
		where term_ont_id = term_id);


-- filter out term records with modified comments
create temp table tmp_term_comment_changed 	(
			 term_comment		lvarchar,
			 term_comment_old		lvarchar,
			 term_id		varchar(50)
	)
with no log;

insert into tmp_term_comment_changed 
  select
  	no_dups.term_comment,
  	term.term_comment,
  	no_dups.term_id
  from tmp_term_onto_no_dups no_dups, TERM term
  where 
  	term.term_ont_id = no_dups.term_id AND
  	term.term_comment != no_dups.term_comment AND
        term.term_is_Secondary = 'f';

--unload to debug
--  select term_comment, term_id from tmp_term_comment_changed;

--unload to modified_term_comments.unl
--  select newTerm.term_comment, oldTerm.term_comment, oldTerm.term_ont_id
--  from tmp_term_comment_changed newTerm, TERM oldTerm;

update term
  set term_comment = (select a.term_comment
   		   from tmp_term_comment_changed a
		   where term_ont_id = a.term_id)
where exists (select 'x'
		from tmp_term_comment_changed a
		where term_ont_id = term_id);


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
-- Needs to be refactored to only create new ontologies from a default namespace.
--  select distinct term_ontology,
--  	 	  (select max(ont_order)+1 from ontology),
--		  format_ver,
--		  default_namespace,
--		  data_ver,
--		  saved_by,
--		  remark
--  from tmp_term, tmp_header
--where not exists (Select 'x' from ontology where ont_ontology_name = term_ontology);

--insert into ontology (ont_ontology_name,
--       	    	     ont_order,
--		     ont_format_version,
--		     ont_default_namespace,
--		     ont_data_version,
--		     ont_saved_by,
--		     ont_remark)
-- select distinct term_ontology,
--  	 	  (select max(ont_order)+1 from ontology),
--		  format_ver,
--		  default_namespace,
--		  data_ver,
--		  saved_by,
--		  remark
--   from tmp_term, tmp_header
--  where not exists (Select 'x' from ontology where ont_ontology_name = term_ontology);


--unload to debug
-- select * from tmp_term;

-- remove terms that already exist in TERM table
-- and keep only the new terms.
delete from tmp_term
  where exists (select 'x' from term where term_ont_id = term_id);

!echo "number of records in tmp_term before removing existing terms" ;

-- unload to debug
-- select * from tmp_term;

update tmp_term set term_zdb_id =  get_id('TERM');

!echo "number of records in tmp_term after  removing existing terms" ;

unload to debug
  select * from tmp_term;

!echo "insert the new term_ids into zdb_active_data" ;

-- insert new ids into zdb_active_data
insert into zdb_active_data
  select term_Zdb_id
    from tmp_term
    where not exists (Select 'x'
			from zdb_active_data
			where zactvd_zdb_id = term_zdb_id);

insert into term (term_zdb_id,
			term_ont_id,
			term_name,
			term_ontology,
			term_is_obsolete,
			term_comment,
			term_definition,
			term_ontology_Id)
  select term_zdb_id,
	term_id,
	term_name,
	term_ontology,
	term_is_obsolete,
	term_comment,
	term_definition,
	term_ontology_id
    from tmp_term ;

--update statistics high for table term ;

create temp table tmp_obsoletes (term_id varchar(50))
with no log ;

load from term_obsolete.unl
  insert into tmp_obsoletes ;

!echo "Number of total obsoletes in obo file";

unload to debug
    select * from tmp_obsoletes;


-- set all other terms back to obsolete = 'f'

-- obsoletes from term table
create temp table tmp_term_obsoletes (term_id varchar(50))
with no log ;

insert into tmp_term_obsoletes (term_id)
  select term.term_ont_id from term as term , tmp_term_onto_no_dups as t where 
    term.term_ont_id = t.term_id 
    and term.term_is_obsolete = 't'
    and t.term_is_obsolete = 'f';
    	     
unload to debug
    select * from tmp_term_obsoletes;

update term set term_is_obsolete = 'f'
  where exists (select 'x' from tmp_term_obsoletes as obs
                where obs.term_id = term_ont_id);

unload to 'terms_un_obsoleted.txt'
 select t.* from term as t, tmp_term_obsoletes as obs
  where obs.term_id = t.term_ont_id;

unload to debug
 select * from tmp_obsoletes as obsolete
  where exists ( select 'x'
		from term term
		where term.term_ont_id = obsolete.term_id
		      AND term.term_is_obsolete = 't');

delete from tmp_obsoletes 
  where exists ( select 'x'
		from term term
		where term.term_ont_id = term_id
		      AND term.term_is_obsolete = 't');

!echo "Terms that were obsoleted: ";

unload to debug
    select * from tmp_obsoletes;

update term
  set term_is_obsolete = 't'
  where exists (select 'x'
			from tmp_obsoletes
			where term_ont_id = term_id);

!echo "load term replacements";

create temp table tmp_replaced (replaced_id varchar(50), term_id varchar(50), termrep varchar(20))
with no log;

load from term_replaced.unl
  insert into tmp_replaced;

unload to debug
    select * from tmp_replaced;

unload to debug
 select * from obsolete_term_replacement
  where exists (Select 'x' from term, tmp_term_onto_with_dups
  	       	       where term_ont_id = term_id
		       and term_zdb_id = obstermrep_term_zdb_id);

delete from obsolete_term_replacement
  where exists (Select 'x' from term, tmp_term_onto_with_dups
  	       	       where term_ont_id = term_id
		       and term_zdb_id = obstermrep_term_zdb_id);

unload to debug
  select * from obsolete_term_replacement;

insert into obsolete_term_replacement (obstermrep_term_zdb_id, obstermrep_term_replacement_zdb_id)
  select a.term_zdb_id, b.term_zdb_id
    from term a, term b, tmp_replaced
    where a.term_ont_id = term_id
    and b.term_ont_id = replaced_id;

unload to debug
  select * from obsolete_term_replacement;

!echo "LOAD SUGGESTIONS aka consider";

create temp table tmp_consider (term_id varchar(50), replaced_id varchar(50), termrep varchar(20))
with no log;

load from term_consider.unl
  insert into tmp_consider;

unload to debug
    select * from tmp_consider;

unload to debug
 select * from obsolete_term_suggestion
  where exists (Select 'x' from term, tmp_term_onto_with_dups
  	       	       where term_ont_id = term_id
		       and term_zdb_id = obstermsug_term_zdb_id);

delete from obsolete_term_suggestion
  where exists (Select 'x' from term, tmp_term_onto_with_dups
  	       	       where term_Zdb_id = obstermsug_term_zdb_id
		       and term_ont_id = term_id);

insert into obsolete_term_suggestion (obstermsug_term_zdb_id, obstermsug_term_suggestion_zdb_id)
  select a.term_zdb_id, b.term_zdb_id
    from term a, term b, tmp_suggestion
    where a.term_ont_id = id
    and b.term_ont_id = suggested_id;

--rollback work;
commit work;
