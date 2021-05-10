-- loadNewSNPAttrs.sql
-- load snp_download_attribution table
-- need forsnpdattrtable.unl 

begin work;
  
copy snp_download_attribution from '<!--|ROOT_PATH|-->/server_apps/data_transfer/SNP/forsnpdattrtable.unl' (delimiter '|');
  

--! echo "                           into snp_download_attribution table."

commit work;
--rollback work;
