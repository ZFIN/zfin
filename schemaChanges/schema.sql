begin work ;

insert into foreign_db (fdb_db_name, fdb_db_query, fdb_url_suffix, fdb_db_display_name, fdb_db_significance)
  values ('PANTHER','http://pantree.org/node/annotationNode.jsp?id=','','PANTHER','13');

insert into go_evidence_code (goev_code,goev_name,goev_display_order) values ('IBA','inferred from biological aspect of ancestor',12);
insert into go_evidence_code (goev_code,goev_name,goev_display_order) values ('IBD','inferred from biological aspect of descendant',13);
insert into go_evidence_code (goev_code,goev_name,goev_display_order) values ('IKR','inferred from key residues',14);
insert into go_evidence_code (goev_code,goev_name,goev_display_order) values ('IMR','inferred from missingy residues',15);
insert into go_evidence_code (goev_code,goev_name,goev_display_order) values ('IRD','inferred from rapid divergence',16);



update foreign_db 
set fdb_db_query='http://www.ensembl.org/Danio_rerio/Variation/Summary?db=core;vdb=variation;v='
where fdb_db_name = 'Ensembl_SNP'

execute function regen_term();

commit work;

--rollback work;
