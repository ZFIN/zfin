-- loadNewSNPAttrs.sql
-- load snp_download_attribution table
-- need forsnpdattrtable.unl 

begin work;

load from forsnpdattrtable.unl 
  insert into snp_download_attribution;

! echo "                           into snp_download_attribution table."

commit work;
--rollback work;
