--liquibase formatted sql
--changeset sierra:addPostComposedDefaults

update experiment_condition
 set expcond_ao_term_zdb_id = (select term_zdb_id from term
     			      	      where term_name = 'anatomical structure'
				      and term_ont_id like 'ZFA:%')
 where expcond_zeco_term_zdb_id in (select term_zdb_id from term
       				   	   where term_ont_id = 'ZECO:0000229')
 and expcond_ao_term_zdb_id is null;

update experiment_condition
 set expcond_taxon_term_zdb_id = (Select term_zdb_id from term
     			       	 	 where term_name = 'bacteria'
					 and term_ont_id like 'NCBI%')
 where expcond_zeco_term_zdb_id in (select term_zdb_id from term
       				   	   where term_ont_id = 'ZECO:0000106')
 and expcond_taxon_term_zdb_id is null;


update experiment_condition
 set expcond_taxon_term_zdb_id = (Select term_zdb_id from term
     			       	 	 where term_name = 'fungus'
					 and term_ont_id like 'NCBI%')
 where expcond_zeco_term_zdb_id in (select term_zdb_id from term
       				   	   where term_ont_id = 'ZECO:0000107')
 and expcond_taxon_term_zdb_id is null;

update experiment_condition
 set expcond_taxon_term_zdb_id = (Select term_zdb_id from term
     			       	 	 where term_name = 'virus'
					 and term_ont_id like 'NCBI%')
 where expcond_zeco_term_zdb_id in (select term_zdb_id from term
       				   	   where term_ont_id = 'ZECO:0000110')
 and expcond_taxon_term_zdb_id is null;
