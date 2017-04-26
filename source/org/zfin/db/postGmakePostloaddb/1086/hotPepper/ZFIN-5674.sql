--liquibase formatted sql
--changeset prita:ZFIN-5674

insert into zdb_active_Data (zactvd_Zdb_id) values ('ZDB-CV-170426-1');
insert into controlled_vocabulary (cv_zdb_id,cv_term_name,cv_foreign_species,cv_name_definition) values ('ZDB-CV-170426-1','Pvi.','Asian green mussel','Perna viridis');
