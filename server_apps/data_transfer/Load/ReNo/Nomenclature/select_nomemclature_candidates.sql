--
begin work;

-- priority
-- xpat = 128, si: = 64 zgc: = 32,  wu: = 4

create table nomenclature_candidate (
    nc_mrkr_zdb_id text,
    nc_mrkr_abbrev varchar(25),
    nc_acc_num varchar(20),
    nc_seq_type varchar(20),
    nc_seq_db  varchar (20),
    nc_priority integer
);


delete from nomenclature_candidate;

\echo 'find unnamed x:genes hopefuly with expression patterns'
\echo 'and no orthology; added 2007 Nov 6'

create temp table tmp_xpat_genes (mrkr_zdb_id text, mrkr_abbrev text, priority int);

insert into tmp_xpat_genes (mrkr_zdb_id, mrkr_abbrev, priority)
select distinct mrkr_zdb_id, mrkr_abbrev,0 priority
 from marker
 where substring(mrkr_type,1,4) = 'GENE'
 and  mrkr_abbrev like 'si:%'  --or mrkr_name like  '% like')
 and ( exists (Select 'x' from db_link, marker_relationship
	   	  	  where get_date_from_id(dblink_Zdb_id, 'YYYYMMDD') > '20160622'
			  and dblink_linked_recid = mrel_mrkr_2_zdb_id
			  and mrel_mrkr_1_zdb_id = mrkr_zdb_id
			  and mrel_type = 'gene produces transcript'
			  and dblink_Acc_num like 'OTTDART%'))
 and not exists (
 	select 1 from ortholog
 	where mrkr_zdb_id  = ortho_zebrafish_gene_zdb_id
 )
 and not exists ( select 'x' from feature_marker_Relationship, feature
     	 	  where fmrel_ftr_zdb_id = feature_zdb_id
		  and fmrel_mrkr_zdb_id = mrkr_zdb_id
		  and feature_abbrev like 'sa%');

\echo 'bump expression pattern priority'
update tmp_xpat_genes set priority = priority + 128
 where exists (
  select 't' from expression_experiment2
  where mrkr_zdb_id = xpatex_gene_zdb_id
);

\echo 'bump zgc: OR si: priority'
update tmp_xpat_genes set priority = priority +
    case substring(mrkr_abbrev,1,3)
        when 'zgc' then 32
        when 'si:' then 64
        else 4
    end
;

\echo 'bump priority of si: genes that are also zgc:'
update tmp_xpat_genes set priority = priority + 32
 where substring(mrkr_abbrev,1,4) <> 'zgc:'
 and exists (
    select 1 from data_alias
     where dalias_data_zdb_id = mrkr_zdb_id
     and   substring(dalias_alias,1,4) = 'zgc:'
 )
;

\echo 'bump priority of zgc: genes that are also si:'
update tmp_xpat_genes set priority = priority + 64
 where substring(mrkr_abbrev,1,3) <> 'si:'
 and (exists (
    select 1 from data_alias
     where dalias_data_zdb_id = mrkr_zdb_id
     and   substring(dalias_alias,1,3) = 'si:'
 ) or exists ( -- should not matter but just incase
    select 't' from marker_relationship
     where mrkr_zdb_id = mrel_mrkr_1_zdb_id
      and mrel_type = 'gene produces transcript'
));

create temp table tmp_genesRemoved (mrkr_zdb_id text);

insert into tmp_genesRemoved(mrkr_zdb_id)
  select mrkr_zdb_id
    from tmp_xpat_genes
  where not exists (Select 'x' from db_link,foreign_db_contains, foreign_db,
            	   	   foreign_db_data_type
  	    	   	   where dblink_linked_recid = mrkr_zdb_id
			   and dblink_fdbcont_zdb_id = fdbcont_zdb_id
 			   and    fdbdt_data_type = 'Polypeptide'
 			   and    fdb_db_name not in ('PBLAST')
 			   and    fdbcont_fdb_db_id = fdb_db_pk_id
 			   and    fdbcont_fdbdt_id = fdbdt_pk_id);

\copy (select mrkr_zdb_id from tmp_genesRemoved) to 'genesRemoved.txt';

\echo 'find the longest protein associated with each gene'

create temp table tmp_can_pp (mrkr_zdb_id text,
       	    	  	      mrkr_abbrev text,
			      dblink_acc_num text,
			      dblink_length int,
			      priority int, 
			      db_name varchar(50),
			      fdata_type varchar(40));

insert into tmp_can_pp(mrkr_zdb_id, mrkr_abbrev, dblink_acc_num, dblink_length, priority, db_name,fdata_type)
select mrkr_zdb_id, g.mrkr_abbrev, gdbl.dblink_acc_num, gdbl.dblink_length,g.priority, fdb_db_name,fdbdt_data_type
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
union
 select mrkr_zdb_id, mrkr_abbrev, dblink_acc_num, dblink_length,priority, fdb_db_name,fdbdt_data_type
from tmp_xpat_genes , db_link, marker_relationship, foreign_db_Contains, foreign_db, foreign_db_data_type
  where mrel_mrkr_2_zdb_id = dblink_linked_recid
  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
  and fdbcont_fdb_db_id = fdb_db_pk_id
  and fdbcont_fdbdt_id = fdbdt_pk_id
and mrkr_zdb_id = mrel_mrkr_1_zdb_id
and dblink_acc_num like 'OTTDARP%'
;


create temp table tmp_dups2 (counter int, id text);

insert into tmp_dups2 (counter, id)
select count(*) as counter, mrkr_zdb_id as id
  from tmp_can_pp
 where dblink_length is null
 and dblink_acc_num like 'OTTDARP%'
 group by mrkr_zdb_id
 having count(*) > 1;

delete from tmp_can_pp
 where exists (Select 'x' from tmp_dups2
       	      	      where id = mrkr_zdb_id);

-- drop shorter ones

create temp table tmp_long_pp (mrkr_zdb_id text, dblink_length int);

insert into tmp_long_pp(mrkr_zdb_id, dblink_length)
select mrkr_zdb_id, max(dblink_length) dblink_length
  from tmp_can_pp
  where dblink_length is not null
  group by 1
;

delete from tmp_can_pp where exists (
    select 1 from tmp_long_pp
     where tmp_long_pp.mrkr_zdb_id   = tmp_can_pp.mrkr_zdb_id
     and   tmp_long_pp.dblink_length > tmp_can_pp.dblink_length
);

drop table tmp_long_pp;

\echo 'keep refseq if multiple'

create temp table tmp_nrs_pp (mrkr_zdb_id text);

insert into tmp_nrs_pp(mrkr_zdb_id)
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
;

delete from tmp_can_pp where exists (
    select 1 from tmp_nrs_pp
     where tmp_nrs_pp.mrkr_zdb_id = tmp_can_pp.mrkr_zdb_id
) and ((db_name <> 'RefSeq' and db_name <> 'Ensembl Protein')
  or   dblink_length is null)
;


drop table tmp_nrs_pp;

create temp table tmp_nrs_pp (mrkr_zdb_id text);

\echo 'keep UniProt if multipule'
insert into tmp_nrs_pp(mrkr_zdb_id)
select mrkr_zdb_id
 from tmp_can_pp tcp1
 where db_name = 'UniProtKB'
 and tcp1.dblink_length is not null
  and exists (
    select 1
     from tmp_can_pp tcp2
     where tcp2.mrkr_zdb_id = tcp1.mrkr_zdb_id
     and   (tcp2.db_name <> 'UniProtKB' and db_name <> 'Ensembl Protein'
     or    tcp2.dblink_length is null)
 )
;

delete from tmp_can_pp where exists (
    select 1 from tmp_nrs_pp
     where tmp_nrs_pp.mrkr_zdb_id = tmp_can_pp.mrkr_zdb_id
) and (db_name <> 'UniProtKB' and db_name <> 'Ensembl Protein'
  or   dblink_length is null)
;


drop table tmp_nrs_pp;


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
 from  tmp_can_pp;

 --order by priority DESC


---------------------------------------------------------------------------
-- #######################################################################
---------------------------------------------------------------------------

\copy (select * from nomenclature_candidate where nc_seq_type = 'Polypeptide' order  by nc_priority,nc_acc_num,nc_mrkr_zdb_id,nc_seq_type desc) to 'nomenclature_candidate_pp.unl';

drop table nomenclature_candidate;

\echo 'this roll back is expected';

--
rollback work;
