--liquibase formatted sql
--changeset sierra:INF-3370.sql

delete from feature_assay 
where not exists (select 'x' from feature
      	  	 	 where featassay_feature_zdb_id = feature_zdb_id);

alter table feature_assay
 add constraint (foreign key (featassay_feature_zdb_id)
 references feature constraint featassay_feature_zdb_id_fk);

