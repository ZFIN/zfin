-- loadSNPs.sql
-- load SNP data into ZFIN database
-- need the following files: snp.unl which is output of parser perl scripts
--                           snp2.unl which is a handmade file from curator's validation

begin work;

create temp table tmp_mrkr (accNum varchar(50), name varchar(40), EST varchar(50), 
                            variation varchar(20), offset int, seq lvarchar
                           ) with no log;

load from snp.unl 
  insert into tmp_mrkr;

! echo "Inserted data from parsed result into tmp table."

create temp table pre_marker (id varchar(50),
                              seqid varchar(50),
                              name varchar(40),
                              abbrev varchar(40),
                              type varchar(10),
                              owner varchar(50),
                              variation varchar(20), 
                              ESTacc varchar(50),
                              offset int, 
                              seq lvarchar,
                              acc varchar(50),
                              linkId varchar(50)
                             ) with no log;

insert into pre_marker (id,seqid,name,abbrev,type,owner,variation,ESTacc,offset,seq,acc,linkId)
  select get_id('SNP'),
         get_id('MRKRSEQ'),
         name,
         lower(name),
         'SNP',
         'ZDB-PERS-980902-4',
         variation,
         EST,
         offset,
         seq,
         accNum,
         get_id('DBLINK')
    from tmp_mrkr
    where name not in (select mrkr_name from marker);

insert into zdb_active_data (zactvd_zdb_id)
  select id from pre_marker;  

insert into marker (mrkr_zdb_id,mrkr_name,mrkr_abbrev,mrkr_type,mrkr_owner)
  select id,name,abbrev,type,owner from pre_marker;  

! echo "Inserted data into marker table. Notice that owner is hardcoded."

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
       select id,"ZDB-PUB-021213-2" from pre_marker; 

! echo "All current SNP data in marker table are attributed to ZDB-PUB-021213-2 (hard-coded)."

insert into zdb_active_data (zactvd_zdb_id)
  select seqid from pre_marker;  

insert into marker_sequence (mrkrseq_zdb_id,mrkrseq_mrkr_zdb_id,mrkrseq_sequence,
                             mrkrseq_offset_start,mrkrseq_offset_stop,
                             mrkrseq_variation)
  select seqid,id,seq,offset,offset,variation from pre_marker;         

! echo "Inserted data into marker_sequence table."

create temp table pre_marker_rel (id varchar(50),
                                  type varchar(40),
                                  id1 varchar(50),
                                  id2 varchar(50)
                                 ) with no log;

insert into pre_marker_rel (id,type,id1,id2)
  select get_id('MREL'),
         'contains polymorphism',
         dblink_linked_recid,
         id
    from pre_marker, db_link
    where ESTacc = dblink_acc_num
      and dblink_linked_recid like "ZDB-EST-%";
      
! echo "Inserted data into pre_marker_rel table."

insert into zdb_active_data (zactvd_zdb_id)
  select id from pre_marker_rel;  

insert into marker_relationship (mrel_zdb_id,mrel_type,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id)
  select * from pre_marker_rel;     

! echo "Inserted data into marker_relationship table."

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
  select id,"ZDB-PUB-060208-1" from pre_marker_rel; 

! echo "All current SNP data in marker_relationship table are attributed to ZDB-PUB-060208-1 (hard-coded)."

create temp table pre_update (recid varchar(50), accnum varchar(50), abb varchar(40)) with no log;

insert into pre_update (recid,accnum,abb)
  select mrkr_zdb_id,acc,mrkr_abbrev 
    from marker,pre_marker
    where mrkr_abbrev = abbrev;

! echo "Inserted data into pre_update table."

delete from db_link where dblink_acc_num in (select accnum from pre_update);
! echo "Old db_link data associated with SNP cleared."

insert into zdb_active_data (zactvd_zdb_id)
  select linkId from pre_marker; 

insert into db_link (dblink_linked_recid,dblink_acc_num,dblink_zdb_id,dblink_fdbcont_zdb_id)
       select id,acc,linkId,fdbcont_zdb_id
         from pre_marker,foreign_db_contains
         where fdbcont_fdb_db_name = "Ensembl_SNP";

! echo "Inserted link data with Ensembl_SNP into db_link table."

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
       select linkId,"ZDB-PUB-060208-1" from pre_marker; 

create temp table pre_dblink (recordid varchar(50), accnumber varchar(50), id varchar(50)) with no log;

insert into pre_dblink (recordid,accnumber,id)
  select recid,accnum,get_id('DBLINK') 
    from pre_update;

insert into zdb_active_data (zactvd_zdb_id)
  select id from pre_dblink; 

create temp table pre_dblink2 (recordid varchar(50), accnumber varchar(50), id varchar(50))
  with no log;

insert into pre_dblink2 (recordid,accnumber,id)
  select recid,accnum,get_id('DBLINK') 
    from pre_update;

insert into zdb_active_data (zactvd_zdb_id)
  select id from pre_dblink2; 
           
insert into db_link (dblink_linked_recid,dblink_acc_num,dblink_zdb_id,dblink_fdbcont_zdb_id)
       select recordid,accnumber,id,fdbcont_zdb_id
         from pre_dblink2,foreign_db_contains
         where fdbcont_fdb_db_name = "dbSNP"
           and fdbcont_fdbdt_data_type = "other";           

! echo "Inserted link data of dbSNP with type other into db_link table"

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
       select dblink_zdb_id,"ZDB-PUB-060208-1" from db_link,foreign_db_contains
         where fdbcont_fdb_db_name = "dbSNP"
           and dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and not exists 
               (select 't' from record_attribution
                  where recattrib_data_zdb_id = dblink_zdb_id 
                    and recattrib_source_zdb_id = "ZDB-PUB-060208-1");

! echo "All current dblink data for SNP are attributed to ZDB-PUB-060208-1 (hard-coded)."

-- update mapped_marker table to replace EST to SNP
update mapped_marker 
  set marker_id = 
      (select recid from pre_update
           where abb = map_name) 
  where marker_type = "SNP" 
     and marker_id in 
       (select mrkr_zdb_id from marker)
     and marker_id in 
       (select id from pre_marker);   

! echo "mapped_marker records updated."

-- update zmap_pub_pan_mark table to replace EST to SNP
update zmap_pub_pan_mark 
  set zdb_id = 
      (select recid from pre_update
           where abb = abbrev) 
  where mtype = "SNP" 
     and zdb_id in 
       (select mrkr_zdb_id from marker)
     and zdb_id in 
       (select id from pre_marker);   

! echo "zmap_pub_pan_mark records updated."

-- the following is to load the validated gene-SNP relationship

create temp table tmp_mrkr2 (accNum varchar(50), name varchar(40), geneSym varchar(50), 
                            variation varchar(20), offset int, seq lvarchar
                           ) with no log;

load from snp2.unl 
  insert into tmp_mrkr2;

! echo "Inserted data from validated result into tmp table."

create temp table pre_marker_rel1 (accNum varchar(50), zsnpname varchar(40), gene varchar(50), 
                                   zdb_id varchar(50)
                                  ) with no log;

insert into pre_marker_rel1 (accNum,zsnpname,gene,zdb_id)
  select accNum,
         lower(name),
         geneSym,
         mrkr_zdb_id
    from marker, tmp_mrkr2
    where mrkr_abbrev = lower(name)
      and mrkr_zdb_id like "ZDB-SNP-%";

! echo "Inserted data into pre_marker_rel1 table."

create temp table pre_marker_rel2 (id varchar(50),
                                  type varchar(40),
                                  id1 varchar(50),
                                  id2 varchar(50)
                                 ) with no log;

insert into pre_marker_rel2 (id,type,id1,id2)
  select get_id('MREL'),
         'contains polymorphism',
         mrkr_zdb_id,
         zdb_id
    from marker, pre_marker_rel1
    where gene = mrkr_abbrev
      and mrkr_zdb_id like "ZDB-GENE-%"
      and not exists 
          (select 't' from marker_relationship
             where mrkr_zdb_id = mrel_mrkr_1_zdb_id 
               and zdb_id = mrel_mrkr_2_zdb_id
               and 'contains polymorphism' = mrel_type);
      
! echo "Inserted data into pre_marker_rel2 table." 

-- unload to 'loadedSNPGeneRel.unl' select * from pre_marker_rel2;

insert into zdb_active_data (zactvd_zdb_id)
  select id from pre_marker_rel2;  

insert into marker_relationship (mrel_zdb_id,mrel_type,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id)
  select * from pre_marker_rel2;     

! echo "Inserted data into marker_relationship table."

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
  select id,"ZDB-PUB-060208-1" from pre_marker_rel2; 

! echo "All current SNP data in marker_relationship table are attributed to ZDB-PUB-060208-1."

-- the following is to load inferred gene-SNP relationship

create temp table pre_marker_rel11 (id1 varchar(50), id2 varchar(50)) with no log;

insert into pre_marker_rel11 (id1,id2)
  select mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id from marker_relationship 
    where mrel_mrkr_1_zdb_id like 'ZDB-EST-%' 
      and mrel_mrkr_2_zdb_id like 'ZDB-SNP-%'
      and mrel_type = 'contains polymorphism';
      
create temp table pre_marker_rel22 (id1 varchar(50), id2 varchar(50)) with no log;

insert into pre_marker_rel22 (id1,id2)
  select mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id from marker_relationship 
    where mrel_mrkr_1_zdb_id like 'ZDB-GENE-%' 
      and mrel_mrkr_2_zdb_id like 'ZDB-EST-%'
      and mrel_type = 'gene encodes small segment';  
      
create temp table pre_marker_rel33 (id1 varchar(50)) with no log;

insert into pre_marker_rel33 (id1)
  select mrel_mrkr_1_zdb_id from marker_relationship
    where mrel_mrkr_1_zdb_id like 'ZDB-GENE-%'
      and mrel_mrkr_2_zdb_id like 'ZDB-SNP-%'
      and mrel_type = 'contains polymorphism';        

create temp table pre_marker_rel44 (id1 varchar(50), id2 varchar(50)) with no log;

insert into pre_marker_rel44 (id1,id2)
  select unique p2.id1, p1.id2
    from pre_marker_rel11 p1, pre_marker_rel22 p2, pre_marker_rel33 p3
    where p1.id1 = p2.id2
      and p2.id1 != p3.id1;
    
create temp table pre_marker_rel55 (id varchar(50),
                                   type varchar(40),
                                   id1 varchar(50),
                                   id2 varchar(50)
                                  ) with no log;

insert into pre_marker_rel55 (id,type,id1,id2)
  select get_id('MREL'), 'contains polymorphism', id1, id2
    from pre_marker_rel44
    where not exists 
          (select 't' from marker_relationship
             where id1 = mrel_mrkr_1_zdb_id 
               and id2 = mrel_mrkr_2_zdb_id
               and 'contains polymorphism' = mrel_type);

-- unload to 'loadedInferredGeneSNPRel.unl' select * from pre_marker_rel55;

insert into zdb_active_data (zactvd_zdb_id)
  select id from pre_marker_rel55;  

insert into marker_relationship (mrel_zdb_id,mrel_type,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id)
  select * from pre_marker_rel55;     

! echo "Inserted data into marker_relationship table."

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
  select id,"ZDB-PUB-060208-1" from pre_marker_rel55; 

! echo "All current SNP data in marker_relationship table are attributed to ZDB-PUB-060208-1."

commit work;
-- rollback work;
