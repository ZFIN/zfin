--liquibase formatted sql
--changeset sierra:deleteDups

create temp table tmp_dups (counter int,
				exp_zdb_id varchar(50), 
       	    	  	   			zeco_term_zdb_id varchar(50),
     			 		      ao_term_zdb_id varchar(50),
					      go_cc_term_zdb_id varchar(50),
					      chebi_term_zdb_id varchar(50),
					      taxon_term_zdb_id varchar(50))
with no log;

insert into tmp_dups 
  select count(*) as counter, expcond_exp_zdb_id , expcond_zeco_term_zdb_id,
     			 		      expcond_ao_term_zdb_id,
					      expcond_go_cc_term_zdb_id,
					      expcond_chebi_term_zdb_id,
					      expcond_taxon_term_zdb_id
  from experiment_condition
 group by expcond_exp_zdb_id, expcond_zeco_term_zdb_id,
     			 		      expcond_ao_term_zdb_id,
					      expcond_go_cc_term_zdb_id,
					      expcond_chebi_term_zdb_id,
					      expcond_taxon_term_zdb_id
having count(*) > 1;

select first 1 * from tmp_dups;

Select min(b.expcond_zdb_id) as miner
  	       	       from experiment_condition b, tmp_dups
		       where b.expcond_exp_zdb_id = exp_zdb_id
		       and b.expcond_zeco_term_zdb_id = zeco_term_zdb_id
		       and b.expcond_ao_term_zdb_id = ao_Term_zdb_id
		       and b.expcond_go_cc_term_zdb_id = go_Cc_term_zdb_id
		       and b.expcond_chebi_Term_zdb_id =chebi_Term_zdb_id
		       and b.expcond_taxon_Term_zdb_id = taxon_term_zdb_id
into temp tmp_delete;

delete from experiment_condition 
where expcond_zdb_id in (Select miner from tmp_delete);
