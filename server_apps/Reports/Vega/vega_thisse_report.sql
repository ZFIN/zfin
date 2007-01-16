create temp table tmp_vega_cdna_link
  (
    vlink_mrkr_zdb_id     varchar(50),
    vlink_acc_num         varchar(40),
    vlink_acc_length      integer
  )
with no log;


create temp table tmp_vega_gene
  (
    vgene_mrkr_zdb_id     varchar(50)
  )
with no log;

create temp table tmp_vega_thisse_report
  (
    veth_significance  integer,
    veth_mrkr_abbrev   varchar(50),
    veth_acc_num       varchar(40),
    veth_length        integer
  )
with no log;

-- All Vega Genes without a Thisse expression experiment

insert into tmp_vega_gene
select distinct dblink_linked_recid
  from db_link, foreign_db_contains
 where fdbcont_zdb_id = dblink_fdbcont_zdb_id
   and fdbcont_fdb_db_name in ("VEGA", "PREVEGA","Vega_Trans")
   and not exists
     (
       select *
         from expression_experiment
        where xpatex_gene_zdb_id = dblink_linked_recid
          and xpatex_source_zdb_id in ("ZDB-PUB-040907-1","ZDB-PUB-010810-1","ZDB-PUB-051025-1")
     )
;

-- All GenBank cDNA links directly related to genes with a Vega link

insert into tmp_vega_cdna_link
select vgene_mrkr_zdb_id, dblink_acc_num, dblink_length
  from tmp_vega_gene, db_link, foreign_db_contains
 where vgene_mrkr_zdb_id = dblink_linked_recid
   and dblink_fdbcont_zdb_id = fdbcont_zdb_id
   and fdbcont_fdbdt_data_type = "cDNA"
   and fdbcont_fdb_db_name = "GenBank"
;


-- All GenBank cDNA links associated with a Vega gene through marker_relationship

insert into tmp_vega_cdna_link
select vgene_mrkr_zdb_id, dblink_acc_num, dblink_length
  from tmp_vega_gene, marker_relationship, db_link, foreign_db_contains
 where vgene_mrkr_zdb_id = mrel_mrkr_1_zdb_id
   and mrel_mrkr_2_zdb_id = dblink_linked_recid
   and dblink_fdbcont_zdb_id = fdbcont_zdb_id
   and fdbcont_fdbdt_data_type = "cDNA"
   and fdbcont_fdb_db_name = "GenBank"
;


-- Report longest length cDNA accession number when exists. Otherwise
-- report the VEGA accession number.

insert into tmp_vega_thisse_report
select '2', mrkr_abbrev, vega1.vlink_acc_num, vega1.vlink_acc_length
  from tmp_vega_cdna_link vega1, marker
 where mrkr_zdb_id = vega1.vlink_mrkr_zdb_id
   and mrkr_abbrev[1,4] != "zgc:"
   and vega1.vlink_acc_length =
     (
       select max(vega2.vlink_acc_length)
         from tmp_vega_cdna_link as vega2
        where vega1.vlink_mrkr_zdb_id = vega2.vlink_mrkr_zdb_id
     )
;

insert into tmp_vega_thisse_report
select '1', mrkr_abbrev, vega1.vlink_acc_num, vega1.vlink_acc_length
  from tmp_vega_cdna_link as vega1, marker
 where mrkr_zdb_id = vega1.vlink_mrkr_zdb_id
   and mrkr_abbrev[1,4] = "zgc:"
   and vega1.vlink_acc_length =
     (
       select max(vega2.vlink_acc_length)
         from tmp_vega_cdna_link as vega2
        where vega1.vlink_mrkr_zdb_id = vega2.vlink_mrkr_zdb_id
     )
;

insert into tmp_vega_thisse_report
select '3', mrkr_abbrev, dblink_acc_num, dblink_length
  from tmp_vega_gene, marker, db_link, foreign_db_contains
 where mrkr_zdb_id = vgene_mrkr_zdb_id
   and mrkr_zdb_id = dblink_linked_recid
   and dblink_fdbcont_zdb_id = fdbcont_zdb_id
   and fdbcont_fdb_db_name in ("VEGA", "PREVEGA","Vega_Trans")
   and not exists
     (
       select *
         from tmp_vega_cdna_link
        where vlink_mrkr_zdb_id = mrkr_zdb_id
     )
;

unload to vega_thisse_report.unl
select *
  from tmp_vega_thisse_report
 order by 1,2;