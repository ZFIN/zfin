-- addTalbotSNPAttr.sql
-- add attribution (publication of J. Smith and/or Johson SNP) to Talbot SNPs, if shared common dbSNP rs id
begin work;

create temporary table tmp_record_attribution (tmp_snp_id text, tmp_pub_id text);

copy tmp_record_attribution from '<!--|ROOT_PATH|-->/server_apps/data_transfer/SNP/forrecordattrtable.unl' (delimiter '|');

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
   select tmp_snp_id,tmp_pub_id from tmp_record_attribution; 

commit work;
-- rollback work;
