-- loadNewSNPs.sql
-- load snp_download
-- need forSNPDtable.unl

begin work;

create temporary table tmp_snpd (       tmp_snpd_rs_acc_num text,
	                           tmp_snpd_mrkr_zdb_id text
                           );
                           
copy tmp_snpd from '<!--|ROOT_PATH|-->/server_apps/data_transfer/SNP/forSNPDtable.unl' (delimiter '|');


--! echo "                           into tmp table tmp_snpd."

insert into snp_download (snpd_rs_acc_num,snpd_mrkr_zdb_id)
  select tmp_snpd_rs_acc_num,tmp_snpd_mrkr_zdb_id
    from tmp_snpd;  

--! echo "                   into snp_download table."

commit work;
--rollback work;

