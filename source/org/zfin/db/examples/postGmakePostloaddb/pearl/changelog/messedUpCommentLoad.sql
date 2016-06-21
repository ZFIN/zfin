
create temp table tmp_load (feature_id varchar(50),
       	    	  	    dna_change varchar(15),
			   minus_dna_change int8,
			    plus_dna_change int8,
			    dna_start int8,
			    dna_end int8,
			    ref_seq varchar(50),
			    loc_within_gene varchar(50),
			    dna_intron int8,
			    dna_exon int8,
			    consequence_transcript varchar(100),
			    rna_exon int8,
			    rna_intron int8,
			    aa_change_old varchar(50),
			    aa_minus int8,
			    aa_plus int8,
			    aa_change_new varchar(50),
			    --changed position to varchar(10) from int b/c "143-201",
			    aa_start int8,
			    aa_end int8,
			    refseq_prot varchar(30),
			    protein_consequence varchar(50),
			    chr_assembly varchar(30),
			    ref varchar(30))
with no log;


load from updated_notes_fixed.txt
 insert into tmp_load;

update tmp_load
 set rna_exon = null
 where rna_exon = '';

update tmp_load
 set rna_intron = null
 where rna_intron = '';


update tmp_load
 set aa_plus = null
 where aa_plus = '';

update tmp_load
 set aa_minus = null
 where aa_minus = '';


update tmp_load
 set aa_change_old = null
 where aa_change_old = '';

update tmp_load
 set aa_change_new = null
 where aa_change_new = '';


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

update tmp_load
 set feature_id = (replace(feature_id,' ',''));

!echo "protein change";
----
---- PROTEIN CHANGE, CONSEQUENCE, POSITION ----
----

update tmp_load 
 set aa_change_new = null
 where aa_change_new = 'X' or lower(aa_change_new) = 'stop';

select * from tmp_load
where feature_id = 'ZDB-ALT-070730-10';


create temp table tmp_protein_change (id varchar(50), feature_id varchar(50), 
  	    	  		     aa_change_old varchar(50), 
				     aa_change_new varchar(50),
				     protein_so_term varchar(50),
				     aa_start int8,
				     aa_end int8,
				     aa_plus int8,
				     aa_minus int8
				     
)
with no log;


insert into tmp_protein_change (feature_id)
 select distinct trim(feature_id) 
  from tmp_load
;


update tmp_protein_change
 set aa_plus = (select aa_plus from tmp_load
     	       	       where tmp_protein_change.feature_id = tmp_load.feature_id
		       and aa_plus is not null);

update tmp_protein_change
 set aa_minus = (select aa_minus from tmp_load
     	       	       where tmp_protein_change.feature_id = tmp_load.feature_id
		       and aa_minus is not null);

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

update tmp_protein_change
 set aa_end = (select distinct aa_end from tmp_load
     	      		where tmp_load.feature_id = tmp_protein_change.feature_id
			and tmp_load.aa_end is not null);




update tmp_protein_change
 set aa_change_old = (Select mdcv_term_Zdb_id from mutation_detail_Controlled_vocabulary 
     		     	     where mdcv_term_abbreviation = aa_Change_old);

update tmp_protein_change
 set aa_change_old = (Select mdcv_term_zdb_id from mutation_detail_Controlled_vocabulary 
     		     	     where mdcv_term_display_name = aa_Change_old)
where aa_change_old not like 'ZDB-TERM%';


update tmp_protein_change
 set aa_change_new = (Select mdcv_term_zdb_id from mutation_detail_Controlled_vocabulary 
     		     	     where mdcv_term_abbreviation = aa_Change_new);

update tmp_protein_change
 set aa_change_new = (Select mdcv_term_zdb_id from mutation_detail_Controlled_vocabulary 
     		     	     where mdcv_term_display_name = aa_Change_new)
where aa_change_new not like 'ZDB-TERM%';

select feature_id, protein_consequence
 from tmp_load
 where protein_consequence is not null
into temp tmp_prot;

select count(*), feature_id
 from tmp_prot
group by feature_id
having count(*) > 1;


update tmp_protein_change
 set protein_so_term = (select distinct protein_consequence from tmp_load 
     		       	       where tmp_load.feature_id = tmp_protein_change.feature_id
			       and protein_consequence is not null)
 where feature_id not in ('ZDB-ALT-980203-769',                                
               'ZDB-ALT-040224-26');

update tmp_protein_change
 set protein_So_term = (select term_zdb_id from term where term_name = protein_so_term)
 where exists (Select 'x' from term where term_name = protein_so_term);


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

insert into zdb_active_data
 select id from tmp_protein_change;


insert into feature_protein_mutation_Detail (fpmd_zdb_id, fpmd_feature_zdb_id, fpmd_wt_protein_term_zdb_id, fpmd_mutant_or_stop_protein_Term_zdb_id, fpmd_protein_consequence_term_zdb_id, fpmd_protein_position_Start, fpmd_protein_position_end,
       fpmd_number_amino_acids_removed, fpmd_number_amino_acids_added)
  select id, feature_id, aa_change_old, aa_change_new, protein_so_term, aa_start, aa_end, aa_minus, aa_plus
   from tmp_protein_change
   where not exists (Select 'x' from feature_protein_mutation_detail
   	     	    	    where fpmd_feature_zdb_id = featurE_id);

select first 2 * from tmp_protein_change
 where aa_start is not null;




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
			   loc_so_term varchar(50)			  )
with no log;

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
 set dna_change = null
 where dna_change = '';


insert into tmp_dna_change (feature_id)
 select distinct feature_id from tmp_load;

update tmp_dna_change
 set dna_change = (Select distinct dna_change from tmp_load
     		  	  where tmp_load.feature_id = tmp_dna_change.feature_id
			  and dna_change is not null);

select * from tmp_dna_change
where feature_id = 'ZDB-ALT-070730-10';



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
into temp tmp_start2;

select count(*), feature_id
 from tmp_start2
where dna_start is not null
 group by feature_id
 having count(*) > 1;

select * from tmp_load
 where feature_id = 'ZDB-ALT-100504-7';

update tmp_dna_change
  set dna_position_start = (Select distinct dna_Start from tmp_load
      		       	 	 where tmp_load.feature_id = tmp_dna_change.feature_id
				 and dna_start is not null
				 and tmp_load.feature_id not in ('ZDB-ALT-150423-4',                                
               'ZDB-ALT-140611-5')
);


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
				 and tmp_load.feature_id not in ('ZDB-ALT-150423-4', 'ZDB-ALT-140611-5'));



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
 set intron_number = (select distinct dna_intron from tmp_load 
     		   	   where tmp_load.feature_id = tmp_dna_change.feature_id
			   and dna_intron is not null);

update tmp_dna_change
 set exon_number = (select distinct dna_exon from tmp_load 
     		   	   where tmp_load.feature_id = tmp_dna_change.feature_id
			   and dna_exon is not null);


update tmp_dna_change
 set loc_so_term = null
 where loc_so_term in ('exon','intron');

update tmp_dna_change
 set loc_so_term = (select distinct loc_within_gene from tmp_load 
     		   	   where tmp_load.feature_id = tmp_dna_change.feature_id
			   and loc_within_gene is not null);



update tmp_dna_change
 set id = get_id('FDMD');

insert into zdb_Active_data
 select id from tmp_dna_change;

update tmp_dna_change
 set loc_so_term = (select term_zdb_id from term
     		   	   where term_name = loc_so_term);

update tmp_dna_change
  set loc_so_term = (select mdcv_term_zdb_id from mutation_Detail_controlled_vocabulary
      		    	    where mdcv_term_display_name = loc_so_term)
 where loc_so_term not like 'ZDB-TERM%';

select distinct loc_so_term
 from tmp_Dna_change
 where loc_so_term not like 'ZDB-TERM%';

select * from tmp_dna_change
 where feature_id = 'ZDB-ALT-070730-10';


insert into mutation_detail_controlled_vocabulary (mdcv_term_zdb_id, mdcv_term_display_name, mdcv_used_in)
 select  distinct loc_so_term, term_name, 'dna_mutation_term'
   from term, tmp_dna_change
   where term_Zdb_id = loc_so_term
 and not exists (select 'x' from mutation_detail_controlled_vocabulary
     	 		where mdcv_term_zdb_id = loc_so_term);


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


create temp table tmp_rnaChange (id varchar(50),
       	    	  		    feature_id varchar(50),
				    rna_term varchar(50),
				    rna_exon int8,
				    rna_intron int8
				    )
with no log;

insert into tmp_rnaChange(feature_id, rna_term, rna_exon, rna_intron)
  select distinct feature_id,consequence_transcript, rna_exon,rna_intron from tmp_load;

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
		   where mdcv_term_display_name = rna_term);

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
							     ftmd_transcript_consequence_term_zdb_id,
							     ftmd_exon_number,
							     ftmd_intron_number)
 select id, feature_id, rna_term, rna_exon, rna_intron
 from tmp_rnachange
      where exists (Select 'x' from feature where feature_zdb_id = feature_id)
        and rna_term is not null 
        and not exists (Select 'x' from feature_transcript_mutation_Detail
	    	       	       where ftmd_feature_zdb_id = feature_id
			       and ftmd_transcript_consequence_term_zdb_id = rna_term);



update feature_dna_mutation_detail
 set fdmd_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-36'
 where fdmd_dna_sequence_of_reference_accession_number is not null;

update feature_protein_mutation_detail
 set fpmd_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-36'
 where fpmd_sequence_of_reference_accession_number is not null;

