--liquibase formatted sql
--changeset sierra:convertConditions

update tmp_gap_dup_tt
  set cztId = 'NCBITaxon:'||cztId
 where cztId not like 'CHEBI%'
 and cztId not like 'ZFA%'
 and cztId not like 'GO%'
 and cztId not like 'none';

update tmp_gap_dup_tt
 set expcondId = get_id('EXPCOND')
 where expcondId = 'none';

insert into zdb_active_data
 select expcondId from tmp_gap_dup_tt
 where not exists (Select 'x' from zdb_active_data
       	   	  	      where zactvd_zdb_id = expcondId);

select count(*) as counter, expcondId as id from tmp_gap_dup_tt
 group by expcondId 
having count(*) > 1
 into temp tmp_dups1;

delete from tmp_gap_dup_tt
 where exists (Select 'x' from tmp_dups1
       	      	      where id = expcondId);

delete from tmp_gap_dup_tt
 where expcondId is null or expcondId = '';

insert into experiment_condition (expcond_zdb_id, 
       	    			  expcond_exp_zdb_id)
 select expcondId, expId
   from tmp_gap_dup_tt
 where not exists (Select 'x' from experiment_condition
       	   	  	  where expcond_zdb_id = expcondId);

update experiment_condition
  set expcond_zeco_term_Zdb_id = (Select term_zdb_id from tmp_gap_dup_tt, term
      			       	 	 where expcond_zdb_id = expcondId
					 and trim(zecoId) = term_ont_id
					 )
 where (expcond_zeco_term_Zdb_id is null
       				 or expcond_zeco_term_zdb_id = ''); 



update experiment_condition
  set expcond_chebi_term_Zdb_id = (Select term_zdb_id from tmp_gap_dup_tt, term
      			       	 	 where expcond_zdb_id = expcondId
					 and trim(cztId) = term_ont_id
					 and cztId like 'CHEBI%')
 where (expcond_chebi_term_Zdb_id is null
       				 or expcond_chebi_term_zdb_id = ''); 

update experiment_condition
  set expcond_ao_term_Zdb_id = (Select term_zdb_id from tmp_gap_dup_tt, term
      			       	 	 where expcond_zdb_id = expcondId
					 and trim(cztId) = term_ont_id
					 and cztId like 'ZFA%')
 where (expcond_ao_term_Zdb_id is null
       				 or expcond_ao_term_zdb_id = ''); 

update experiment_condition
  set expcond_taxon_term_Zdb_id = (Select term_zdb_id from tmp_gap_dup_tt, term
      			       	 	 where expcond_zdb_id = expcondId
					 and trim(cztId) = term_ont_id
					 and cztId like 'NCBI%')
 where (expcond_taxon_term_Zdb_id is null
       				 or expcond_taxon_term_zdb_id = ''); 
