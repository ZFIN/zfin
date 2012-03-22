begin work;

create table sangerfeatures (
        abbrev varchar(50));
load from sangeralleles insert into sangerfeatures;

create table pre_db_link (
        predblink_data_zdb_id varchar(50) not null,
        predblink_acc_num varchar(50) not null,
        predblink_acc_num_display varchar(50) not null,
        predblink_fdbcont_zdb_id varchar(50) not null
);

insert into pre_db_link (
        predblink_data_zdb_id,
        predblink_acc_num,
        predblink_acc_num_display,
        predblink_fdbcont_zdb_id)
  select distinct feature_zdb_id, feature_abbrev, feature_abbrev, fdbcont_zdb_id 
    from feature, foreign_db, foreign_db_contains 
   where feature_comments like '%Zebrafish Mutation Resource%' 
     and fdbcont_fdb_db_id = fdb_db_pk_id 
     and fdb_db_name = 'ZMP';

insert into pre_db_link (
        predblink_data_zdb_id,
        predblink_acc_num,
        predblink_acc_num_display,
        predblink_fdbcont_zdb_id)
  select distinct feature_zdb_id, feature_abbrev, feature_abbrev, fdbcont_zdb_id 
    from feature, foreign_db, foreign_db_contains,sangerfeatures
   where feature_abbrev=abbrev
     and fdbcont_fdb_db_id = fdb_db_pk_id 
     and fdb_db_name = 'ZMP' and feature_comments not like '%Zebrafish Mutation Resource%'
    ;
     
! echo "         into pre_db_link table."  


alter table pre_db_link add predblink_dblink_zdb_id varchar(50);

update pre_db_link set predblink_dblink_zdb_id = get_id('DBLINK');

unload to 'pre_db_link_cleanupSanger' select * from pre_db_link order by predblink_acc_num;

! echo "         to pre_db_link_cleanupSanger"  
 
insert into zdb_active_data select predblink_dblink_zdb_id from pre_db_link;

! echo "         into zdb_active_data table."  

insert into db_link (dblink_linked_recid,dblink_acc_num, dblink_zdb_id ,dblink_acc_num_display,dblink_fdbcont_zdb_id) 
  select predblink_data_zdb_id, predblink_acc_num, predblink_dblink_zdb_id, predblink_acc_num_display, predblink_fdbcont_zdb_id 
    from pre_db_link; 

! echo "         into db_link table."      


insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)  
  select predblink_dblink_zdb_id,'ZDB-PUB-110121-1' from pre_db_link;

! echo "         into record_attribution table."  

unload to 'updatedFeaturesToCleanUpSangerComments' 
   select feature_abbrev, feature_name, feature_zdb_id 
     from feature
    where feature_comments like '%Zebrafish Mutation Resource%' 
 order by feature_abbrev;

unload to 'updatedGenotypesToCleanUpSangerComments' 
   select geno_display_name,extnote_note
     from genotype,external_note
    where extnote_note like '%Zebrafish Mutation Resource%' 
    and extnote_data_zdb_id=geno_zdb_id
 order by geno_zdb_id;

update feature set feature_comments = '' 
             where feature_comments like '%Zebrafish Mutation Resource%'; 
delete from external_note where extnote_note like '%Zebrafish Mutation Resource%';

unload to 'Stemplegenotypes'
    select idsup_data_zdb_id,geno_display_name 
    from int_data_supplier, genotype
    where idsup_data_zdb_id=geno_zdb_id
    and idsup_supplier_zdb_id='ZDB-LAB-050412-2';

unload to 'Stemplefeatures'
    select idsup_data_zdb_id,feature_abbrev 
    from int_data_supplier, feature
    where idsup_data_zdb_id=feature_zdb_id
    and idsup_supplier_zdb_id='ZDB-LAB-050412-2';

--delete from int_data_supplier where idsup_supplier_zdb_id='ZDB-LAB-050412-2' and idsup_data_zdb_id like 'ZDB-GENO%';

unload to 'sangerstemple' select geno_zdb_id from genotype, genotype_feature,feature, sangerfeatures,int_data_supplier where abbrev=feature_abbrev and feature_zdb_id=genofeat_feature_zdb_id and geno_zdb_id=genofeat_geno_zdb_id and geno_zdb_id=idsup_data_zdb_id and idsup_supplier_zdb_id='ZDB-LAB-050412-2';

delete from int_data_supplier where idsup_data_zdb_id in (select geno_zdb_id from genotype, genotype_feature,feature, sangerfeatures where abbrev=feature_abbrev and feature_zdb_id=genofeat_feature_zdb_id and geno_zdb_id=genofeat_geno_zdb_id) and idsup_supplier_zdb_id='ZDB-LAB-050412-2';

drop table pre_db_link;

                                 
--rollback work;

commit work;
