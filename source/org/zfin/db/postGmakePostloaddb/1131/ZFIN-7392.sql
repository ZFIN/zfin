--liquibase formatted sql
--changeset cmpich:ZFIN-7392

select count(distinct dblink_linked_recid) "1. Total number of transcripts in ZFIN with Vega ID (OTTDART)"
from db_Link
where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-060417-1'
  and dblink_linked_recid like 'ZDB-TSCRIPT-%'
;

select dblink_linked_recid "1. TS in ZFIN with Vega ID (OTTDART)"
from db_Link
where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-060417-1'
  and dblink_linked_recid like 'ZDB-TSCRIPT-%'
limit 10
;

select count(distinct d.dblink_linked_recid) "1.a Within the above: Tot number of TS with ENSEMBL and ENSDART"
from db_Link as d
where d.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-060417-1'
  and d.dblink_linked_recid like 'ZDB-TSCRIPT-%'
  and exists(
        select *
        from db_link as db
        where db.dblink_linked_recid = d.dblink_linked_recid
          and db.dblink_acc_num like 'ENSDART%'
    )
;

select d.dblink_linked_recid "1.a Within the above: TS with ENSEMBL ID ENSDART"
from db_Link as d
where d.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-060417-1'
  and d.dblink_linked_recid like 'ZDB-TSCRIPT-%'
  and exists(
        select *
        from db_link as db
        where db.dblink_linked_recid = d.dblink_linked_recid
          and db.dblink_acc_num like 'ENSDART%'
    )
limit 10
;

select count(distinct d.dblink_linked_recid) "1.b Tot number of TS withdrawn and no enxembl link"
from db_Link as d,
     transcript
where d.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-060417-1'
  and d.dblink_linked_recid like 'ZDB-TSCRIPT-%'
  and tscript_mrkr_zdb_id = d.dblink_linked_recid
  and tscript_status_id = 1
  and not exists(
        select *
        from db_link as db
        where db.dblink_linked_recid = d.dblink_linked_recid
          and db.dblink_acc_num like 'ENSDART%'
    )
;

select distinct d.dblink_linked_recid "1.b TS withdrawn and no enxembl link"
from db_Link as d,
     transcript
where d.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-060417-1'
  and d.dblink_linked_recid like 'ZDB-TSCRIPT-%'
  and tscript_mrkr_zdb_id = d.dblink_linked_recid
  and tscript_status_id = 1
  and not exists(
        select *
        from db_link as db
        where db.dblink_linked_recid = d.dblink_linked_recid
          and db.dblink_acc_num like 'ENSDART%'
    )
limit 10
;


select count(distinct dblink_linked_recid) "1.c Tot number of TS with strain AB"
from db_Link,
     transcript,
     clone,
     probe_library,
     genotype,
     marker_relationship
where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-060417-1'
  and dblink_linked_recid like 'ZDB-TSCRIPT-%'
  and dblink_linked_recid = tscript_mrkr_zdb_id
  and mrel_mrkr_2_zdb_id = tscript_mrkr_zdb_id
  and mrel_mrkr_1_zdb_id = clone_mrkr_zdb_id
  and clone_probelib_zdb_id = probelib_zdb_id
  and probelib_strain_zdb_id = geno_zdb_id
  and geno_handle = 'AB'
;

select distinct dblink_linked_recid  "1.c TS with strain AB"
from db_Link,
     transcript,
     clone,
     probe_library,
     genotype,
     marker_relationship
where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-060417-1'
  and dblink_linked_recid like 'ZDB-TSCRIPT-%'
  and dblink_linked_recid = tscript_mrkr_zdb_id
  and mrel_mrkr_2_zdb_id = tscript_mrkr_zdb_id
  and mrel_mrkr_1_zdb_id = clone_mrkr_zdb_id
  and clone_probelib_zdb_id = probelib_zdb_id
  and probelib_strain_zdb_id = geno_zdb_id
  and geno_handle = 'AB'
limit 10
;


select mrkr_zdb_id, tscript_mrkr_zdb_id "2. all gene transcripts with ENSDART and no ENSDARG"
from marker,
     transcript,
     marker_relationship,
     db_link as ensdart
where mrel_mrkr_1_zdb_id = mrkr_zdb_id
  and mrel_mrkr_2_zdb_id = tscript_mrkr_zdb_id
  and mrel_type = 'gene produces transcript'
  and ensdart.dblink_linked_recid = tscript_mrkr_zdb_id
  and ensdart.dblink_acc_num like 'ENSDART%'
  and ensdart.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-110301-1'
    except
select mrkr_zdb_id, tscript_mrkr_zdb_id
from marker,
     transcript,
     marker_relationship,
     db_link as ensdarg,
     db_link as ensdart
where mrel_mrkr_1_zdb_id = mrkr_zdb_id
  and mrel_mrkr_2_zdb_id = tscript_mrkr_zdb_id
  and mrel_type = 'gene produces transcript'
  and ensdarg.dblink_linked_recid = mrkr_zdb_id
  and ensdarg.dblink_acc_num like 'ENSDARG%'
  and ensdarg.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
  and ensdart.dblink_linked_recid = tscript_mrkr_zdb_id
  and ensdart.dblink_acc_num like 'ENSDART%'
  and ensdart.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-110301-1'
;

select count(*) "3. No of TS withe ENSDART and pub ZDB-PUB-190221-12"
from marker,
     transcript,
     marker_relationship,
     db_link as ensdart
where mrel_mrkr_1_zdb_id = mrkr_zdb_id
  and mrel_mrkr_2_zdb_id = tscript_mrkr_zdb_id
  and mrel_type = 'gene produces transcript'
  and ensdart.dblink_linked_recid = tscript_mrkr_zdb_id
  and ensdart.dblink_acc_num like 'ENSDART%'
  and ensdart.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-110301-1'
  and exists(select *
             from record_attribution
             where recattrib_data_zdb_id = ensdart.dblink_zdb_id
               and recattrib_source_zdb_id = 'ZDB-PUB-190221-12')
;

select mrkr_zdb_id, tscript_mrkr_zdb_id "3. TS withe ENSDART and pub ZDB-PUB-190221-12"
from marker,
     transcript,
     marker_relationship,
     db_link as ensdart
where mrel_mrkr_1_zdb_id = mrkr_zdb_id
  and mrel_mrkr_2_zdb_id = tscript_mrkr_zdb_id
  and mrel_type = 'gene produces transcript'
  and ensdart.dblink_linked_recid = tscript_mrkr_zdb_id
  and ensdart.dblink_acc_num like 'ENSDART%'
  and ensdart.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-110301-1'
  and exists(select *
             from record_attribution
             where recattrib_data_zdb_id = ensdart.dblink_zdb_id
               and recattrib_source_zdb_id = 'ZDB-PUB-190221-12')
limit 10
;

select count(*) "3.b No of gene with TS with ENSDARG and pub ZDB-PUB-190221-12"
from marker,
     db_link as ensdarg
where ensdarg.dblink_linked_recid = mrkr_zdb_id
  and ensdarg.dblink_acc_num like 'ENSDARG%'
  and ensdarg.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
  and exists(select *
             from record_attribution
             where recattrib_data_zdb_id = ensdarg.dblink_zdb_id
               and recattrib_source_zdb_id = 'ZDB-PUB-190221-12')
;

select mrkr_zdb_id "3.b genes with TS with ENSDARG and pub ZDB-PUB-190221-12"
from marker,
     db_link as ensdarg
where ensdarg.dblink_linked_recid = mrkr_zdb_id
  and ensdarg.dblink_acc_num like 'ENSDARG%'
  and ensdarg.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
  and exists(select *
             from record_attribution
             where recattrib_data_zdb_id = ensdarg.dblink_zdb_id
               and recattrib_source_zdb_id = 'ZDB-PUB-190221-12')
limit 10
;
