--liquibase formatted sql
--changeset sierra:convertToZeco

update tmp_zeco_tt
 set zecoid = null 
where zecoid = 'none';

update tmp_zeco_tt
 set aoTermId = null 
where aoTermId = 'none';

update tmp_zeco_tt
 set chebiTermId = null 
where chebiTermId = 'none';

update tmp_zeco_tt
 set ccTermId = null 
where ccTermId = 'none';


update experiment_condition
 set expcond_zeco_term_zdb_id = (select zecoid from tmp_zeco_tt
     			      		where expcond_exp_zdb_id = expid
					and expcond_cdt_zdb_id = cdtid);


     			      
update experiment_condition
 set expcond_chebi_zdb_id = (select chebiTermId from tmp_zeco_tt
     			      		where expid = expcond_exp_zdb_id
					and cdtid = expcond_cdt_zdb_id);

update experiment_condition
 set expcond_ao_term_zdb_id = (select aoTermId from tmp_zeco_tt
     			      		where expid = expcond_exp_zdb_id
					and cdtid = expcond_cdt_zdb_id);
update experiment_condition
 set expcond_go_cc_term_zdb_id = (select ccTermid from tmp_zeco_tt
     			      		where expid = expcond_exp_zdb_id
					and cdtid = expcond_cdt_zdb_id);
