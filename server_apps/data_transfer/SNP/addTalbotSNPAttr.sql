-- addTalbotSNPAttr.sql
-- add attribution (pub for J. Smith and/or Johson SNP, if shared common dbSNP rs id) to Talbot SNPs
begin work;

create temp table tmp_record_attribution (tmp_snp_id varchar(50), tmp_pub_id varchar(50)) with no log;

load from forrecordattrtable.unl 
  insert into tmp_record_attribution;

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
   select tmp_snp_id,tmp_pub_id from tmp_record_attribution; 

commit work;
-- rollback work;
