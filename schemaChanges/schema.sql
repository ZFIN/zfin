begin work ;

insert into foreign_db (fdb_db_name, fdb_db_query, fdb_url_suffix, fdb_db_display_name, fdb_db_significance)
  values ('PANTHER','http://pantree.org/node/annotationNode.jsp?id=','','PANTHER','13');

commit work;

--rollback work;
