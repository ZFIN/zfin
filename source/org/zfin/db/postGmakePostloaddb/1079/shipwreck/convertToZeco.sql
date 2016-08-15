--liquibase formatted sql
--changeset sierra:convertToZeco


update tmp_zeco_tt
  set aoTermId = (select term_zdb_id from term where term_ont_id = otherTermId
      		   	   and otherTermId like 'ZFA:%');

update tmp_zeco_tt
  set chebiTermId = (select term_zdb_id from term where term_ont_id = otherTermId
      		   	   and otherTermId like 'CHEBI:%');

update tmp_zeco_tt
  set otherTermId = "NCBITaxon:"||otherTermId
 where otherTermId not like "%:%"
 and otherTermId not like "none";

update tmp_zeco_tt
 set zecoid = (select term_zdb_id from term 
     	       	       where term_ont_id = zecoid);

update tmp_zeco_tt
 set ccTermId = (select term_zdb_id from term
     		  	  where term_ont_id = ccTermId);

update tmp_zeco_tt
 set otherTermId = "none"
 where otherTermId is null;

update tmp_zeco_tt
 set ccTermId = "none"
where ccTermId is null
 or ccTermId = "";


delete from tmp_zeco_tt 
where not exists (select 'x' from experiment_condition
      	  	 	 where expcond_exp_Zdb_id = expid);

select count(*) as counter, expid as exp_id, cdtId
 from tmp_zeco_tt
 group by exp_id, cdtId having count(*) > 1
into temp temp_dups_temp;

delete from tmp_zeco_tt
 where exists (Select 'x' from temp_dups_temp
       	      	      	  where expid = exp_id);

delete from tmp_zeco_tt
 where zecoid is null;

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
 set expcond_zeco_term_zdb_id = (select zecoid  from tmp_zeco_tt
     			      		where expcond_exp_zdb_id = expid
					and expcond_cdt_zdb_id = cdtid);
     			      
update experiment_condition
 set expcond_chebi_term_zdb_id = (select chebitermid  from tmp_zeco_tt
     			      		where expid = expcond_exp_zdb_id
					and cdtid = expcond_cdt_zdb_id);

update experiment_condition
 set expcond_ao_term_zdb_id = (select aotermid from tmp_zeco_tt
     			      		where expid = expcond_exp_zdb_id
					and cdtid = expcond_cdt_zdb_id);
update experiment_condition
 set expcond_go_cc_term_zdb_id = (select cctermid from tmp_zeco_tt
     			      		where expid = expcond_exp_zdb_id
					and cdtid = expcond_cdt_zdb_id);
