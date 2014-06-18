-- this file loads the subset information found in the obo file
-- subsets that are new are added
-- subsets that are not found in the obo file are deleted
-- subset data are injected through subsetdefs_header.unl parameter (java) or unload file (from command line)
-- uses one temp table: tmp_subset


!echo "load term_subset";

create temp table tmp_subset (id varchar(30), subset_name varchar(40), subset_def varchar(100), subset varchar(10))
with no log;

load from subsetdefs_header.unl
  insert into tmp_subset;

!echo "all subset records from obo file";
!echo "temp table tmp_subset";
!echo "id, subset_name, subset_def, subset";
 select * from tmp_subset;

select default_namespace from tmp_header;

!echo "Number of matching subsets found in production";

Select count(*) from ontology_subset, ontology, tmp_header, tmp_subset
		       where default_namespace = ont_default_namespace
		       and osubset_subset_name = subset_name
		       and ont_pk_id = osubset_ont_id;

!echo "Remove rows in tmp_subset"
select * from tmp_subset
  where exists (Select 'x' from ontology_subset, ontology, tmp_header
  	       	       where osubset_subset_name = subset_name
		       and osubset_subset_definition =subset_def
		       and default_namespace = ont_default_namespace
		       and ont_pk_id = osubset_ont_id);

delete from tmp_subset
  where exists (Select 'x' from ontology_subset, ontology, tmp_header
  	       	       where osubset_subset_name = subset_name
		       and osubset_subset_definition =subset_def
		       and default_namespace = ont_default_namespace
		       and ont_pk_id = osubset_ont_id);

!echo "records in tmp_subset after deletion.";

unload to 'debug'
  select count(*) from tmp_subset;

!echo "Remaining rows in tmp_subset"
  select * from tmp_subset;

update ontology_subset
   set osubset_subset_definition = (select subset_def
       				   	   from tmp_subset
					   where subset_name = osubset_subset_name)
   where exists (Select 'x' from tmp_subset
   	 		where subset_name =osubset_subset_name
			and subset_def != osubset_subset_definition);

insert into ontology_subset(osubset_subset_name, osubset_subset_definition, osubset_ont_id)
  select distinct subset_name, subset_def, ont_pk_id
    from tmp_subset, ontology, tmp_header
   where not exists (Select 'x' from ontology_subset
   	     	    	    where osubset_subset_name = subset_name
			    and subset_def = osubset_subset_definition
		        and osubset_ont_id = ont_pk_id)
   and 	default_namespace = ont_default_namespace;

create temp table tmp_term_subset (term_id varchar(40), subset_name varchar(40), subset varchar(10))
 with no log;

load from term_subset.unl
  insert into tmp_term_subset;

create temp table tmp_full_term_subset (full_term_zdb_id varchar(40), term_id varchar(40), subset_name varchar(40), subset varchar(10))
 with no log;

insert into tmp_full_term_subset (full_term_zdb_id, term_id, subset_name, subset)
select
(select term_zdb_id from term where term_ont_Id = term_id), term_id, subset_name, subset from tmp_term_subset;

create index ftzdb_id_index on tmp_full_term_subset (full_term_zdb_id)
  using btree in idxdbs3;

unload to debug
    select count(*) from tmp_full_term_subset;

!echo "delete from term_subset";

select count(*) from term_subset
  where not exists (Select 'x' from tmp_full_term_subset, ontology_subset, ontology, tmp_header
  	    	   	   where termsub_subset_id = osubset_pk_id
			   and termsub_term_zdb_id = full_term_zdb_id
			   and subset_name = osubset_subset_name
			   and default_namespace = ont_default_namespace
    			   and ont_pk_id = osubset_ont_id)
  and exists (Select 'x' from term as t, ontology, ontology_subset, tmp_header
                           where termsub_term_zdb_id = t.term_zdb_id and
                                 ont_ontology_name = default_namespace and
                                 osubset_ont_id = ont_pk_id and
                                 termsub_subset_id = osubset_pk_id);

-- remove all subset records that are not found in the obo file.

delete from term_subset
  where not exists (Select 'x' from tmp_full_term_subset, ontology_subset, ontology, tmp_header
  	    	   	   where termsub_subset_id = osubset_pk_id
			   and termsub_term_zdb_id = full_term_zdb_id
			   and subset_name = osubset_subset_name
			   and default_namespace = ont_default_namespace
    			   and ont_pk_id = osubset_ont_id)
  and exists (Select 'x' from term as t, ontology, ontology_subset, tmp_header
                           where termsub_term_zdb_id = t.term_zdb_id and
                                 ont_ontology_name = default_namespace and
                                 osubset_ont_id = ont_pk_id and
                                 termsub_subset_id = osubset_pk_id);

-- select all term subset records that are about to be inserted.

unload to new_subset
  select distinct term_zdb_id, osubset_pk_id
    from term, tmp_term_subset, ontology_Subset, ontology,tmp_header
    where term_ont_id = term_id
    and osubset_subset_name = subset_name
    and default_namespace = ont_default_namespace
    and ont_pk_id = osubset_ont_id
    and not exists (Select 'x' from term_subset
                           where termsub_term_zdb_id = term_zdb_id
                           and termsub_subset_id = osubset_pk_id );

-- insert new subset records only found in the obo file

insert into term_subset (termsub_term_zdb_id, termsub_subset_id)
  select distinct term_zdb_id, osubset_pk_id
    from term, tmp_term_subset, ontology_Subset, ontology, tmp_header
    where term_ont_id = term_id
    and osubset_subset_name = subset_name
    and default_namespace = ont_default_namespace
    and ont_pk_id = osubset_ont_id
    and not exists (Select 'x' from term_subset
                           where termsub_term_zdb_id = term_zdb_id
                           and termsub_subset_id = osubset_pk_id );

