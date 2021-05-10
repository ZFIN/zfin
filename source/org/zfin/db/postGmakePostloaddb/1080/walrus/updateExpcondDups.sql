--liquibase formatted sql
--changeset sierra:updateExpcondGaps

update experiment_condition
 set expcond_chebi_term_zdb_id = (Select term_zdb_id
     			       	 	 from term, tmp_load2	
					 where term_ont_id = chebiId
					 and expcondId = expcond_zdb_id
					 and expcondId is not null)
 where exists (Select 'x' from tmp_load2
       	      	      where expcond_zdb_id = expcondId
		      and expcondId is not null);

delete from tmp_load2
 where expcondId is not null;

update tmp_load2
 set expcondId = get_id('EXPCOND')
 where expcondId is null or expcondId = '';

insert into zdb_active_data
 select expcondId from tmp_load2
 where not exists (Select 'x' from zdb_active_data
       	   	  	  where zactvd_zdb_id = expcondId);

insert into experiment_condition (expcond_exp_zdb_id, expcond_zdb_id,
       	    			 expcond_zeco_term_zdb_id,
				 expcond_chebi_term_zdb_id)
 select expId, expcondId, zecoId, chebiId
   from tmp_load2;

update experiment_condition
 set expcond_chebi_term_zdb_id = (Select term_zdb_id
     			       	 	 from term, tmp_load2	
					 where term_ont_id = chebiId
					 and expcondId = expcond_zdb_id)
 where exists (Select 'x' from tmp_load2
       	      	      where expcond_zdb_id = expcondId);

