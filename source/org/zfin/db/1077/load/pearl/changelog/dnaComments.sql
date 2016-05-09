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
			    empty2 varchar(30),
			    protein_consequence varchar(50),
			    chr_assembly varchar(30),
			    ref varchar(30),
			    loc_so_term varchar(50), -- exon,intron,5'UTR,
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

delete from tmp_load
 where exists (Select 'x' from tmp_load_removed
       	      	      where id = feature_id);

select count(*) from tmp_load 
where not exists (Select  'x' from external_note
       	   	  	  where feature_id = extnote_data_zdb_id);

delete from tmp_load
 where not exists (Select 'x' from external_note
       	   	  	  where feature_id = extnote_data_zdb_id);

delete from tmp_load
 where feature_id is null 
 or feature_id = '';

create temp table tmp_dna_change (feature_id varchar(50),
       	    	           dna_change varchar(50),
       	    	  	   id varchar(50),
			   minus_dna_change int,
			   plus_dna_change int,
			   dna_position_Start int,
			   dna_position_end int,
			   ref_seq varchar(50),
			   exon_number int8,
			   intron_number int8,
			   loc_so_term varchar(50))
with no log;

insert into tmp_dna_change (feature_id)
 select distinct feature_id from tmp_load
 where feature_id not in ('ZDB-ALT-160317-9',                                 
               'ZDB-ALT-120120-7',                                  
               'ZDB-ALT-150417-6',                                  
               'ZDB-ALT-150429-3',                                  
               'ZDB-ALT-160111-1',                                  
               'ZDB-ALT-140623-6',                                  
               'ZDB-ALT-150416-3',                                  
               'ZDB-ALT-131122-4',                                  
               'ZDB-ALT-150325-7',                                  
               'ZDB-ALT-090520-6',                                  
               'ZDB-ALT-131122-15',                                 
               'ZDB-ALT-131122-12',                                 
               'ZDB-ALT-140116-7',                                  
               'ZDB-ALT-111110-2',                                  
               'ZDB-ALT-140116-8',                                  
               'ZDB-ALT-160314-1',                                  
               'ZDB-ALT-160314-2',                                  
               'ZDB-ALT-151230-13',                                 
               'ZDB-ALT-150909-2'
	       );

update tmp_load
 set minus_dna_change = null
 where minus_dna_change = '';

update tmp_load
 set plus_dna_change = null
 where plus_dna_change = '';

update tmp_load
 set dna_start = null
 where dna_start = '';

update tmp_load
 set dna_end = null
 where dna_end = '';

update tmp_load
 set ref_seq = null
 where ref_seq = '';


update tmp_dna_change
  set minus_dna_change = (Select distinct minus_dna_change from tmp_load
      		       	 	 where tmp_load.feature_id = tmp_dna_change.feature_id
				 and minus_dna_change is not null);


update tmp_dna_change
  set plus_dna_change = (Select distinct plus_dna_change from tmp_load
      		       	 	 where tmp_load.feature_id = tmp_dna_change.feature_id
				 and plus_dna_change is not null);

select feature_id, dna_start
 from tmp_load
into temp tmp_start;

select count(*), feature_id
 from tmp_start
where dna_start is not null
 group by feature_id
 having count(*) > 1;

select * from tmp_dna_change
 where feature_id = 'ZDB-ALT-070730-10';


update tmp_dna_change
  set dna_position_start = (Select distinct dna_Start from tmp_load
      		       	 	 where tmp_load.feature_id = tmp_dna_change.feature_id
				 and dna_end is not null
				 and tmp_load.feature_id not in ('ZDB-ALT-160111-1',                                 
               'ZDB-ALT-140623-6',                                  
               'ZDB-ALT-140411-5'));

select feature_id, dna_end
 from tmp_load
where dna_end is not null
 into temp tmp_end;

select feature_id, count(*) 
 from tmp_end
group by feature_id
 having count(*) > 1;

update tmp_dna_change
  set dna_position_end = (Select distinct dna_end from tmp_load
      		       	 	 where tmp_load.feature_id = tmp_dna_change.feature_id
				 and dna_end is not null
				 and tmp_load.feature_id not in ('ZDB-ALT-160111-1',                                  
               'ZDB-ALT-140623-6',                                  
               'ZDB-ALT-140411-5' ));

update tmp_dna_change
  set ref_seq = (Select distinct ref_seq from tmp_load
      		       	 	 where tmp_load.feature_id = tmp_dna_change.feature_id
				 and ref_seq is not null);

update tmp_dna_change
 set dna_change = (select distinct dna_change from tmp_load
     		   where tmp_load.feature_id = tmp_dna_change.feature_id
				 and dna_change is not null);
select * from tmp_dna_change
 where feature_id = 'ZDB-ALT-070730-10';


update tmp_dna_change
 set dna_change = (Select mdcv_term_Zdb_id from mutation_detail_Controlled_vocabulary
     		  	  where mdcv_term_display_name = dna_change);

update tmp_dna_change
 set exon_number = (select distinct exon_intron_number from tmp_load 
     		   	   where tmp_load.feature_id = tmp_dna_change.feature_id
			   and trim(loc_so_term) = 'exon'
			   and exon_intron_number is not null);

update tmp_dna_change
 set intron_number = (select distinct exon_intron_number from tmp_load 
     		   	   where tmp_load.feature_id = tmp_dna_change.feature_id
			   and trim(loc_so_term) = 'intron'
			   and exon_intron_number is not null);


update tmp_dna_change
 set loc_so_term = null
 where loc_so_term in ('exon','intron');

update tmp_dna_change
 set loc_so_term = (select distinct loc_so_term from tmp_load 
     		   	   where tmp_load.feature_id = tmp_dna_change.feature_id
			   and loc_so_term is not null);



update tmp_dna_change
 set id = get_id('FDMD');

insert into zdb_Active_data
 select id from tmp_dna_change;

update tmp_dna_change
 set loc_so_term = (select term_zdb_id from term
     		   	   where term_name = loc_so_term);

update tmp_dna_change
  set loc_so_term = (select mdcv_term_zdb_id from mutation_Detail_controlled_vocabulary
      		    	    where lower(mdcv_term_display_name) = lower(loc_so_term))
 where loc_so_term not like 'ZDB-TERM%';

select distinct loc_so_term
 from tmp_Dna_change
 where loc_so_term not like 'ZDB-TERM%';


insert into mutation_detail_controlled_vocabulary (mdcv_term_zdb_id, mdcv_term_display_name, mdcv_used_in)
 select  distinct loc_so_term, term_name, 'dna_mutation_term'
   from term, tmp_dna_change
   where term_Zdb_id = loc_so_term
 and not exists (select 'x' from mutation_detail_controlled_vocabulary
     	 		where mdcv_term_zdb_id = loc_so_term);

--select loc_so_term
-- from tmp_dna_change
-- where not exists (select 'x' from mutation_Detail_controlled_vocabulary
--       	   	  	  where mdcv_term_zdb_id = loc_so_term);

select * from tmp_load
 where feature_id = 'ZDB-ALT-160323-7';

delete from tmp_load
 where dna_start is null
 or dna_start = '';

update tmp_dna_change
  set dna_position_start = (Select distinct dna_Start from tmp_load
      		       	 	 where tmp_load.feature_id = tmp_dna_change.feature_id
				 and dna_end is not null
				 and tmp_load.feature_id not in ('ZDB-ALT-160111-1',                                 
               'ZDB-ALT-140623-6',                                  
               'ZDB-ALT-140411-5'));
select * from tmp_load
 where feature_id = 'ZDB-ALT-160323-7';
select * from tmp_dna_change
 where feature_id = 'ZDB-ALT-160323-7';

insert into feature_dna_mutation_detail (fdmd_zdb_id, fdmd_feature_zdb_id, fdmd_dna_mutation_term_zdb_id,
       	    					      fdmd_dna_position_start,
						      fdmd_dna_position_end,
						      fdmd_number_additional_dna_base_pairs,
						      fdmd_number_removed_dna_base_pairs,
						      fdmd_dna_sequence_of_reference_accession_number,
						      fdmd_gene_localization_term_zdb_id,
						      fdmd_exon_number,
						      fdmd_intron_number)
 select id, feature_id, dna_change, dna_position_Start, dna_position_end, plus_dna_change, minus_dna_change,
 	ref_seq, loc_so_term,exon_number, intron_number
 from tmp_dna_change
 where exists (Select 'x' from feature where feature_zdb_id = feature_id)
 and not exists (Select 'x' from feature_dna_mutation_detail
     	 		where fdmd_feature_zdb_id = feature_id);

select first 10 * from feature_dna_mutation_Detail
 where fdmd_dna_mutation_Term_zdb_id is not null
and fdmd_feature_zdb_id = 'ZDB-ALT-041104-2';

select * from tmp_load
 where feature_id = 'ZDB-ALT-041104-2';


						      
--rollback work;

commit work;
