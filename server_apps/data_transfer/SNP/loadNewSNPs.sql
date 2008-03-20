-- loadNewSNPs.sql
-- load dbSNP data into snp_download and snp_download_attribution tables if they are not in ZFIN
-- need the following file2: forSNPDtable.unl forsnpdattrtable.unl

begin work;

create temp table tmp_snpd (       tmp_snpd_rs_acc_num varchar(40),
	                           tmp_snpd_mrkr_zdb_id varchar(50)
                           ) with no log;

load from forSNPDtable.unl 
  insert into tmp_snpd;

! echo "                           into tmp table tmp_snpd."

create temp table tmp_snpdattr (    tmp_snpd_rs_acc varchar(40),
	                            tmp_snpd_mrkr varchar(50),
	                            tmp_snpd_pub varchar(50)
                               ) with no log;

load from forsnpdattrtable.unl 
  insert into tmp_snpdattr;

! echo "                           into tmp table tmp_snpdattr."


insert into snp_download (snpd_pk_id,snpd_rs_acc_num,snpd_mrkr_zdb_id)
  select 0,tmp_snpd_rs_acc_num,tmp_snpd_mrkr_zdb_id
    from tmp_snpd;  

! echo "                   into snp_download table."


insert into snp_download_attribution (snpdattr_snpd_pk_id,snpdattr_pub_zdb_id)
  select snpd_pk_id,tmp_snpd_pub
  from snp_download, tmp_snpdattr
 where snpd_rs_acc_num = tmp_snpd_rs_acc
   and snpd_mrkr_zdb_id = tmp_snpd_mrkr;  

! echo "                    into snp_download_attribution table."

commit work;
--rollback work;
