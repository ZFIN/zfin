--liquibase formatted sql
--changeset cmpich:zfin-6005

update foreign_db set fdb_db_query = 'http://alliancegenome.org/disease/' where fdb_db_name ='AGR Disease';
update foreign_db set fdb_db_query = 'http://alliancegenome.org/gene/ZFIN:' where fdb_db_name ='AGR Gene';
