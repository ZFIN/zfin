{
	move_post_vega.sql

	called by the "vega_public" target

}

begin work;

create table ottdarT_ottdarG(
	tg_ottdarT varchar(20) primary key, 
	tg_ottdarG varchar(20),
	tg_ottdarP varchar(20) 
)
 fragment by round robin in tbldbs1, tbldbs2, tbldbs3
;

! echo "load ottdarT_ottdarG.unl"
load from 'ottdarT_ottdarG_ottdarP.unl' insert into ottdarT_ottdarG;

create index ottdarT_ottdarG_gidx on ottdarT_ottdarG(tg_ottdarG) in idxdbs3;

update statistics high for table ottdarT_ottdarG;

! echo "double check withdrawn transcripts are not in the new load"
select count(*) bezero
 from transcript, ottdarT_ottdarG
 where tscript_status_id == 1
   and tscript_load_id == tg_ottdarT
;


! echo "drop vega Gene links to withdrawn transcripts (if any still exist)"
delete from zdb_active_data where exists(
	select 't' from db_link, transcript
	  where dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-040412-14' -- vega_gene
	    and dblink_linked_recid == tscript_mrkr_zdb_id
	    and tscript_status_id == 1 -- withdrawn
	    and dblink_acc_num[1,8] == 'OTTDARG0'
	    and dblink_zdb_id == zactvd_zdb_id
);


! echo "drop protein links to withdrawn transcripts (may already be done)"
delete from zdb_active_data where exists(
	select 't' from db_link, transcript
	  where dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-090929-9' -- vega protein
	    and dblink_linked_recid == tscript_mrkr_zdb_id
	    and tscript_status_id == 1 -- withdrawn
	    and dblink_acc_num[1,8] == 'OTTDARP0'
	    and dblink_zdb_id == zactvd_zdb_id
);


! echo "update the new PREVEGA to Vega_Trans"
update db_link set dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-060417-1' -- Vega_Trans
 where dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-050210-1' -- PREVEGA
   and exists (
   	select 't' from ottdarT_ottdarG
   	 where dblink_acc_num == tg_ottdarT
);


! echo "check if any PREVEGA not converted"
select count(*) bezero_prevega
 from db_link
 where dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-050210-1' -- prevega
;

! echo "check for current ottdarTs attached to more than one transcript"

select  tg_ottdarT,  count(*) dup_tscript
 from db_link,  ottdarT_ottdarG
 where dblink_acc_num = tg_ottdarT
   --and dblink_linked_recid == tscript_mrkr_zdb_id
   and dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-050210-1','ZDB-FDBCONT-060417-1')
group by 1 having count(*) > 1
;

! echo "check for any ottdarTs attached to more than one transcript"

select  dblink_acc_num,  count(*) dup_tscript
 from db_link
 where dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-050210-1','ZDB-FDBCONT-060417-1')
group by 1 having count(*) > 1
;


! echo "project genes for incomming transcripts for dups"

select tg_ottdarT, count(distinct mrel_mrkr_1_zdb_id) g
 from ottdarT_ottdarG, transcript, marker_relationship
 where tg_ottdarT == tscript_load_id
   and tscript_mrkr_zdb_id == mrel_mrkr_2_zdb_id
   and mrel_type = 'gene produces transcript'
   group by 1
   having count(mrel_mrkr_1_zdb_id) > 1
;

! echo "find associated gene for transcript ottdarTs"

alter table ottdarT_ottdarG add tscript_id varchar(50);

update ottdarT_ottdarG set tscript_id = (
	select distinct mrel_mrkr_1_zdb_id
	 from transcript, marker_relationship
	 where tg_ottdarT == tscript_load_id
       and mrel_type = 'gene produces transcript'
       and tscript_mrkr_zdb_id = mrel_mrkr_2_zdb_id
);
update statistics high for table ottdarT_ottdarG;

! echo "WARN if vega gene associated with more than one ZFIN gene"

select distinct tg_ottdarG,  tscript_id
 from ottdarT_ottdarG
 group by 1,2
 into temp tmp_ogg with no log
;

select tg_ottdarG, count(tscript_id) bezero
 from tmp_ogg group by 1 having count(*) > 1;

select a.tg_ottdarG, a.tscript_id , b.tscript_id
 from tmp_ogg a ,tmp_ogg b
 where a.tg_ottdarG == b.tg_ottdarG
 and a.tscript_id > b.tscript_id
;

drop table tmp_ogg;

! echo "find associated tscript_zdbids for transcript ottdarTs"
-- clobbering the gene_ids that were there temporarily
update ottdarT_ottdarG set tscript_id = (
	select tscript_mrkr_zdb_id
	 from transcript
	 where tg_ottdarT == tscript_load_id
);
update statistics high for table ottdarT_ottdarG;


! echo "drop existing ottdarG that already have a Vega gene Link"
-- bad to do first as may be wrong when existing gene gets new alt tscript
delete from ottdarT_ottdarG where exists (
        select 't' from db_link
        where dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-040412-14' -- vega_gene
          and dblink_acc_num = tg_ottdarG
);
update statistics high for table ottdarT_ottdarG;


! echo "make the rest into new vega links"
alter table ottdarT_ottdarG add zad varchar(50);
update ottdarT_ottdarG set zad = get_id('DBLINK');
insert into zdb_active_data select zad from ottdarT_ottdarG;

insert into db_link  (
    dblink_linked_recid,
    dblink_acc_num,
    dblink_info,
    dblink_zdb_id,
--    dblink_acc_num_display,
--    dblink_length integer,
    dblink_fdbcont_zdb_id
  )
select tscript_id,tg_ottdarG,"uncurated " ||TODAY, zad,'ZDB-FDBCONT-040412-14'
 from ottdarT_ottdarG where tscript_id is not NULL
;

! echo "attribute vega links"

insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
) select zad, 'ZDB-PUB-030703-1' -- vega pub
 from ottdarT_ottdarG
 where tscript_id is not NULL
;


! echo "Are any transcripts still unattached to a GENE?"
select * from ottdarT_ottdarG  where tscript_id is NULL;

! echo "write out novel_vega_genes_generated"
unload to 'novel_vega_genes_generated.unl'
select tg_ottdarG, tscript_id, tg_ottdarP 
from ottdarT_ottdarG
where tg_ottdarP != "no_translation";


-- 'ZDB-FDBCONT-060417-1' -- vega_trans
-- 'ZDB-FDBCONT-050210-1' -- PREVEGA
------------------------------------------------------------------------
! echo "###############################################################"
! echo " update ottdarP "
! echo "###############################################################"
------------------------------------------------------------------------

drop table ottdarT_ottdarG;

-- transaction terminated externally
