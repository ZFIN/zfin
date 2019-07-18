--liquibase formatted sql
--changeset pm:DLOAD-619




update tmpcne set nccrid=get_id('NCCR');

insert into zdb_replaced_data (zrepld_new_zdb_id, zrepld_old_zdb_id)
 select cneid,nccrid from tmpcne;

 
insert into withdrawn_data (wd_new_zdb_id, wd_old_zdb_id)
select cneid,nccrid from tmpcne;

insert into zdb_active_data (zactvd_zdb_id)
select nccrid from tmpcne;

insert into marker(mrkr_zdb_id,mrkr_abbrev,mrkr_name,mrkr_type,mrkr_owner,mrkr_name_order,mrkr_abbrev_order)
 select nccrid,nccrabbrev,nccrname||'NCCR','NCCR','ZDB-PERS-981201-7',nccrname||'NCCR',nccrabbrev
from tmpcne;





update sequence_feature_chromosome_location
   set sfcl_feature_Zdb_id = nccrid
   from tmpcne
 where sfcl_feature_Zdb_id =cneid;

update record_attribution
 set recattrib_data_zdb_id = nccrid
 from tmpcne
 where recattrib_data_zdb_id=cneid;

 delete from zdb_active_data where zactvd_zdb_id in (select cneid from tmpcne) ;

 update marker set mrkr_name=replace(mrkr_name,'NCCR','') where mrkr_name like '%NCCR%';
 update marker set mrkr_name_order=replace(mrkr_name_order,'NCCR','') where mrkr_name_order like '%NCCR%';

