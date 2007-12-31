begin work ;

alter table journal
  add (jrnl_permission_status lvarchar default 'Permission DENIED' not null constraint jrnl_permission_status_not_null);

alter table journal
  add (jrnl_perms_date_granted datetime year to second) ;

update journal
  set jrnl_permission_status = 'Open Access:Full Permission GRANTED'
  where jrnl_is_nice = 't' ;

update journal
  set jrnl_permission_status = 'Permission DENIED' 
  where jrnl_is_nice = 'f' ;

update journal
  set jrnl_permission_status = 'Open Access:Full Permission GRANTED' 
  where jrnl_is_nice = 't' 
  and jrnl_zdb_id = 'ZDB-JRNL-050621-6';

update journal
  set jrnl_permission_status = 'Open Access:Full Permission GRANTED' 
  where jrnl_abbrev like 'BMC%'
  and jrnl_is_nice = 't';

--commit work ;

rollback work ;
