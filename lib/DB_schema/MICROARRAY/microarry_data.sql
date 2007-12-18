insert into marker_type_group ( mtgrp_name , mtgrp_comments) values ('CDNA_AND_EST','Group contains only CDNA and EST') ;

insert into marker_type_group_member ( mtgrpmem_mrkr_type , mtgrpmem_mrkr_type_group) values ('CDNA','CDNA_AND_EST') ;

insert into marker_type_group_member ( mtgrpmem_mrkr_type , mtgrpmem_mrkr_type_group) values ('EST','CDNA_AND_EST') ;

insert into foreign_db (fdb_db_name,fdb_db_query,fdb_url_suffix,fdb_db_significance) VALUES('GEO','http://www.ncbi.nlm.nih.gov/sites/entrez?cmd=search&db=geo&term=txid7955%20(',')',2);

insert into zdb_active_data (zactvd_zdb_id) VALUES('ZDB-FDBCONT-070919-1');

insert into foreign_db_contains (fdbcont_zdb_id,fdbcont_fdbdt_data_type,fdbcont_fdb_db_name,fdbcont_organism_common_name,fdbcont_fdbdt_super_type) VALUES('ZDB-FDBCONT-070919-1','other','GEO','Zebrafish','summary page'); 


--insert into foreign_db (fdb_db_name,fdb_db_query,fdb_url_suffix,fdb_db_significance) VALUES('ZF-Espresso','http://zf-espresso.tuebingen.mpg.de/webservice.php?page=graph.php&pf=pftxt&pst=',null,2);

--insert into zdb_active_data (zactvd_zdb_id) VALUES('ZDB-FDBCONT-071107-1');

--insert into foreign_db_contains (fdbcont_zdb_id,fdbcont_fdbdt_data_type,fdbcont_fdb_db_name,fdbcont_organism_common_name,fdbcont_fdbdt_super_type) VALUES('ZDB-FDBCONT-071107-1','other','ZF-Espresso','Zebrafish','summary page'); 



--insert into foreign_db (fdb_db_name,fdb_db_query,fdb_url_suffix,fdb_db_significance) VALUES('ArrayExpress','http://www.ebi.ac.uk/microarray-as/aew/DW?queryFor=gene&species=Danio%20Rerio&gene_query=',null,2);

--insert into zdb_active_data (zactvd_zdb_id) VALUES('ZDB-FDBCONT-071107-2');

--insert into foreign_db_contains (fdbcont_zdb_id,fdbcont_fdbdt_data_type,fdbcont_fdb_db_name,fdbcont_organism_common_name,fdbcont_fdbdt_super_type) VALUES('ZDB-FDBCONT-071107-2','other','ArrayExpress','Zebrafish','summary page'); 


