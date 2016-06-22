--liquibase formatted sql
--changeset sierra:updateToDefaultZecoTerms

update experiment_condition
 set expcond_zeco_term_zdb_id = (Select term_zdb_id
     			      		from term
					where term_name = 'chemical treatment')
 where exists (Select 'x' from condition_data_type
       	      	      where cdt_zdb_id = expcond_zdb_id
		      and cdt_group = 'chemical');
