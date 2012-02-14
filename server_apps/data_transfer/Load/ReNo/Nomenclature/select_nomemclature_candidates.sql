--
begin work;

-- priority
-- xpat = 128, si: = 64 zgc: = 32,  wu: = 4

create table nomenclature_candidate (
    nc_mrkr_zdb_id varchar(50),
    nc_mrkr_abbrev varchar(25),
    nc_acc_num varchar(20),
    nc_seq_type varchar(20),
    nc_seq_db  varchar (20),
    nc_priority integer
);


delete from nomenclature_candidate;

! echo "find unnamed x:genes hopefuly with expression patterns"
! echo "and no orthology; added 2007 Nov 6"
select distinct mrkr_zdb_id, mrkr_abbrev,0 priority
 from marker
 where mrkr_type[1,4] = 'GENE'
 and  mrkr_abbrev like "%:%"  --or mrkr_name like  "% like")
 and not exists (
 	select 1 from orthologue
 	where mrkr_zdb_id  = c_gene_id
 )
 into temp tmp_xpat_genes with no log;

! echo "bump expression pattern priority"
update tmp_xpat_genes set priority = priority + 128
 where exists (
  select 't' from expression_experiment
  where mrkr_zdb_id = xpatex_gene_zdb_id
);

! echo "bump zgc: OR si: priority"
update tmp_xpat_genes set priority = priority +
    case mrkr_abbrev[1,3]
        when 'zgc' then 32
        when 'si:' then 64
        else 4
    end
;

! echo "bump priority of si: genes that are also zgc:"
update tmp_xpat_genes set priority = priority + 32
 where mrkr_abbrev[1,4] <> 'zgc:'
 and exists (
    select 1 from data_alias
     where dalias_data_zdb_id = mrkr_zdb_id
     and   dalias_alias[1,4] = 'zgc:'
 )
;

! echo "bump priority of zgc: genes that are also si:"
update tmp_xpat_genes set priority = priority + 64
 where mrkr_abbrev[1,3] <> 'si:'
 and (exists (
    select 1 from data_alias
     where dalias_data_zdb_id = mrkr_zdb_id
     and   dalias_alias[1,3] = 'si:'
 ) or exists ( -- should not matter but just incase
    select 't' from marker_relationship
     where mrkr_zdb_id = mrel_mrkr_1_zdb_id
      and mrel_type == 'gene produces transcript'
));


! echo "find the longest protein associated with each gene"

select mrkr_zdb_id, g.mrkr_abbrev, gdbl.dblink_acc_num, gdbl.dblink_length,g.priority, fdb_db_name  db_name,fdbdt_data_type fdata_type
 from   tmp_xpat_genes g, db_link gdbl, foreign_db_contains, foreign_db,
         foreign_db_data_type
 where  g.mrkr_zdb_id  =  gdbl.dblink_linked_recid
 and    gdbl.dblink_fdbcont_zdb_id = fdbcont_zdb_id
 and    fdbdt_data_type = 'Polypeptide'
 and    fdb_db_name not in ('PBLAST')
 and    fdbcont_fdb_db_id = fdb_db_pk_id
 and    fdbcont_fdbdt_id = fdbdt_pk_id
 union
select g.mrkr_zdb_id, g.mrkr_abbrev, edbl.dblink_acc_num, edbl.dblink_length,g.priority, fdb_db_name,fdbdt_data_type
 from tmp_xpat_genes g, db_link edbl,
  foreign_db_contains, foreign_db, foreign_db_data_type, marker_relationship, marker e
 where  g.mrkr_zdb_id = mrel_mrkr_1_zdb_id
 and    e.mrkr_zdb_id = mrel_mrkr_2_zdb_id
 and    mrel_type = 'gene encodes small segment'
 and    e.mrkr_zdb_id  =  edbl.dblink_linked_recid
 and    edbl.dblink_fdbcont_zdb_id = fdbcont_zdb_id
 and    fdbdt_data_type = 'Polypeptide'
 and    fdb_db_name not in ('PBLAST')
 and    fdbcont_fdb_db_id = fdb_db_pk_id
 and    fdbcont_fdbdt_id = fdbdt_pk_id
 into temp tmp_can_pp with no log
;

-- drop shorter ones
select mrkr_zdb_id, max(dblink_length) dblink_length
  from tmp_can_pp
  where dblink_length is not null
  group by 1
  into temp tmp_long_pp with no log
;

delete from tmp_can_pp where exists (
    select 1 from tmp_long_pp
     where tmp_long_pp.mrkr_zdb_id   = tmp_can_pp.mrkr_zdb_id
     and   tmp_long_pp.dblink_length > tmp_can_pp.dblink_length
);

drop table tmp_long_pp;

! echo "keep refseq if multiple"
select mrkr_zdb_id
 from tmp_can_pp tcp1
 where db_name = 'RefSeq'
  and tcp1.dblink_length is not null
  and exists (
    select 1
     from tmp_can_pp tcp2
     where tcp2.mrkr_zdb_id = tcp1.mrkr_zdb_id
     and   (tcp2.db_name <> 'RefSeq'
     or    tcp2.dblink_length is null)
 )
 into temp tmp_nrs_pp with no log
;

delete from tmp_can_pp where exists (
    select 1 from tmp_nrs_pp
     where tmp_nrs_pp.mrkr_zdb_id = tmp_can_pp.mrkr_zdb_id
) and (db_name <> 'RefSeq'
  or   dblink_length is null)
;

drop table tmp_nrs_pp;

! echo "keep UniProt if multipule"
select mrkr_zdb_id
 from tmp_can_pp tcp1
 where db_name = 'UniProtKB'
 and tcp1.dblink_length is not null
  and exists (
    select 1
     from tmp_can_pp tcp2
     where tcp2.mrkr_zdb_id = tcp1.mrkr_zdb_id
     and   (tcp2.db_name <> 'UniProtKB'
     or    tcp2.dblink_length is null)
 )
 into temp tmp_nrs_pp with no log
;

delete from tmp_can_pp where exists (
    select 1 from tmp_nrs_pp
     where tmp_nrs_pp.mrkr_zdb_id = tmp_can_pp.mrkr_zdb_id
) and (db_name <> 'UniProtKB'
  or   dblink_length is null)
;

drop table tmp_nrs_pp;


{
select mrkr_zdb_id
 from tmp_can_pp
 group by 1
 having count(*) > 1
 into temp tmp_dups
;
select *
 from tmp_can_pp
 where mrkr_zdb_id in (
    select * from tmp_dups
)
order by mrkr_zdb_id;
drop table tmp_dups;
}

insert into nomenclature_candidate(
    nc_mrkr_zdb_id,
    nc_mrkr_abbrev,
    nc_acc_num,
    nc_seq_type,
    nc_seq_db,
    nc_priority
)  select
    mrkr_zdb_id,
    mrkr_abbrev,
    dblink_acc_num,
    fdata_type,
    db_name,
    priority
 from  tmp_can_pp
 where priority between 161  AND 223 -- TWIDDEL THIS

 --order by priority DESC
 ;
{
-- xpat+si 		= 192 
-- xpat+si+zgc  = 224  
-- zpat+zgc    == 160

}



---------------------------------------------------------------------------
-- #######################################################################
---------------------------------------------------------------------------

! echo "select_nomenclature_candidate.sql -> nomenclature_candidate_pp.unl"
unload to 'nomenclature_candidate_pp.unl'
 select  *
 --nc_mrkr_zdb_id, nc_priority
 from nomenclature_candidate
 where nc_seq_type = 'Polypeptide'
 --and nc_priority >= 160
 order by nc_priority,nc_acc_num,nc_mrkr_zdb_id,nc_seq_type desc
;

drop table nomenclature_candidate;
--
rollback work;
--commit work;
