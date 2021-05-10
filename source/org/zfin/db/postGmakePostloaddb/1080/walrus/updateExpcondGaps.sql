--liquibase formatted sql
--changeset sierra:updateExpcondGaps

update experiment_condition
 set expcond_chebi_term_zdb_id = (Select term_zdb_id
     			       	 	 from term, tmp_load	
					 where term_ont_id = chebiId
					 and expcondId = expcond_zdb_id)
 where exists (Select 'x' from tmp_load
       	      	      where expcond_zdb_id = expcondId);

