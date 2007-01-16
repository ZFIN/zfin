begin work ;

update marker
  set mrkr_type = 'EFG'
 where mrkr_zdb_id like 'ZDB-EFG-%' ;

--rollback work ;
commit work ;