-- load novel transcript

-- NEEDS to have ottdarP to more correctly classify type & status

begin work;

create table novel_transcript(
        nt_ottdarT varchar(20),
        nt_tname varchar(40),
        nt_tver int,
        nt_ttype  varchar(40),
        nt_ottdarP varchar(20),
        nt_ottdarG varchar(20),
        nt_gname  varchar(40),
        nt_gver int,
        nt_gtype  varchar(40),
        nt_gstatus  varchar(40),
        nt_clone_list varchar(255),
        primary key (nt_ottdarT) constraint pk_nt_ottdarT
) fragment by round robin in tbldbs1,tbldbs2,tbldbs3
extent size 3820518  next size 3820518
; -- using current filesystem size in k as SWAG

-- OTTDART00000001373|BUSM1-129I22.1-001|1|protein_coding|OTTDARP00000001211|OTTDARG00000001273|BUSM1-129I22.1|1|protein_coding|NOVEL|AL592495.1|





! echo "load the 'novel_transcript.unl' file"
load from 'novel_transcript.unl' insert into novel_transcript;

create index nt_nt_gtype_idx on novel_transcript(nt_gtype) in idxdbs3;
create index nt_nt_ttype_idx on novel_transcript(nt_ttype) in idxdbs2;

update statistics medium for table novel_transcript;

create table ottdart_length(ol_ottdarT varchar(20) primary key, ol_length integer);
! echo "load the 'ottdarT_length.unl' file"

load from 'ottdarT_length.unl' insert into ottdarT_length;

alter table novel_transcript add nt_length integer;
update novel_transcript set nt_length = (
	select ol_length from ottdart_length
	 where ol_ottdarT == nt_ottdart
);

drop table ottdart_length;

------------------------------------------
-- ! echo "load up a vega to zfin transcript type translation table"
-- delete from int_vegatype_tscripttype where ivtst_vega_type = 'artifact'; --
-- load from 'int_vega-tt_zfin-tt.unl' insert into int_vegatype_tscripttype;

-- should become permmant zfin table eventualy, but still some churn 

-- select * from int_vegatype_tscripttype;

create table tmp_vega_type_translation (
        tvtt_type varchar(11) not NULL,
        tvtt_term varchar(40) not NULL,
        tvtt_zfin_type integer default NULL,
        tvtt_zfin_status integer default NULL
);
! echo "load the 'vega_type_translation.unl' file"
load from 'vega_type_translation.unl' insert into tmp_vega_type_translation;

! echo "Check that all novel Vega's transcript and gene types can be translated"

select nt_ttype, count(*) howmany_ttype from novel_transcript where exists (
    select 1 from tmp_vega_type_translation
     where nt_ttype == tvtt_term
     and tvtt_type == 'transcript'
) group by 1 order by 2;

select nt_ttype missing_ttype from novel_transcript where not exists (
    select 't' from tmp_vega_type_translation
     where nt_ttype == tvtt_term
     and tvtt_type =='transcript' 
);

select nt_gtype, count(*) howmany_gtype from novel_transcript where exists (
    select 1 from tmp_vega_type_translation
     where nt_gtype == tvtt_term
     and tvtt_type == 'gene'
) group by 1 order by 2;
select nt_gtype missing_gtype from novel_transcript where not exists (
    select 1 from tmp_vega_type_translation
     where nt_gtype == tvtt_term
     and tvtt_type == 'gene'
);


! echo "get a snapshot of existing (zombie?) zfin transcript type and status before updating"
! echo " --> zfin_tscript_pre.unl"
unload to 'zfin_tscript_pre.unl'
 select nt_ottdarT, tscript_type_id, tscript_status_id
 from novel_transcript, transcript
  where tscript_load_id = nt_ottdarT
  order by 1
;
! echo "##########################################################"
! echo "update existing transcript types and status" 

--select count(*) type_different -- nulls?
-- from transcript, novel_transcript,tmp_vega_type_translation
-- where tscript_load_id = nt_ottdarT
--   and nt_ttype = tvtt_term
--   and tscript_type_id != tvtt_zfin_type
--;

--update transcript


! echo "Sanity check that the status of an existing (in this load)"
! echo "ottdarTs is not WITHDRAWN (should never happen)"
select tscript_load_id zt_ottdart,  tscript_mrkr_zdb_id zt_zdb
 from  transcript
 where tscript_load_id[1,8] = 'OTTDART0'
   and tscript_status_id == 1
   and  exists (
   	select 't' from novel_transcript
   	 where nt_ottdarT == tscript_load_id
)
into temp tmp_zombie_transcript with no log;

{
! echo "... but of course it does happen ... sigh.  what to do"
! echo "a WRONG solution  (see: resurect zombie transcript)"

Ha!
	With a sane approach to withdrawn transcripts in place (2012)
	this becomes trivial.
		
		o	remove the withdrawn flag 
		o	change the dblink fdb_type to prevega	
}
! echo "Back from  Sabadical?  flip W flag and change dblink type"
update transcript set tscript_status_id = NULL 
 where tscript_status_id == 1
   and exists (
	select 't' from tmp_zombie_transcript
	 where tscript_load_id == zt_ottdart  
	   and tscript_mrkr_zdb_id == zt_zdb
 );
 
update db_link set dblink_fdbcont_zdb_id = "ZDB-FDBCONT-050210-1"
 where dblink_fdbcont_zdb_id == "ZDB-FDBCONT-100114-1"   
   and exists (
	select 't' from tmp_zombie_transcript
	 where dblink_acc_num == zt_ottdart  
	   and dblink_linked_recid == zt_zdb
 );

! echo "If any are still WITHDRAWN remove them for now "
delete from novel_transcript where exists (
	select 't' from transcript
	 where tscript_load_id = nt_ottdarT
	   and tscript_status_id == 1
);

! echo "Returning zombies are :"
select * from tmp_zombie_transcript;
drop table tmp_zombie_transcript;
------------------------------------------------------------------------
! echo "default TYPE to 'ncRNA' if there is no protein id"
update transcript set tscript_type_id = 6
 where exists (
 	select 1 from novel_transcript
 	 where  tscript_load_id = nt_ottdarT
 	   and tscript_type_id != 6
 	   --and nt_ottdarP = "no translation"
 	   and nt_ottdarP is NULL
);

! echo "default TYPE to 'mRNA' if there is a protein id"
update transcript set tscript_type_id = 7
 where exists (
 	select 1 from novel_transcript
 	 where tscript_load_id = nt_ottdarT
 	   and tscript_type_id != 7
       and nt_ottdarP[1,8] = "OTTDARP0"
);

! echo "zero out the existing status if it is not 'FRAGMENTED' or 'AB' or 'Withdrawn'"
update transcript set tscript_status_id = NULL
 where exists (
 	select 1 from novel_transcript
 	 where tscript_load_id = nt_ottdarT
 	   and tscript_status_id != 17
 	   and tscript_status_id != 4
 	   and tscript_status_id != 1
);

! echo "set the status of existing ottdarTs missing from the current load to WITHDRAWN"
update transcript set tscript_status_id = 1
 where tscript_load_id[1,8] = 'OTTDART0'
   and not exists (
   	select 't' from novel_transcript
   	 where nt_ottdarT == tscript_load_id
);


! echo "set the status of existing ottdarTs on PACs to AB, if they are not"
! echo "should not ever change much"
update transcript set tscript_status_id = 4
 where tscript_load_id[1,8] = 'OTTDART0'
   and tscript_status_id != 4
   and exists (
    select 't' from marker_relationship
     where mrel_type == "clone contains transcript"
       and mrel_mrkr_2_zdb_id == tscript_load_id
       and mrel_mrkr_1_zdb_id[1,8] == 'ZDB-PAC-'
);

! echo "##########################################################"
-----------------------------------------------------------------
! echo "update zfin transcript TYPE from  vega transcript type"
! echo " existing NULLs first"
update transcript set tscript_type_id = (
        select tvtt_zfin_type
         from tmp_vega_type_translation,novel_transcript
         where tscript_load_id = nt_ottdarT
           and tvtt_type == 'transcript'
           and tvtt_term == nt_ttype
           and tvtt_zfin_type IS NOT NULL
           and tscript_type_id != tvtt_zfin_type
           and tscript_type_id IS NULL
)where exists (
        select 't' from tmp_vega_type_translation,novel_transcript
         where tscript_load_id = nt_ottdarT
           and tvtt_type == 'transcript'
           and tvtt_term == nt_ttype
           and tvtt_zfin_type IS NOT NULL
           and tscript_type_id != tvtt_zfin_type
           and tscript_type_id IS NULL
)
  and tscript_type_id IS NULL
;

! echo "update zfin transcript TYPE from  vega transcript type"
update transcript set tscript_type_id = (
        select tvtt_zfin_type
         from tmp_vega_type_translation,novel_transcript
         where tscript_load_id = nt_ottdarT
           and tvtt_type == 'transcript'
           and tvtt_term == nt_ttype
           and tvtt_zfin_type IS NOT NULL
           and tscript_type_id != tvtt_zfin_type
)where exists (
        select 't' from tmp_vega_type_translation,novel_transcript
         where tscript_load_id = nt_ottdarT
           and tvtt_type == 'transcript'
           and tvtt_term == nt_ttype
           and tvtt_zfin_type IS NOT NULL
           and tscript_type_id != tvtt_zfin_type
);

! echo "update zfin transcript STATUS from  vega transcript type"
! echo " existing NULLs first"
update transcript set tscript_status_id = (
        select tvtt_zfin_status
         from tmp_vega_type_translation, novel_transcript
         where tscript_load_id = nt_ottdarT
           and tvtt_type == 'transcript'
           and tvtt_term == nt_ttype
           and tvtt_zfin_status IS NOT NULL
           and tscript_status_id IS NULL
)where  exists (
        select 't' from tmp_vega_type_translation,novel_transcript
         where tscript_load_id = nt_ottdarT
           and tvtt_type == 'transcript'
           and tvtt_term == nt_ttype
           and tvtt_zfin_status IS NOT NULL
           and tscript_status_id IS NULL
)
  and tscript_status_id IS NULL
;
! echo "then the existing NOT NULLs"
update transcript set tscript_status_id = (
        select tvtt_zfin_status
         from tmp_vega_type_translation, novel_transcript
         where tscript_load_id = nt_ottdarT
           and tvtt_type == 'transcript'
           and tvtt_term == nt_ttype
           and tvtt_zfin_status IS NOT NULL
           and tscript_status_id IS NOT NULL
           and tscript_status_id != tvtt_zfin_status
           and tscript_status_id != 17 -- leave fragmented, we cant tell from here
           and tscript_status_id != 4  -- AB
           and tscript_status_id != 1  -- WITHDRAWN
)where  exists (
        select 't' from tmp_vega_type_translation,novel_transcript
         where tscript_load_id = nt_ottdarT
           and tvtt_type == 'transcript'
           and tvtt_term == nt_ttype
           and tvtt_zfin_status IS NOT NULL
           and tscript_status_id IS NOT NULL
           and tscript_status_id != tvtt_zfin_status
           and tscript_status_id != 17 -- leave fragmented, we cant tell from here
           and tscript_status_id != 4  -- AB
           and tscript_status_id != 1  -- WITHDRAWN
);

! echo "update zfin transcript TYPE from  vega gene type"
! echo "only V-gene at this time"
update transcript set tscript_type_id = (
        select tvtt_zfin_type
         from tmp_vega_type_translation,novel_transcript
         where tscript_load_id = nt_ottdarT
           and tvtt_type == 'gene'
           and tvtt_term == nt_gtype
           and tvtt_zfin_type IS NOT NULL
           and tscript_type_id != tvtt_zfin_type
)where exists (
        select 't' from tmp_vega_type_translation,novel_transcript
         where tscript_load_id = nt_ottdarT
           and tvtt_type == 'gene'
           and tvtt_term == nt_gtype
           and tvtt_zfin_type IS NOT NULL
           and tscript_type_id != tvtt_zfin_type
);

! echo "update zfin transcript STATUS from vega gene type"
! echo "variant if not something else"
! echo "first the existing NULL"

update transcript set tscript_status_id = (
        select tvtt_zfin_status
         from tmp_vega_type_translation, novel_transcript
         where tscript_load_id = nt_ottdarT
           and tvtt_type == 'gene'
           and tvtt_term == nt_gtype
           and tvtt_zfin_status IS NOT NULL
           and tscript_status_id IS NULL
)where  exists (
        select 't' from tmp_vega_type_translation,novel_transcript
         where tscript_load_id = nt_ottdarT
           and tvtt_type == 'gene'
           and tvtt_term == nt_gtype
           and tvtt_zfin_status IS NOT NULL
           and tscript_status_id IS NULL
);

! echo "then the existing NOT NULL"
update transcript set tscript_status_id = (
        select tvtt_zfin_status
         from tmp_vega_type_translation, novel_transcript
         where tscript_load_id = nt_ottdarT
           and tvtt_type == 'gene'
           and tvtt_term == nt_gtype
           and tvtt_zfin_status IS NOT NULL
           and tvtt_zfin_status IS NOT NULL
           and tscript_status_id != tvtt_zfin_status
           and tscript_status_id != 17 -- leave fragmented
           and tscript_status_id != 8  -- leave artifact
           and tscript_status_id != 7  -- leave NMD
           and tscript_status_id != 4  -- leave AB
           and tscript_status_id != 1  -- WITHDRAWN
)where  exists (
        select 't' from tmp_vega_type_translation,novel_transcript
         where tscript_load_id = nt_ottdarT
           and tvtt_type == 'gene'
           and tvtt_term == nt_gtype
           and tvtt_zfin_status IS NOT NULL
           and tvtt_zfin_status IS NOT NULL
           and tscript_status_id != tvtt_zfin_status
           and tscript_status_id != 17 -- leave fragmented
           and tscript_status_id != 8  -- leave artifact
           and tscript_status_id != 7  -- leave NMD
           and tscript_status_id != 4  -- leave AB
           and tscript_status_id != 1  -- WITHDRAWN
);

--------------------------------------------------------
--------------------------------------------------------
--------------------------------------------------------
! echo "##########################################################"
! echo "get a snapshot of zfin transcript type and status after updating existing"
! echo " --> zfin_tscript_post.unl"

unload to 'zfin_tscript_post.unl'
 select nt_ottdarT, tscript_type_id, tscript_status_id
 from novel_transcript, transcript
  where tscript_load_id == nt_ottdarT
  order by 1
;

--------------------------------------------------------
--------------------------------------------------------
--------------------------------------------------------


! echo "update the length of existing transcript that have changed in db link"

select  dblink_acc_num[1,25] tscript, dblink_length old_bp, nt_length new_bp,
	(nt_length - dblink_length) dif
 from novel_transcript, db_link
  where nt_ottdarT == dblink_acc_num
   and dblink_length != nt_length
   and dblink_length IS NOT NULL
   order by abs(nt_length - dblink_length)
;

! echo "catch any currently NULL ottdarT lengths first"
update db_link set dblink_length = (
	select nt_length from novel_transcript
	 where nt_ottdarT == dblink_acc_num
	   and dblink_length IS NULL
)where exists(
select 1 from novel_transcript
	where nt_ottdarT == dblink_acc_num
   and dblink_length IS NULL
);

! echo "update changed transcript lengths"
update db_link set dblink_length = (
	select nt_length from novel_transcript
	 where nt_ottdarT == dblink_acc_num
   and dblink_length != nt_length
   and dblink_length IS NOT NULL
)where exists(
select 1 from novel_transcript
	where nt_ottdarT == dblink_acc_num
   and dblink_length != nt_length
   and dblink_length IS NOT NULL
);

! echo "remove 'Novel' transcript which do exist in db_link"
-- this should make the script safe to run twice
delete from novel_transcript where exists(
        select 't' from db_link
         where dblink_acc_num = nt_ottdarT
);


! echo "check the Novel transcript ottdarT do not exist as db_links"
--! echo "############   DISABLED for testing #######################"
select distinct dblink_linked_recid, nt_ottdarT, mrel_mrkr_1_zdb_id related
 from db_link,novel_transcript,outer marker_relationship
 where dblink_acc_num = nt_ottdarT
   and dblink_linked_recid = mrel_mrkr_2_zdb_id
;

! echo ""


! echo "check which seem to be new alternative transcripts"
select distinct dblink_linked_recid, nt_ottdarT
 from db_link, novel_transcript
 where dblink_acc_num = nt_ottdarG
;

! echo "force names to lower case"
update novel_transcript set  nt_tname = lower(nt_tname);

! echo "check for colliding transcript names"
select mrkr_abbrev[1,20] tsname, dblink_acc_num[1,20] in_zfin, nt_ottdarT[1,20] novel,
	case  tscript_status_id when 1 then "WITHDRAWN" else "Fragmented?" end status
 from marker, novel_transcript, db_link, transcript
 where mrkr_abbrev = nt_tname
   and dblink_linked_recid = mrkr_zdb_id
   and dblink_acc_num[1,8] = 'OTTDART0'
   and mrkr_zdb_id = tscript_mrkr_zdb_id
;


! echo "check for novel transcript within a VEGA gene with lost Transcripts"
-- was unable to find value in comparing the lengths of withdrawn transcripts
-- so am updating changed transcript lengths a couple of statements up
select distinct mrkr_abbrev[1,20] symbol,
	--g.dblink_acc_num[1,20] common,
	t.dblink_acc_num[1,20] || " " ||   t.dblink_length inzfin,
	nt_ottdarT[1,20]  novel --|| " " || nt_length novel
 from marker gene, novel_transcript, db_link g, db_link t, transcript, marker_relationship
 where mrel_type == 'gene produces transcript'
   and mrel_mrkr_1_zdb_id == mrkr_zdb_id
   and mrel_mrkr_2_zdb_id == tscript_mrkr_zdb_id
   and g.dblink_linked_recid == mrkr_zdb_id
   and g.dblink_acc_num[1,8] == 'OTTDARG0'
   --and t.dblink_length is not NULL
   and g.dblink_acc_num == nt_ottdarG
   and tscript_status_id == 1
   and tscript_mrkr_zdb_id = t.dblink_linked_recid
   and t.dblink_acc_num[1,8] in ('OTTDART0','ZFINNUCL')
;


! echo "change colliding vega tscript-name to ottdarT"
-- hmmm may want to set the existing in zfin to OTTDART instead or as well.

update novel_transcript set nt_tname = lower(nt_ottdarT)
 where exists (
    select 't' from marker
 where mrkr_abbrev = nt_tname
);

! echo "check for duplicate transcript names"
select a.nt_tname[1,20],a.nt_ottdarT[1,20] ottdarT
 from novel_transcript a, novel_transcript b
 where a.nt_ottdarT  > b.nt_ottdarT
   and a.nt_tname = b.nt_tname
   into temp tmp_dup_tscript_name with no log
;

--select * from  tmp_dup_tscript_name;

! echo "change duplicate transcript names to ottdarT"
update novel_transcript set nt_tname = lower(nt_ottdarT)
 where exists (
    select 't' from tmp_dup_tscript_name
     where nt_ottdarT  = ottdarT
);

drop table tmp_dup_tscript_name;

! echo "recheck for dups after renaming"

select mrkr_abbrev[1,20],nt_tname[1,20],lower( nt_ottdarT[1,20])
 from marker, novel_transcript
 where mrkr_abbrev = nt_tname
;

! echo "drop dups after renaming"
delete from novel_transcript where exists(
        select 't' from marker
         where mrkr_abbrev = nt_tname
);

! echo "check incomming ottdarT are unique w.r.t themselves "
select nt_ottdarT,  count (*) howmany
 from novel_transcript
 group by nt_ottdarT having count(*) > 1
;

! echo "check incomming ottdarT are unique w.r.t existing transcripts "
select count (*) howmany
 from novel_transcript, transcript
 where tscript_load_id == nt_ottdarT
;


! echo "create new TSCRIPT ZDBIDs"
alter table novel_transcript add nt_zad varchar(50);
update novel_transcript set nt_zad = get_id('TSCRIPT');

insert into zdb_active_data select nt_zad from novel_transcript;

insert into marker(
        mrkr_zdb_id,
        mrkr_name,
        --mrkr_comments,
        mrkr_abbrev,
        mrkr_type,
        mrkr_owner
)
select nt_zad,nt_tname,nt_tname,'TSCRIPT','ZDB-PERS-001130-2'
 from novel_transcript
;


! echo "Translate Vega's transcript and gene types to"
! echo "ZFIN transcript TYPE and STATUS"

alter table novel_transcript add zfin_ttype integer default 0;
alter table novel_transcript add zfin_tstatus integer default NULL;

--! echo  "ambigous transcript type?"
--    select ivtst_tscriptT_type_id
--     from  int_vegatype_tscripttype, transcript_type,novel_transcript
--     where ivtst_vega_type = nt_ttype
--     group by 1 having count(*) > 1;



! echo "default to 'ncRNA' if there is no protein id"
update novel_transcript set zfin_ttype = 6
 --where nt_ottdarP = "no translation"
   where nt_ottdarP is NULL
   and zfin_ttype = 0
;

! echo "default to 'mRNA' if there is a protein id"
update novel_transcript set zfin_ttype = 7
 where nt_ottdarP[1,8] = "OTTDARP0"
   and zfin_ttype = 0
;

------------------------------------------------------------------------
--
--  the int_vegatype_tscripttype table is a train wreck
--  just tmp load a rational one
--
--! echo "if there is a more specific rule for a type that applies use it"
--update novel_transcript set zfin_ttype = (
--    select distinct ivtst_tscriptT_type_id
--     from  int_vegatype_tscripttype, transcript_type
--     where ivtst_vega_type = nt_ttype
--)
--where exists(
--        select 't' from int_vegatype_tscripttype
--         where nt_ttype = ivtst_vega_type
--);

--! echo "change spellings of ttypes for status table"
--! echo "'nonsense_mediated_decay' -> 'NMD'"
--update novel_transcript set nt_ttype = 'NMD'
-- where nt_ttype = 'nonsense_mediated_decay';
--
--! echo "'TEC' -> 'to be experimentally confirmed'"
--update novel_transcript set nt_ttype = 'to be experimentally confirmed'
-- where nt_ttype = 'TEC';
--
--! echo "'retained_intron' -> 'retained intron'"
--update novel_transcript set nt_ttype = 'retained intron'
-- where nt_ttype = 'retained_intron';
--
--! echo "change spellings of gtypes for status table"
--! echo "'polymorphic' -> 'variant'"
--update novel_transcript set nt_gtype = 'variant'
-- where nt_gtype[1,11]  = 'polymorphic';
--
--! echo "translate from vega transcript_type to ZFIN tscript STATUS when non-coding"
--update novel_transcript set zfin_tstatus = (
--    select tscriptS_pk_id
--     from  transcript_status
--     where tscriptS_status = nt_ttype
-- )
-- where zfin_ttype = 6 -- ncRNA
--   and zfin_tstatus is NULL -- justincase
--;
--
--! echo "translate from vega gene_type to ZFIN tscript STATUS"
--update novel_transcript set zfin_tstatus = (
--    select tscriptS_pk_id
--     from  transcript_status
--     where zfin_tstatus is NULL
--       and tscriptS_status = nt_gtype
--)
-- where nt_gtype != 'protein_coding'
--   and zfin_tstatus is NULL
--;
---------------------------------------------------------------------


! echo "Check that all novel Vega's transcript and gene types can be translated"

select nt_ttype, count(*) howmany_ttype from novel_transcript where exists (
    select 1 from tmp_vega_type_translation
     where nt_ttype = tvtt_term
     and tvtt_type = 'transcript'
) group by 1 order by 2
;
select nt_ttype missing_ttype from novel_transcript where not exists (
    select 1 from tmp_vega_type_translation
     where nt_ttype = tvtt_term
     and tvtt_type = 'transcript'
)
;


select nt_gtype, count(*) howmany_gtype from novel_transcript where exists (
    select 1 from tmp_vega_type_translation
     where nt_gtype = tvtt_term
     and tvtt_type = 'gene'
) group by 1 order by 2
;

select nt_gtype missing_gtype from novel_transcript where not exists (
    select 1 from tmp_vega_type_translation
     where nt_gtype = tvtt_term
     and tvtt_type = 'gene'
)
;
-----------------------------------------------------------------
! echo "translate vega transcript type  to zfin transcript TYPE"
update novel_transcript set zfin_ttype = (
        select tvtt_zfin_type
         from tmp_vega_type_translation
         where tvtt_type == 'transcript'
           and tvtt_term == nt_ttype
           and tvtt_zfin_type IS NOT NULL
)where exists (
        select 't' from tmp_vega_type_translation
         where tvtt_type == 'transcript'
           and tvtt_term == nt_ttype
           and tvtt_zfin_type IS NOT NULL
);

! echo "translate vega transcript type  to zfin transcript STATUS"
update novel_transcript set zfin_tstatus = (
        select tvtt_zfin_status
         from tmp_vega_type_translation
         where tvtt_type == 'transcript'
           and tvtt_term == nt_ttype
           and tvtt_zfin_status IS NOT NULL
)where  zfin_tstatus IS NULL
  and exists (
        select 't' from tmp_vega_type_translation
         where tvtt_type == 'transcript'
           and tvtt_term == nt_ttype
           and tvtt_zfin_status IS NOT NULL
);

! echo "translate vega gene type  to zfin transcript TYPE  [V-gene]"
-- just checking
--select count(*) ig_gene from novel_transcript where nt_gtype == 'IG_gene';
--select tvtt_type,tvtt_term,tvtt_zfin_type, count(*) howmany
-- from tmp_vega_type_translation, novel_transcript
-- where tvtt_type == 'gene'
--   and tvtt_term == nt_gtype
--   and tvtt_zfin_type IS NOT NULL
-- group by 1,2,3
--;

update novel_transcript set zfin_ttype = (
        select tvtt_zfin_type
         from tmp_vega_type_translation
         where tvtt_type == 'gene'
           and tvtt_term == nt_gtype
           and tvtt_zfin_type IS NOT NULL
)where exists (
        select 't' from tmp_vega_type_translation
         where tvtt_type == 'gene'
           and tvtt_term == nt_gtype
           and tvtt_zfin_type IS NOT NULL
);

! echo "translate vega gene type to zfin transcript STATUS [variant]"

update novel_transcript set zfin_tstatus = (
        select tvtt_zfin_status
         from tmp_vega_type_translation
         where tvtt_type = 'gene'
           and tvtt_term = nt_gtype
           and tvtt_zfin_status IS NOT NULL
)where  exists (
        select 't' from tmp_vega_type_translation
         where tvtt_type == 'gene'
           and tvtt_term == nt_gtype
           and tvtt_zfin_status IS NOT NULL
) -- don't clobber existing status
and zfin_tstatus IS NULL
;

--------------------------------------------------------------------------
! echo "check all types were translated check non have a wittness of zero"
select count(*) be_zero from novel_transcript where zfin_ttype = 0;

! echo "check that none are NULL"
select * from novel_transcript where zfin_ttype is NULL;

--! echo "what types are not defaults"
--select distinct * from novel_transcript where zfin_ttype not in (6,7);

--! echo what status are set"
--select sistinct * from novel_transcript where zfin_tstatus is not NULL;



! echo "create transcript records"
insert into transcript (
        tscript_mrkr_zdb_id,
        tscript_type_id,
        tscript_status_id,
        tscript_load_id
)
select nt_zad, zfin_ttype, zfin_tstatus, nt_ottdarT from novel_transcript
;

! echo "create db links to local sequence source"

--------------------------------------------------------
--------------------------------------------------------
--------------------------------------------------------
-- clear the deck
delete from zdb_active_data where exists(
    select 't' from novel_transcript, db_link
     where dblink_acc_num = nt_ottdarT
       and dblink_zdb_id = zactvd_zdb_id
 );
--------------------------------------------------------
--------------------------------------------------------
--------------------------------------------------------

! echo "make dblink ZDBIDs"

-- not sure which local dblink will be used
--    PREVEGA ZDB-FDBCONT-050210-1         -- YEP
-- or INTVEGA ZDB-FDBCONT-071128-1         -- NOPE
-- or maybe PUBRNA ZDB-FDBCONT-090601-10   -- NOPE

alter table novel_transcript add dblink_zad varchar(50);
update novel_transcript set dblink_zad = get_id('DBLINK');

insert into zdb_active_data select dblink_zad from novel_transcript;
insert into db_link (
    dblink_linked_recid,
    dblink_acc_num,
    dblink_zdb_id,
    dblink_fdbcont_zdb_id,
    dblink_info,
    dblink_length
)
select nt_zad, nt_ottdart, dblink_zad, 'ZDB-FDBCONT-050210-1','uncurated ' || TODAY, nt_length
 from novel_transcript
;

! echo "attribute db links"

insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
) select  dblink_zad, 'ZDB-PUB-030703-1' from novel_transcript -- vega pub
;

! echo "read file 'query_ottdarT.unl'"

create table query_acc (qa_ottdarT varchar(20) primary key)
fragment by round robin in tbldbs1,tbldbs2,tbldbs3
;
load from 'query_ottdarT.unl' insert into query_acc;

! echo "change existing transcripts dblinks to PREVEGA if they have changed versions" 
update db_link set dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-050210-1' -- PREVEGA
 where dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-060417-1' -- Vega_Trans
   and exists (select 1 from query_acc where qa_ottdarT == dblink_acc_num)
;

drop table query_acc;
----------------------------------------------------------
--- for testing
! echo "get a snapshot of transcript type and status after"
! echo "--> novel_tscript_post.unl"
unload to 'novel_tscript_post.unl'
 select nt_ottdarT, tscript_type_id,tscript_status_id
 from novel_transcript,transcript
  where nt_zad = tscript_mrkr_zdb_id
  order by 1
;
--------------------------------------------------------

drop table tmp_vega_type_translation;

drop table novel_transcript;

! echo "transaction terminated externaly"

--rollback work;
--commit work;
