--liquibase formatted sql
--changeset sierra:updateToDefaultZecoTerms

update experiment_condition
 set expcond_zeco_term_zdb_id = (Select term_zdb_id
     			      		from term
					where term_name = 'chemical treatment')
 where exists (Select 'x' from condition_data_type
       	      	      where cdt_zdb_id = expcond_cdt_zdb_id
		      and cdt_group = 'chemical');


update experiment_condition
 set expcond_chebi_zdb_id = (Select term_zdb_id
     			      		from term
					where term_ont_id = 'CHEBI:52217')
 where expcond_chebi_zdb_id is null
 and expcond_zeco_term_zdb_id = (Select term_zdb_id
     			      		from term
					where term_name = 'chemical treatment');
