--liquibase formatted sql
--changeset sierra:removeZdbReplacedDataRecord

delete from zdb_replaced_data
 where zrepld_old_Zdb_id = 'ZDB-GENEP-090511-3'
and zrepld_new_zdb_id = 'ZDB-GENE-110921-3';


create temp table tmp_mrel (id varchar(50), gene_id varchar(50), est_id varchar(50), snp_id varchar(50))
with no log;

select get_id('MREL'), e.mrel_mrkr_1_zdb_id, d.mrel_mrkr_1_zdb_id, d.mrel_mrkr_2_zdb_id
  from marker_relationship d, marker_relationship e
  where d.mrel_mrkr_1_Zdb_id like 'ZDB-EST%'
 and d.mrel_mrkr_2_zdb_id like 'ZDB-SNP%'
 and d.mrel_mrkr_1_zdb_id = e.mrel_mrkr_2_zdb_id
 and e.mrel_mrkr_1_zdb_id like 'ZDB-GENE%'
 and not exists (Select 'x' from marker_relationship f
                        where f.mrel_mrkr_1_zdb_id = e.mrel_mrkr_1_zdb_id
                        and f.mrel_mrkr_2_zdb_id = d.mrel_mrkr_2_zdb_id);

insert into tmp_mrel (gene_id, est_id, snp_id)
 select distinct e.mrel_mrkr_1_zdb_id, d.mrel_mrkr_1_zdb_id, d.mrel_mrkr_2_zdb_id
  from marker_relationship d, marker_relationship e
  where d.mrel_mrkr_1_Zdb_id like 'ZDB-EST%'
 and d.mrel_mrkr_2_zdb_id like 'ZDB-SNP%'
 and d.mrel_mrkr_1_zdb_id = e.mrel_mrkr_2_zdb_id
 and e.mrel_mrkr_1_zdb_id like 'ZDB-GENE%'
 and not exists (Select 'x' from marker_relationship f
     	 		where f.mrel_mrkr_1_zdb_id = e.mrel_mrkr_1_zdb_id
			and f.mrel_mrkr_2_zdb_id = d.mrel_mrkr_2_zdb_id);

update tmp_mrel
 set id = get_id('MREL');

insert into zdb_active_data
 select id from tmp_mrel;

insert into marker_relationship (mrel_zdb_id, mrel_mrkr_1_zdb_id,
       	    				      mrel_mrkr_2_zdb_id,
					      mrel_type)
 select id, gene_id, snp_id, 'contains polymorphism'
   from tmp_mrel
 where not exists (select 'x' from marker_relationship
       	   	  	  where mrel_mrkr_1_zdb_id = gene_id
			  and mrel_mrkr_2_zdb_id = snp_id
			  and mrel_type = 'contains polymorphism');
