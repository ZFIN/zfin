-- update transcript names

begin work;

create table ottdarT_abbrev (oa_ottdarT varchar(20) primary key, oa_abbrev varchar(40))
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 100000 next size 100000
;

load from 'vega_transcript_names.tsv' delimiter "	"
 insert into ottdarT_abbrev
;


create index ottdarT_abbrev_oa_abbrev_idx on ottdarT_abbrev(oa_abbrev) in idxdbs3;

update statistics high for table ottdarT_abbrev;

! echo "drop the abbrevs we already agree on"
delete from ottdarT_abbrev where exists (
	select 't' from marker join transcript on mrkr_zdb_id == tscript_mrkr_zdb_id
	 where tscript_load_id == oa_ottdarT
	   and mrkr_abbrev == oa_abbrev
);

update statistics high for table ottdarT_abbrev;

! echo "How many remain to get new abbrevs"
select count(*)howmany from ottdarT_abbrev;


! echo "Any new names in conflict with assigned abbrev become ottdarTs"
! echo "might want to revisit this choice"

update ottdarT_abbrev set oa_abbrev = lower(oa_ottdarT) where exists(
	select 't' from marker join transcript on mrkr_zdb_id == tscript_mrkr_zdb_id
	 where mrkr_abbrev == oa_abbrev
);

! echo "Again, drop the abbrev we already agree on"
delete from ottdarT_abbrev where exists (
	select 't' from marker join transcript on mrkr_zdb_id == tscript_mrkr_zdb_id
	 where tscript_load_id == oa_ottdarT
	   and mrkr_abbrev == oa_abbrev
);

-- #################################################################
! echo "Isolate the abbrevs that do not conflict with themselves"

select oa_abbrev dup
 from ottdarT_abbrev
 group by 1 having count(*) > 1
into temp tmp_dup with no log
;

select * from tmp_dup;

select * from ottdarT_abbrev where exists (
	select 't' from tmp_dup where dup == oa_abbrev
) into temp tmp_ott_abr with no log;

delete from  ottdarT_abbrev where exists (
	select 't' from tmp_dup where dup == oa_abbrev
);

drop table tmp_dup;

select count(*)to_go from ottdarT_abbrev;

! echo "upercase names will break loading "
select * from ottdarT_abbrev where oa_abbrev != lower(oa_abbrev);


! echo "update the unambigous new transcript abbrevs"
update marker set mrkr_abbrev = (
	select oa_abbrev
	 from transcript join ottdarT_abbrev on tscript_load_id  == oa_ottdarT
	 where tscript_mrkr_zdb_id == mrkr_zdb_id
) where exists (
	select 't'
	 from transcript join ottdarT_abbrev on tscript_load_id == oa_ottdarT
	 where tscript_mrkr_zdb_id == mrkr_zdb_id
);
delete from ottdarT_abbrev;
insert into ottdarT_abbrev select * from tmp_ott_abr;
drop table tmp_ott_abr;

update statistics high for table ottdarT_abbrev;

! echo "Try keeping the abbrev on the longer transcript"

select shrt.oa_ottdart shortstick
 from  ottdarT_abbrev shrt,  ottdarT_abbrev lng,
       db_link a, db_link b
 where shrt.oa_abbrev == lng.oa_abbrev
   and shrt.oa_ottdarT == b.dblink_acc_num
   and lng.oa_ottdarT == a.dblink_acc_num
   and a.dblink_length > b.dblink_length
   and a.dblink_length is not NULL
   and b.dblink_length is not NULL
into temp tmp_shrt with no log
;

-- #################################################################
! echo "Shorter transcripts get the less informative abbrev"
update ottdarT_abbrev set oa_abbrev = lower(oa_ottdarT) where exists(
	select 't' from tmp_shrt where oa_ottdarT == shortstick
);

! echo "rinse and repeat"
! echo "Isolate the abbrevs that do not conflict with themselves now"
select oa_abbrev dup
 from ottdarT_abbrev
 group by 1 having count(*) > 1
into temp tmp_dup with no log
;

select * from ottdarT_abbrev where exists (
	select 't' from tmp_dup where dup == oa_abbrev
) into temp tmp_ott_abr with no log;

delete from  ottdarT_abbrev where exists (
	select 't' from tmp_dup where dup == oa_abbrev
);
drop table tmp_dup;

! echo "update the unambigous new transcript abbrevs"
update marker set mrkr_abbrev = (
	select oa_abbrev
	 from transcript join ottdarT_abbrev on tscript_load_id  == oa_ottdarT
	 where tscript_mrkr_zdb_id == mrkr_zdb_id
) where exists (
	select 't'
	 from transcript join ottdarT_abbrev on tscript_load_id == oa_ottdarT
	 where tscript_mrkr_zdb_id == mrkr_zdb_id
);
delete from ottdarT_abbrev;
insert into ottdarT_abbrev select * from tmp_ott_abr;
drop table tmp_ott_abr;

update statistics high for table ottdarT_abbrev;
-- #################################################################

! echo "Of the abbrevs remaining, are many duplicates?"
select oa_abbrev, count(*) dups
 from ottdarT_abbrev
 group by 1 having count(*) > 1
;




! echo "Let older ottdarT keep informative abbrev"

select min(oa_ottdarT) keep
 from ottdarT_abbrev group by oa_abbrev
 into temp tmp_keep_name with no log
;
update ottdarT_abbrev set oa_abbrev = lower(oa_ottdarT) where not exists(
	select 't' from tmp_keep_name where oa_ottdarT == keep
);

drop table tmp_keep_name;

update marker set mrkr_abbrev = (
	select oa_abbrev
	 from transcript join ottdarT_abbrev on tscript_load_id  == oa_ottdarT
	 where tscript_mrkr_zdb_id == mrkr_zdb_id
) where exists (
	select 't'
	 from transcript join ottdarT_abbrev on tscript_load_id == oa_ottdarT
	 where tscript_mrkr_zdb_id == mrkr_zdb_id
);

! echo "although it is inefficent to use names instead of abbrevs"
! echo "for anything but formal gene names (b/c size)"
! echo "update the transcript names to equal the abbrevs where they differ"

update marker set mrkr_name = mrkr_abbrev
 where mrkr_type == 'TSCRIPT'
   and mrkr_name != mrkr_abbrev
   and exists (
   	select 't' from transcript
   	where mrkr_zdb_id == tscript_mrkr_zdb_id
   	  and tscript_load_id[1,8] == 'OTTDART0'
);

drop table ottdarT_abbrev;

! echo "transaction terminated externaly"
--rollback work;
--commit work;

