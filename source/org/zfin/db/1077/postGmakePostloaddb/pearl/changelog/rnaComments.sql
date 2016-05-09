
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

select * from tmp_load
 where feature_id ='ZDB-ALT-160317-7';

delete from tmp_load
 where feature_id is null 
 or feature_id = '';

create temp table tmp_rnaChange (id varchar(50),
       	    	  		    feature_id varchar(50),
				    rna_term varchar(50))
with no log;

insert into tmp_rnaChange(feature_id, rna_term)
  select distinct feature_id,consequence_transcript from tmp_load;

update tmp_rnaChange
 set rna_term = (select term_zdb_id from term
     	      		where rna_term = term_name)
where exists (Select 'x' from term
      	     	     where rna_term = term_name);

update tmp_rnaChange
 set rna_term = (select mdcv_term_zdb_id from mutation_detail_controlled_vocabulary
     	      		where mdcv_term_display_name = rna_term)
where rna_term not like 'ZDB-TERM%'
and exists (select 'x' 
    	   	   from mutation_detail_controlled_vocabulary
		   where lower(mdcv_term_display_name) = lower(rna_term));

select distinct rna_term
 from tmp_rnaChange
 where rna_term not like 'ZDB-TERM%';

delete from tmp_rnachange
 where rna_term not like 'ZDB-TERM%';

 select distinct rna_term, term_name
   from tmp_rnachange, term
 where rna_term=term_zdb_id
 and not exists (Select 'x' from mutation_detail_controlled_vocabulary
     	 		where mdcv_term_zdb_id = rna_term);

select * from tmp_rnachange
 where feature_id ='ZDB-ALT-160317-7';

insert into mutation_detail_controlled_vocabulary (mdcv_term_Zdb_id, mdcv_term_display_name, mdcv_used_in)
 select distinct rna_term, term_name, "transcript_consequence_term"
   from tmp_rnachange, term
 where rna_term=term_zdb_id
 and not exists (Select 'x' from mutation_detail_controlled_vocabulary
     	 		where mdcv_term_zdb_id = rna_term);


update tmp_rnaChange 
 set id = get_id('FTMD');

insert into zdb_active_data
 select id from tmp_rnachange;

insert into feature_transcript_mutation_detail (ftmd_zdb_id, 
       	    				       		     ftmd_feature_zdb_id,
							     ftmd_transcript_consequence_term_zdb_id)
 select id, feature_id, rna_term
 from tmp_rnachange
      where exists (Select 'x' from feature where feature_zdb_id = feature_id)
        and rna_term is not null 
        and not exists (Select 'x' from feature_transcript_mutation_Detail
	    	       	       where ftmd_feature_zdb_id = feature_id
			       and ftmd_transcript_consequence_term_zdb_id = rna_term);			    

