begin work;

update zdb_submitters 
  set name = 'sie dawg               '
where login = 'staylor';

select * from zdb_submitters where login = 'staylor';

commit work;

