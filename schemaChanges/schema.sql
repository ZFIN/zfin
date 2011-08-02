begin work ;

insert into foreign_db (fdb_db_name, fdb_db_query, fdb_url_suffix, fdb_db_display_name, fdb_db_significance)
  values ('PANTHER','http://pantree.org/node/annotationNode.jsp?id=','','PANTHER','13');


update foreign_db 
set fdb_db_query='http://www.ensembl.org/Danio_rerio/Variation/Summary?db=core;vdb=variation;v='
where fdb_db_name = 'Ensembl_SNP'

commit work;

--rollback work;
