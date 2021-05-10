--liquibase formatted sql
--changeset sierra:convertConditions

update tmp_gap_tt
  set cztId = 'NCBITaxon:'||cztId
 where cztId not like 'CHEBI%'
 and cztId not like 'ZFA%'
 and cztId not like 'GO%'
 and cztId not like 'none';

select count(*) as counter, expcondId as id from tmp_gap_tt
 group by expcondId 
having count(*) > 1
 into temp tmp_dups2;

select * from tmp_gap_tt
 where expcondId = 'ZDB-EXPCOND-150327-12';

delete from tmp_gap_tt
 where exists (Select 'x' from tmp_dups2
       	      	      where id = expcondId);

delete from tmp_gap_tt
 where expcondId is null or expcondId = '';

select * from tmp_gap_tt
 where expcondId = 'ZDB-EXPCOND-150327-12';

update experiment_condition
  set expcond_zeco_term_Zdb_id = (Select term_zdb_id from tmp_gap_tt, term
      			       	 	 where expcond_zdb_id = expcondId
					 and trim(zecoId) = term_ont_id
					 )
 where (expcond_zeco_term_Zdb_id is null
       				 or expcond_zeco_term_zdb_id = ''); 

update experiment_condition
  set expcond_go_cc_term_Zdb_id = (Select term_zdb_id from tmp_gap_tt, term
      			       	 	 where expcond_zdb_id = expcondId
					 and trim(goccId) = term_ont_id)
 where (expcond_go_cc_term_Zdb_id is null
       				 or expcond_go_cc_term_zdb_id = ''); 

update experiment_condition
  set expcond_chebi_term_Zdb_id = (Select term_zdb_id from tmp_gap_tt, term
      			       	 	 where expcond_zdb_id = expcondId
					 and trim(cztId) = term_ont_id
					 and cztId like 'CHEBI%')
 where (expcond_chebi_term_Zdb_id is null
       				 or expcond_chebi_term_zdb_id = ''); 

update experiment_condition
  set expcond_ao_term_Zdb_id = (Select term_zdb_id from tmp_gap_tt, term
      			       	 	 where expcond_zdb_id = expcondId
					 and trim(cztId) = term_ont_id
					 and cztId like 'ZFA%')
 where (expcond_ao_term_Zdb_id is null
       				 or expcond_ao_term_zdb_id = ''); 

update experiment_condition
  set expcond_taxon_term_Zdb_id = (Select term_zdb_id from tmp_gap_tt, term
      			       	 	 where expcond_zdb_id = expcondId
					 and trim(cztId) = term_ont_id
					 and cztId like 'NCBI%')
 where (expcond_taxon_term_Zdb_id is null
       				 or expcond_taxon_term_zdb_id = ''); 

select * from experiment_condition
 where expcond_zdb_id = 'ZDB-EXPCOND-150327-12';


