
-- sanity check ottdarT post ReNo

begin work;

create table tgp(
        ottdarT varchar(20) primary key, ottdarG varchar(20), ottdarP varchar(20)
) fragment by round robin in tbldbs1,tbldbs2,tbldbs3 extent size 1610310
;

! echo "load file 'ottdarT_ottdarG_ottdarP.unl'"
load from 'ottdarT_ottdarG_ottdarP.unl' insert into tgp;
create index tgp_ottdarG_idx on tgp(ottdarG) in idxdbs3;
create index tgp_ottdarP_idx on tgp(ottdarP) in idxdbs2;

update statistics for table tgp;

! echo "how many transcript in this load?"
select count(*) howmany from tgp;

! echo "are all transcript in zfin?"
select count(*) bezero from tgp where not exists (
	select 't' from transcript where tscript_load_id == ottdarT
);

! echo "are all transcripts associated with a ZFIN gene"
select ottdarT,tscript_mrkr_zdb_id[1,25] geneless
 from tgp join transcript on tscript_load_id == ottdarT
  where not exists (
	select 't'
	 from marker_relationship
	 where mrel_type == "gene produces transcript"
	   and mrel_mrkr_2_zdb_id == tscript_mrkr_zdb_id
);

! echo "are all alt_transcripts associated with same zfin gene"
--! echo "           note only top N are taken! "
--! echo "if list does not go till  zfin_genes = 1 then EDIT ME"

-- if ottdarGs are loaded yet ...

select ottdarG ottG, count(distinct mrel_mrkr_1_zdb_id) zfin_genes
 from tgp
  join transcript          on tscript_load_id == ottdarT
  join marker_relationship on mrel_mrkr_2_zdb_id == tscript_mrkr_zdb_id
 where mrel_type == "gene produces transcript"
 group by ottdarG
 having count(mrel_mrkr_1_zdb_id) > 1
  into temp tmp_split_ottdarG with no log
;
! echo "reduces to: is an orrdarG associated with more than one zfin gene"
select * from tmp_split_ottdarG where  zfin_genes != 1;

! echo "recover which genes & transcripts as the ottdarGs may not even be public yet"

-- select ottG, ottdarT
--  from tmp_split_ottdarG
--   join tgp on ottdarG == ottG
--  where zfin_genes != 1
--  order by 1,2
-- ;

select ottG, mrel_mrkr_1_zdb_id[1,25], tgp.ottdarT
 from tmp_split_ottdarG
  join tgp on ottdarG == ottG
  join transcript on tscript_load_id == ottdarT
  join marker_relationship on mrel_mrkr_2_zdb_id == tscript_mrkr_zdb_id
  where mrel_type == "gene produces transcript"
    and zfin_genes != 1
 order by 1,2
;

drop table tmp_split_ottdarG;



! echo "are any transcripts in the current assembly withdrawn?"
select tscripts_status status, count(*) howmany
 from tgp, transcript, transcript_status
 where tscript_load_id ==  ottdarT
   and tscript_status_id == tscripts_pk_id
 group by 1 order by 2
;

! echo "what db links do Current transcripts have?"

select fdb_db_name, count(*) howmany
 from tgp, db_link,foreign_db_contains,foreign_db
 where ottdarT == dblink_acc_num
   and dblink_fdbcont_zdb_id == fdbcont_zdb_id
   and fdbcont_fdb_db_id ==  fdb_db_pk_id
 group by 1 order by 2
;

! echo "what db links do ALL transcripts have? (inc. withdrawn)"
select fdb_db_name, count(*) howmany
 from db_link,transcript ,foreign_db_contains,foreign_db
 where dblink_acc_num == tscript_load_id
   and dblink_fdbcont_zdb_id == fdbcont_zdb_id
   and fdbcont_fdb_db_id ==  fdb_db_pk_id
  group by 1 order by 2
;


! echo "what db links do withdrawn transcripts have?"
select fdb_db_name, count(*) howmany
 from db_link, transcript ,foreign_db_contains,foreign_db
 where dblink_acc_num == tscript_load_id
   and dblink_fdbcont_zdb_id == fdbcont_zdb_id
   and fdbcont_fdb_db_id ==  fdb_db_pk_id
   and tscript_status_id == 1
  group by 1 order by 2
;


! echo "Are transcript sets for zfin genes on same LG"
! echo "this method is not guarenteed correct until the gff3 file is loaded into zfin"
! echo "... which may not be available when we first need it"

select first 50 mrel_mrkr_1_zdb_id[1,25] gene, count( distinct gff_seqname ) num_of_LGs
 from marker_relationship
  join transcript on mrel_mrkr_2_zdb_id == tscript_mrkr_zdb_id
  join gff3 on tscript_load_id == gff_id
 where mrel_type == 'gene produces transcript'
   and gff_source == "vega"
   and gff_seqname != "AB"
   group by 1
   order by 2 desc
;

drop table tgp;


--
rollback work;

--commit work;
