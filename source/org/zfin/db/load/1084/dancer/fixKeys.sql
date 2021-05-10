begin work ;

set constraints all deferred;

set triggers for feature_marker_relationship disabled; 
set triggers for marker_Relationship disabled;
set triggers for marker disabled;

update marker
 set mrkr_Zdb_id = trim(mrkr_zdb_id);

update construcT_marker_relationship
 set conmrkrrel_mrkr_zdb_id = trim(conmrkrrel_mrkr_zdb_id);

update feature_marker_Relationship
  set fmrel_mrkr_zdb_id = trim(fmrel_mrkr_zdb_id);

update marker_relationship
 set mrel_mrkr_1_zdb_id = trim(mrel_mrkr_1_zdb_id);

update marker_relationship
 set mrel_mrkr_2_zdb_id = trim(mrel_mrkr_2_zdb_id);

update person
 set zdb_id = trim(zdb_id);

update lab
 set zdb_id = trim(zdb_id);

update journal 
 set jrnl_zdb_id = trim(jrnl_zdb_id);

update publication set pub_jrnl_zdb_id = trim(pub_jrnl_zdb_id);

update marker
 set mrkr_owner = trim(mrkr_owner);

update int_data_supplier 
       set idsup_supplier_zdb_id = trim(idsup_supplier_zdb_id) 
       where idsup_supplier_zdb_id like 'ZDB-LAB%';

alter table company 
  modify (entry_time datetime year to second default current year to second);

alter table lab 
  modify (entry_time datetime year to second default current year to second);

alter table person 
  modify (entry_time datetime year to second default current year to second);

alter table genotype
  modify (geno_date_entered datetime year to second default current year to second);

set constraints all immediate;

set triggers for feature_marker_relationship enabled; 
set triggers for marker_relationship enabled; 
set triggers for marker enabled;

drop table obo_file;

commit work ;
