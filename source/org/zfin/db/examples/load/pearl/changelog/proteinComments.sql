begin work;

create temp table tmp_load (feature_id varchar(50),
       	    	  	    dna_change varchar(15),
			    dna_start int8,
			    dna_end int8,
			    ref_seq varchar(50),
			    loc_within_gene varchar(50),
			    consequence_transcript varchar(100),
			    empty varchar(10),
			    aa_change_old varchar(50),
			    aa_change_new varchar(50),
			    --changed position to varchar(10) from int b/c "143-201",
			    aa_position_ignore varchar(10),
			    aa_start int8,
			    aa_end int8,
			    reference_seq_prot varchar(30),
			    aa_bp_removed int8,
			    protein_consequence varchar(50),
			    chr_assembly varchar(30),
			    ref varchar(30),
			    loc_so_term varchar(20), -- exon,intron,5'UTR,
			    exon_intron_number int8,
			    minus_dna_change int8,
			    plus_dna_change int8)
with no log;

create temp table tmp_load_removed (id varchar(50))
with no log;

load from messedUp.txt
 insert into tmp_load_removed;

load from feature_comments_all.txt
 insert into tmp_load;

update tmp_load
 set aa_change_old = null
 where aa_change_old = '';

update tmp_load
 set aa_change_new = null
 where aa_change_new = '';

delete from tmp_load
 where exists (Select 'x' from tmp_load_removed
       	      	      where id = feature_id);

delete from tmp_load
 where feature_id is null 
 or feature_id = '';

update tmp_load
 set aa_start = null
 where aa_start = '';


update tmp_load
 set aa_end = null
 where aa_end = '';

delete from tmp_load
 where not exists (Select 'x' from feature
       	   	  	  where feature_zdb_id = feature_id);

select count(*) from tmp_load 
where not exists (Select  'x' from external_note
       	   	  	  where feature_id = extnote_data_zdb_id);

delete from tmp_load
 where not exists (Select 'x' from external_note
       	   	  	  where feature_id = extnote_data_zdb_id);

select first 2 * from tmp_load
 where aa_start is not null;

update tmp_load
 set feature_id = (replace(feature_id,' ',''));

!echo "protein change";
----
---- PROTEIN CHANGE, CONSEQUENCE, POSITION ----
----

update tmp_load 
 set aa_change_new = null
 where aa_change_new = 'X' or lower(aa_change_new) = 'stop';
update tmp_load 
 set aa_change_old = null
 where aa_change_old = 'X' or lower(aa_change_new) = 'stop';

update tmp_load 
 set aa_change_new = trim(aa_change_new);
update tmp_load 
 set aa_change_old = trim(aa_change_old);


select * from tmp_load
where feature_id = 'ZDB-ALT-091124-1';


create temp table tmp_protein_change (id varchar(50), feature_id varchar(50), 
  	    	  		     aa_change_old varchar(50), 
				     aa_change_new varchar(50),
				     protein_so_term varchar(50),
				     aa_start int8,
				     aa_end int8,
				     aa_bp_removed int8
)
with no log;


insert into tmp_protein_change (feature_id)
 select distinct trim(feature_id) 
  from tmp_load
;


update tmp_protein_change
  set aa_change_old = (select distinct aa_change_old
      		      	      from tmp_load
			      where tmp_load.feature_id = tmp_protein_change.feature_id
			      and aa_change_old is not null);



update tmp_protein_change
  set aa_change_new = (select distinct aa_change_new
      		      	      from tmp_load
			      where tmp_load.feature_id = tmp_protein_change.feature_id
			      and aa_change_new is not null);

select * from tmp_protein_change
where feature_id = 'ZDB-ALT-091124-1';

select distinct aa_bp_removed, feature_id
 from tmp_load
into temp tmp_r;

select count(*), feature_id
 from tmp_r
group by feature_id
having count(*)> 1;

update tmp_protein_change
  set aa_bp_removed = (Select distinct aa_bp_removed
      		      	       from tmp_load
			       where trim(tmp_protein_change.feature_id) = trim(tmp_load.feature_id)
			       )
where feature_id != 'ZDB-ALT-140116-7';


select * from tmp_protein_change
where feature_id = 'ZDB-ALT-091124-1';


update tmp_protein_change
 set aa_end = (select distinct aa_end from tmp_load
     	      		where tmp_load.feature_id = tmp_protein_change.feature_id
			and tmp_load.aa_end is not null);




update tmp_protein_change
 set aa_change_old = (Select mdcv_term_Zdb_id from mutation_detail_Controlled_vocabulary 
     		     	     where (lower(mdcv_term_abbreviation) = lower(aa_Change_old)
			     	   or lower(mdcv_term_display_name) = lower(aa_change_old))
				   )
 where exists (select 'x' from mutation_detail_Controlled_vocabulary 
     		     	     where (mdcv_term_abbreviation = aa_Change_old
			     	   or mdcv_term_display_name = aa_change_old)
				   );

update tmp_protein_change
 set aa_change_new = (Select mdcv_term_zdb_id from mutation_detail_Controlled_vocabulary 
     		     	     where (mdcv_term_abbreviation = aa_Change_new
			     	   or mdcv_term_display_name = aa_change_new))
where exists (select 'x' from mutation_detail_Controlled_vocabulary 
     		     	     where (mdcv_term_abbreviation = aa_Change_new
			     	   or mdcv_term_display_name = aa_change_new)
				   );

update tmp_protein_change
 set aa_change_new = null
 where aa_change_new not like 'ZDB-TERM%';

update tmp_protein_change
 set aa_change_old = null
 where aa_change_old not like 'ZDB-TERM%';

select feature_id, protein_consequence
 from tmp_load
 where protein_consequence is not null
into temp tmp_p;

select count(*), feature_id from tmp_p
group by feature_id
having count(*) > 1;


update tmp_protein_change
 set protein_so_term = (Select protein_consequence
     		       	       from tmp_load
			       where tmp_load.feature_id = tmp_protein_change.feature_id
			       and protein_consequence is not null)
where feature_id not in ('ZDB-ALT-160111-1',                                  
               'ZDB-ALT-141218-9');



update tmp_protein_change
 set protein_so_term = (select distinct term_zdb_id 
     		       	       from term
			         where term_name = protein_so_term
				 and term_ontology = 'sequence');

select protein_so_term
from tmp_protein_change
 where protein_so_Term not like 'ZDB-TERM%';

create temp table tmp_Start (feature_id varchar(50), aa_start int8)
 with no log;

insert into tmp_start (feature_id, aa_start)
 select distinct feature_id, aa_start
   from tmp_load where aa_start is not null ;

select count(*) as counter, feature_id
 from tmp_start
 group by feature_id having count(*) > 1
into temp tmp_start_dups;


update tmp_protein_change
 set aa_start = (select distinct aa_start from tmp_start
     	      		where tmp_start.feature_id = tmp_protein_change.feature_id
			and tmp_start.aa_start is not null)
where exists (Select 'x' from tmp_start
      	     	     where tmp_start.feature_id = tmp_protein_change.feature_id )
 and not exists (Select 'x' from tmp_start_dups
     	 		where tmp_start_dups.feature_id = tmp_protein_change.featurE_id);


update tmp_protein_change
 set id = get_id('FPMD');

--set constraints all deferred;

insert into zdb_Active_data
 select id from tmp_protein_change;

insert into feature_protein_mutation_Detail (fpmd_zdb_id, fpmd_feature_zdb_id, fpmd_wt_protein_term_zdb_id, fpmd_mutant_or_stop_protein_Term_zdb_id, fpmd_protein_consequence_term_zdb_id, fpmd_protein_position_Start, fpmd_protein_position_end, fpmd_number_amino_acids_removed)
  select id, feature_id, aa_change_old, aa_change_new, protein_so_term, aa_start, aa_end, aa_bp_removed
   from tmp_protein_change
   where not exists (Select 'x' from feature_protein_mutation_detail
   	     	    	    where fpmd_feature_zdb_id = featurE_id);

alter table feature_protein_mutation_detail
 add constraint (Foreign key (fpmd_zdb_id)
 references zdb_active_data on delete cascade 
 constraint feature_protein_mutation_detail_zdb_active_data_fk_odc);

--set constraints all immediate;


----
----
----


alter table mutation_detail_controlled_vocabulary 
  modify (mdcv_used_in varchar(100) not null constraint mdcv_used_in_not_null);

alter table mutation_detail_controlled_vocabulary 
  modify (mdcv_term_zdb_id varchar(50) not null constraint mdcv_term_zdb_id_not_null);

select * from tmp_protein_change
 where feature_id = 'ZDB-ALT-980203-1248';

commit work;
--rollback work;
