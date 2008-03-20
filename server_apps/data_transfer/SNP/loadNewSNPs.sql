-- loadNewSNPs.sql
-- load snp_download
-- need forSNPDtable.unl

begin work;

create temp table tmp_snpd (       tmp_snpd_rs_acc_num varchar(40),
	                           tmp_snpd_mrkr_zdb_id varchar(50)
                           ) with no log;

load from forSNPDtable.unl 
  insert into tmp_snpd;

! echo "                           into tmp table tmp_snpd."

insert into snp_download (snpd_pk_id,snpd_rs_acc_num,snpd_mrkr_zdb_id)
  select 0,tmp_snpd_rs_acc_num,tmp_snpd_mrkr_zdb_id
    from tmp_snpd;  

! echo "                   into snp_download table."

commit work;
--rollback work;

