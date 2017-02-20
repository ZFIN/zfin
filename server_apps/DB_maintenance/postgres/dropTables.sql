begin work ;

set constraints all deferred;
set triggers disabled; 

drop table staging_webpages;
drop table sysblderrorlog;
drop table sysbldiprovided;
drop table sysbldirequired;
drop table sysbldobjdepends;
drop table sysbldobjects;
drop table sysbldobjkinds ;
drop table sysbldregistered;
drop table syserrors;
drop table systraceclasses; 
drop table systracemsgs;
drop table webcmimages ;
drop table webcmpages ;
drop table webconfigs;
drop table webenvvariables ;
drop table webpages;
drop table webtags; 
drop table webudrs;
drop table btsfse_storage ;
drop table affected_gene_group;
drop view vmrkrgoevsamesize;
drop view vgroupsize;

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


--TODO: why don't these process with the schema file
drop table paneled_markers;
drop table pub_db_xref;

alter table company 
  modify (entry_time datetime year to second default current year to second);

alter table lab 
  modify (entry_time datetime year to second default current year to second);

alter table person 
  modify (entry_time datetime year to second default current year to second);

alter table genotype
  modify (geno_date_entered datetime year to second default current year to second);

set constraints all immediate;

set triggers enabled; 

commit work;
